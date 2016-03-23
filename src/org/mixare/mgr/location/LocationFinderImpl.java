/*
 * Copyright (C) 2012- Peer internet solutions & Finalist IT Group
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare.mgr.location;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.mixare.Config;
import org.mixare.MixContext;
import org.mixare.R;
import org.mixare.mgr.downloader.DownloadManager;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * This class is repsonsible for finding the location, and sending it back to
 * the mixcontext.
 *
 * @author A. Egal
 */
class LocationFinderImpl implements LocationFinder {

    private static final long LOCATION_WAIT_TIME = 20 * 1000; //wait 20 seconds for the location updates to find the location

    private static final int ACCURACY_THRESHOLD = 200;
    private static final int TIME_THRESHOLD = 1000 * 60 * 2; //two minutes

	// frequency and minimum distance for update
	// this values will only be used after there's a good GPS fix
	// see back-off pattern discussion
	// http://stackoverflow.com/questions/3433875/how-to-force-gps-provider-to-get-speed-in-android
	// thanks Reto Meier for his presentation at gddde 2010
	private static final long CONTINUOUS_UPDATE_FREQ = 5000; // 5 seconds
	private static final float CONTINUOUS_UPDATE_DIST = 20; // 20 meters

    private LocationManager locationManager;
    private String bestLocationProvider;
    private final MixContext mixContext;
    private Location curLocation;
    private Location locationAtLastDownload; // not yet used
    private LocationFinderState state;
    private final ContinuousLocationObserver continuousLocationObserver;
    private List<InitialLocationResolver> initialLocationResolvers;
    private static Location initialLocation = new Location("");

    public LocationFinderImpl(MixContext mixContext) {
		this.mixContext = mixContext;
		this.continuousLocationObserver = new ContinuousLocationObserver(this);
		this.state = LocationFinderState.INACTIVE;
		this.initialLocationResolvers = new ArrayList<>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mixare.mgr.location.LocationFinder#initLocationSearch(android.content.Context
	 * )
	 */
    @Override
	public void initLocationSearch() { //throws SecurityException
		try {
			startSearchForBestLocationProvider();
			//temporary set the current location, until a good provider is found
			curLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), true));
            Log.d(Config.TAG, "LocationFinderImpl - initLocationSearch "+curLocation);
            if (curLocation == null) {
                curLocation = initialLocation;
			}
		} catch (Exception ex) {
            Log.d(Config.TAG, "LocationFinderImpl - initLocationSearch exception", ex);
            curLocation = initialLocation;
		}
	}

    @Override
    public void setInitialLocation(Location initialLocation){
        LocationFinderImpl.initialLocation = initialLocation;
    }

	private void startSearchForBestLocationProvider() { // throws SecurityException
		Timer timer = new Timer();
		for (String curProvider : locationManager.getAllProviders()) {
			if(locationManager.isProviderEnabled(curProvider)){
				InitialLocationResolver initialLocationResolver = new InitialLocationResolver(curProvider, this);
				initialLocationResolvers.add(initialLocationResolver);
				locationManager.requestLocationUpdates(curProvider, 0, 0, initialLocationResolver);
			}
		}
		timer.schedule(new LocationTimerTask(), LOCATION_WAIT_TIME); //wait for the location updates to find the location
	}

    private void stopSearchForBestLocationProvider() {
        for(InitialLocationResolver initialLocationResolver : initialLocationResolvers){
            locationManager.removeUpdates(initialLocationResolver);
        }
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mixare.mgr.location.LocationFinder#onWorkingProviderFound(android.content
	 * .Context)
	 */
    @Override
	public void onWorkingProviderFound(String provider, Location foundLocation) { // throws SecurityException
		if (bestLocationProvider != null) {
			Location bestLocation = locationManager.getLastKnownLocation(bestLocationProvider);
			if (isBetterLocation(foundLocation, bestLocation)) {
				curLocation = foundLocation;
				bestLocationProvider = provider;
			}
		} else {
			curLocation = foundLocation;
			bestLocationProvider = provider;
		}
        locationAtLastDownload=curLocation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mixare.mgr.location.LocationFinder#getCurrentLocation()
	 */
    @Override
	public Location getCurrentLocation() {
		if (curLocation == null) {
			initLocationSearch();
//			mixContext.getNotificationManager().
//			addNotification(mixContext.getString(R.string.location_not_found));
//			throw new RuntimeException("No GPS Found");
		}
		synchronized (curLocation) {
			return curLocation;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mixare.mgr.location.LocationFinder#getLocationAtLastDownload()
	 */
    @Override
	public Location getLocationAtLastDownload() {
		return locationAtLastDownload;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mixare.mgr.location.LocationFinder#setLocationAtLastDownload(android
	 * .location.Location)
	 */
    @Override
	public void setLocationAtLastDownload(Location locationAtLastDownload) {
		this.locationAtLastDownload = locationAtLastDownload;
        Log.d(Config.TAG,"setLocationAtLastDownload "+locationAtLastDownload);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mixare.mgr.location.LocationFinder#getGeomagneticField()
	 */
    @Override
	public GeomagneticField getGeomagneticField() {
		Location location = getCurrentLocation();
		GeomagneticField geomagneticField = new GeomagneticField(
				(float) location.getLatitude(),
				(float) location.getLongitude(),
				(float) location.getAltitude(), System.currentTimeMillis());
		return geomagneticField;
	}

	@Override
	public void setCurrentLocation(Location location) {
        mixContext.getDownloadManager().resetActivity();
		synchronized (curLocation) {
			curLocation = location;
		}
		mixContext.getActualMixViewActivity().refresh();
        mixContext.saveLocation(location);
		if (locationAtLastDownload == null) {
            locationAtLastDownload=location;
		}
	}

	@Override
	public void switchOn() {
		if (!LocationFinderState.ACTIVE.equals(state)) {
			locationManager = (LocationManager) mixContext.getSystemService(Context.LOCATION_SERVICE);
			state = LocationFinderState.CONFUSED;
		}
	}

	@Override
	public void switchOff() {
		if (locationManager != null) {
            stopSearchForBestLocationProvider();
			locationManager.removeUpdates(getObserver());
			state = LocationFinderState.INACTIVE;
		}
	}

	@Override
	public LocationFinderState getStatus() {
		return state;
	}
	
	private synchronized ContinuousLocationObserver getObserver() {
		return continuousLocationObserver;
	}

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TIME_THRESHOLD;
		boolean isSignificantlyOlder = timeDelta < -TIME_THRESHOLD;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > ACCURACY_THRESHOLD;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),	currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate	&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}


    // after 20 seconds, stop searching for best provider
    // if one was found, hand continuous updates over to MixViewActivity
    // if not, show error
	class LocationTimerTask extends TimerTask {

		@Override
		public void run() {
			//remove all location updates
            stopSearchForBestLocationProvider();
			if(bestLocationProvider != null){
				locationManager.removeUpdates(getObserver()); // just to make sure
				state = LocationFinderState.CONFUSED;
				mixContext.getActualMixViewActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						locationManager.requestLocationUpdates(bestLocationProvider, CONTINUOUS_UPDATE_FREQ, CONTINUOUS_UPDATE_DIST, getObserver());
					}
				});
				state = LocationFinderState.ACTIVE;
			} else { //no location/provider found, so show error
				mixContext.getActualMixViewActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mixContext.getNotificationManager().
						addNotification(mixContext.getString(R.string.location_not_found));
					}
				});
			}
		}
	}
}