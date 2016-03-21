package org.mixare.mgr.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * This class will be used to start each location provider for 20 seconds
 * and they will then listen for locations. This class will check for updates for
 * the observer. 
 * Using this method: http://stackoverflow.com/questions/3145089/
 * @author A. Egal
 */
public class LocationResolver implements LocationListener{

	private String provider;
	private LocationFinderImpl locationFinderImpl;
	
	public LocationResolver(String provider, LocationFinderImpl locationFinderImpl){
		this.provider = provider;
		this.locationFinderImpl = locationFinderImpl;
	}
	
	@Override
	public void onLocationChanged(Location location) {
		locationFinderImpl.locationCallback(provider);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
}
