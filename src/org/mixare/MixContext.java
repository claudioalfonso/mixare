/*
 * Copyright (C) 2010- Peer internet solutions
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
package org.mixare;

import org.mixare.lib.MixContextInterface;
import org.mixare.lib.render.Matrix;
import org.mixare.mgr.datasource.DataSourceManager;
import org.mixare.mgr.datasource.DataSourceManagerFactory;
import org.mixare.mgr.downloader.DownloadManager;
import org.mixare.mgr.downloader.DownloadManagerFactory;
import org.mixare.mgr.location.LocationFinder;
import org.mixare.mgr.location.LocationFinderFactory;
import org.mixare.mgr.notification.NotificationManager;
import org.mixare.mgr.notification.NotificationManagerFactory;
import org.mixare.mgr.webcontent.WebContentManager;
import org.mixare.mgr.webcontent.WebContentManagerFactory;

import android.content.ContentResolver;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

/**
 * Cares about location management and about the data (source, inputstream)
 */
public class MixContext extends ContextWrapper implements MixContextInterface {
	private static MixViewActivity mixViewActivity;
    private static MixContext instance;

    private final Matrix rotationM = new Matrix();
    private final SharedPreferences settings;

	private Location curDestination;
	private Location curLocation;

    /** Responsible for all download */
	private DownloadManager downloadManager;

	/** Responsible for all location tasks */
	private LocationFinder locationFinder;

	/** Responsible for data Source Management */
	private DataSourceManager dataSourceManager;

	/** Responsible for Web Content */
	private WebContentManager webContentManager;
	
	/** Responsible for Notification logging */
	private NotificationManager notificationManager;

    public synchronized static MixContext getInstance()
    {
        if (instance == null)	{
            instance = new MixContext();
        }
        return instance;
    }

	private MixContext() {
		super(mixViewActivity);

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        // TODO: RE-ORDER THIS SEQUENCE... IS NECESSARY?
		getDataSourceManager().refreshDataSources();

		if (!getDataSourceManager().isAtLeastOneDataSourceSelected()) {
			rotationM.toIdentity();
		}
		//getLocationFinder().switchOn();
        startLocationManager();

        getLocationFinder().initLocationSearch();
		curDestination=Config.parseLocationFromString(settings.getString(getString(R.string.pref_item_lastdest_key), getString(R.string.pref_item_lastdest_default)));

	}

    public void startLocationManager(){
        if(settings.getBoolean(getString(R.string.pref_item_autolocate_key),true)){
            getLocationFinder().switchOn();
        }
    }

	public String getStartUrl() {
		Intent intent = (getActualMixViewActivity()).getIntent();
		if (intent.getAction() != null
				&& intent.getAction().equals(Intent.ACTION_VIEW)) {
			return intent.getData().toString();
		} else {
			return "";
		}
	}

	public void getRM(Matrix dest) {
		synchronized (rotationM) {
			dest.set(rotationM);
		}
	}

	@Deprecated
	public void loadMixViewWebPage(String url) throws Exception {

	}

	public void updatePositionStatus(boolean working, boolean problem, String statusText) {

	}

	public void updateDataSourceStatus(boolean working, boolean problem, String statusText) {
		if(getActualMixViewActivity().hudView != null) {
			getActualMixViewActivity().hudView.setDataSourcesStatus(working, problem, statusText);
		}
	}

	public void updateSensorsStatus(boolean working, boolean problem, String statusText) {

	}

	public void updateSmoothRotation(Matrix smoothR) {
		synchronized (rotationM) {
			rotationM.set(smoothR);
		}
	}

    public SharedPreferences getSettings(){
        return settings;
    }

	public DataSourceManager getDataSourceManager() {
		if (this.dataSourceManager == null) {
			dataSourceManager = DataSourceManagerFactory
					.makeDataSourceManager(this);
		}
		return dataSourceManager;
	}

	public LocationFinder getLocationFinder() {
		if (this.locationFinder == null) {
			locationFinder = LocationFinderFactory.makeLocationFinder(this);
            locationFinder.setInitialLocation(Config.parseLocationFromString(settings.getString(getString(R.string.pref_item_lastfix_key), getString(R.string.pref_item_lastfix_default))));
		}
		return locationFinder;
	}

	public DownloadManager getDownloadManager() {
		if (this.downloadManager == null) {
			downloadManager = DownloadManagerFactory.makeDownloadManager(this);
		}
		return downloadManager;
	}

	public WebContentManager getWebContentManager() {
		if (this.webContentManager == null) {
			webContentManager = WebContentManagerFactory
					.makeWebContentManager(this);
		}
		return webContentManager;
	}

	public NotificationManager getNotificationManager() {
		if (this.notificationManager == null) {
			notificationManager = NotificationManagerFactory
					.makeNotificationManager(this);
		}
		return notificationManager;
	}
	
	public MixViewActivity getActualMixViewActivity() {
		synchronized (mixViewActivity) {
			return mixViewActivity;
		}
	}

	public static void setActualMixViewActivity(MixViewActivity mixViewActivity) {
		synchronized (mixViewActivity) {
			MixContext.mixViewActivity = mixViewActivity;
		}
	}

	public ContentResolver getContentResolver() {
		ContentResolver out = super.getContentResolver();
		if (super.getContentResolver() == null) {
			out = getActualMixViewActivity().getContentResolver();
		}
		return out;
	}
	
	/**
	 * Toast POPUP notification
	 * 
	 * @param string message
	 */
	public void doPopUp(final String string){
		getNotificationManager().addNotification(string);
	}

	/**
	 * Toast POPUP notification
	 * 
	 * @param RidOfString RidOfString
	 */
	public void doPopUp(int RidOfString) {
        doPopUp(this.getString(RidOfString));
	}

	public Location getCurDestination() {
		return curDestination;
	}

	public Location getCurLocation() {
        if(curLocation==null) {
            curLocation = getLocationFinder().getCurrentLocation();
        }
		return curLocation;
	}

	public void setCurLocation(Location curLocation, boolean manualOverride) {
		if(manualOverride) {
			settings.edit().putBoolean(getString(R.string.pref_item_autolocate_key), false).apply();  //switch off autolocating
            // TODO actually disable LocationFinder
		}
        settings.edit().putString(getString(R.string.pref_item_lastfix_key), Config.locationToString(curLocation)).apply();  //save in settings

        this.curLocation = curLocation;
	}

	public void setCurDestination(Location curDestination) {
        settings.edit().putString(getString(R.string.pref_item_lastdest_key), Config.locationToString(curDestination)).apply();  //save in settings

        this.curDestination = curDestination;
	}
}