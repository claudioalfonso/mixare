package org.mixare;

import android.location.Location;

public class Config {
    /** TAG for logging */
    public static final String TAG = "Mixare";
    /** string to name & access the preference file in the internal storage */
    public static final String PREFS_PLUGINS = "mixarePrefsPlugins";
    public static final String PREFS_DATASOURCES = "mixarePrefsDatasources";
    public static final String PREFS_DATASOURCES_XMLKEY = "xmlDataSources";

    public static final int DEFAULT_RANGE_PROGRESS = 37;

    public final static double DEFAULT_FIX_LAT = 51.46184; //Campus Schützenbahn, Eingang SA
    public final static double DEFAULT_FIX_LON = 7.01655;
    public final static int DEFAULT_FIX_HEIGHT = 0;
    public final static String DEFAULT_FIX_NAME = "defaultFix";
    public final static double DEFAULT_DESTINATION_LAT = 51.46301; //Mensa
    public final static double DEFAULT_DESTINATION_LON = 7.00396;
    public final static int DEFAULT_DESTINATION_HEIGHT = 0;
    public final static String DEFAULT_DESTINATION_NAME = "defaultDest";
    public static final String MANUAL_FIX_NAME = "manualSet";

    public static final int INTENT_REQUEST_CODE_MIXVIEW = 0;
    public static final int INTENT_REQUEST_CODE_CENTERMAP = 76;
    public static final int INTENT_REQUEST_CODE_DATASOURCES = 40;
    public static final int INTENT_REQUEST_CODE_PLUGINS = 35;
    public static final int INTENT_REQUEST_CODE_MARKERLIST = 42;
    public static final int INTENT_REQUEST_CODE_MAP = 20;
    public static final int INTENT_REQUEST_CODE_SETTINGS = 135;
    public static final int INTENT_REQUEST_CODE_PLUGIN_STATUS = 1;

    public static final int INTENT_RESULT_PLUGIN_STATUS_CHANGED = 1;
    public static final int INTENT_RESULT_PLUGIN_STATUS_NOT_CHANGED = 0;
    public static final int INTENT_RESULT_ACTIVITY = 0;

    public static final String INTENT_EXTRA_REFRESH_SCREEN ="RefreshScreen";
    public static final String INTENT_EXTRA_SEARCH_QUERY ="search";
    public static final String INTENT_EXTRA_MENUENTRY = "menuentry";
    public static final String INTENT_EXTRA_LATITUDE = "latitude";
    public static final String INTENT_EXTRA_LONGITUDE = "longitude";
    public static final String INTENT_EXTRA_DO_CENTER = "do_center";
    public static final String INTENT_EXTRA_CLOSED_ACTIVITY = "closed";

    public static final int SPLASHTIME = 1000; // 1 second

    public static boolean drawMarkerTextBlocks = true;

    public static Location getDefaultFix(){
        Location defaultFix = new Location(DEFAULT_FIX_NAME);

        defaultFix.setLatitude(DEFAULT_FIX_LAT);
        defaultFix.setLongitude(DEFAULT_FIX_LON);
        defaultFix.setAltitude(DEFAULT_FIX_HEIGHT);
        return defaultFix;
    }

    public static Location getDefaultDestination(){
        Location defaultDestination = new Location(DEFAULT_DESTINATION_NAME);

        defaultDestination.setLatitude(DEFAULT_DESTINATION_LAT);
        defaultDestination.setLongitude(DEFAULT_DESTINATION_LON);
        defaultDestination.setAltitude(DEFAULT_DESTINATION_HEIGHT);

        return defaultDestination;
    }

    public static Location getManualFix(){
        Location manualFix = new Location(MANUAL_FIX_NAME);
        manualFix.setTime(System.currentTimeMillis());
        return manualFix;
    }
}