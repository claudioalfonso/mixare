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

import org.mixare.mgr.downloader.DownloadManager;

import android.hardware.GeomagneticField;
import android.location.Location;

/**
 * A class implementing this interface is responsible for
 * finding the location and sending it back to the MixContext.
 */
public interface LocationFinder {

	//Possible status of LocationFinder
	enum LocationFinderState {
		ACTIVE, // Providing location information
		INACTIVE, // Not active
		CONFUSED // Some problem in internal state
	}

//	boolean isEnabled();

	/**
	 * Finds the location through the providers  
	 */
	void initLocationSearch();

    void setInitialLocation(Location initialLocation);

    /**
	 * A working location provider has been found: check if 
	 * the found location has the best accuracy.
	 */
	void onWorkingProviderFound(String provider, Location foundLocation);
	
	/**
	 * Returns the current location.
     */
	Location getCurrentLocation();

	/**
	 * Gets the location that was used in the last download for
	 * datasources.
	 * @return Location
	 */
	Location getLocationAtLastDownload();

	/**
	 * Sets the property to the location with the last successful download.
	 */
	void setLocationAtLastDownload(Location locationAtLastDownload);

	void setCurrentLocation(Location location);

	/**
	 * Request to activate the service
	 */
	void switchOn();

	/**
	 * Request to deactivate the service
	 */
	void switchOff();

	/**
	 * Status of service
	 * 
	 * @return Location
	 */
	LocationFinderState getStatus();

	/**
	 * 
	 * @return GeomagneticField
	 */
	GeomagneticField getGeomagneticField();

}