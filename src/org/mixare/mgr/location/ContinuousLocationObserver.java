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

import org.mixare.Config;
//import org.mixare.map.GoogleMap;

//import com.google.android.maps.GeoPoint;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

class ContinuousLocationObserver implements LocationListener {
	
	private LocationFinder locationFinder;

	public ContinuousLocationObserver(LocationFinder locationFinder) {
		super();
		this.locationFinder = locationFinder;
	}

	public void onLocationChanged(Location location) {
		Log.d(Config.TAG, "Normal Location Changed: " + location.getProvider()
						+ " lat: " + location.getLatitude() + " lon: "
						+ location.getLongitude() + " alt: "
						+ location.getAltitude() + " acc: "
						+ location.getAccuracy());
		try {
			recordPosition(location);
			Log.v(Config.TAG, "Location Changed: " + location.getProvider()
							+ " lat: " + location.getLatitude() + " lon: "
							+ location.getLongitude() + " alt: "
							+ location.getAltitude() + " acc: "
							+ location.getAccuracy());
			locationFinder.setCurrentLocation(location);
		} catch (Exception ex) {
			Log.e(Config.TAG, this.getClass().getName(), ex);
		}
	}

	private void recordPosition(Location location) {
		//GoogleMap.addWalkingPathPosition(new GeoPoint((int) (location.getLatitude() * 1E6),(int) (location.getLongitude() * 1E6)));
	}
	
	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
}