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

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.mixare.data.DataSourceList;
import org.mixare.data.DataSourceStorage;
import org.mixare.gui.HudView;
import org.mixare.gui.LicensePreference;
import org.mixare.gui.opengl.OpenGLAugmentationView;
import org.mixare.gui.opengl.OpenGLMarker;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.render.Matrix;
import org.mixare.map.MapActivity;
import org.mixare.mgr.HttpTools;
import org.mixare.route.RouteManager;
import org.mixare.settings.SettingsActivity;

import static android.hardware.SensorManager.SENSOR_DELAY_GAME;


/**
 * This class is the main application which uses the other classes for different
 * functionalities.
 * It sets up the camera screen and the augmented screen which is in front of the
 * camera screen.
 * It also handles the main sensor events, touch events and location events.
 */
public class MixViewActivity extends DrawerMenuActivity implements SensorEventListener, OnTouchListener {

    /* Different error messages */
    protected static final int UNSUPPORTED_HARDWARE = 0;
    protected static final int GPS_ERROR = 1;
    public static final int GENERAL_ERROR = 2;
    protected static final int NO_NETWORK_ERROR = 4;

    private static final int PERMISSIONS_REQUEST_CAMERA = 1;
    private static final int PERMISSIONS_ACCESS_FINE_LOCATION = 2;
    private static final int PERMISSIONS_WRITE_EXTERNAL_STORAGE = 3;

	private CameraSurface cameraSurface;
    private FrameLayout cameraView;
    public HudView hudView;
    private SimpleAugmentationView simpleAugmentationView;
    private OpenGLAugmentationView openGLAugmentationView;

    private boolean isInited;
    private boolean fError;

    private static PaintScreen paintScreen;
	private static MarkerRenderer markerRenderer;

	private SensorManager sensorManager;
	private Sensor orientationSensor;

    private SharedPreferences settings;

	/**
	 * Main application Launcher.
	 * Does:
	 * - Lock Screen.
	 * - Initiate Camera View
	 * - Initiate markerRenderer {@link MarkerRenderer#draw() MarkerRenderer}
	 * - Display License Agreement if mixViewActivity first used.
	 *
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			handleIntent(getIntent());

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

			getMixViewData().setSensorMgr((SensorManager) getSystemService(SENSOR_SERVICE));

			killOnError();
			//requestWindowFeature(Window.FEATURE_NO_TITLE);

			if(getActionBar() != null){
				getActionBar().hide();
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

				if (ContextCompat.checkSelfPermission(this,
						Manifest.permission.CAMERA)
						!= PackageManager.PERMISSION_GRANTED) {

					ActivityCompat.requestPermissions(this,
							new String[]{Manifest.permission.CAMERA},
							PERMISSIONS_REQUEST_CAMERA);
				}
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
					ActivityCompat.requestPermissions(this,
							new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
							PERMISSIONS_ACCESS_FINE_LOCATION);
				}
				if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
					ActivityCompat.requestPermissions(this,
							new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE},
							PERMISSIONS_WRITE_EXTERNAL_STORAGE);
				}
			}

            settings = PreferenceManager.getDefaultSharedPreferences(this);

            maintainViews();

			simpleAugmentationView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent me) {
                    hudView.hideRangeBar();

                    try {
                        killOnError();
                        float xPress = me.getX();
                        float yPress = me.getY();
                        if (me.getAction() == MotionEvent.ACTION_UP) {
                            getMarkerRenderer().clickEvent(xPress, yPress);
                        }
                        return true;
                    } catch (Exception ex) {
                        // doError(ex);
                        Log.e(Config.TAG, this.getClass().getName(), ex);
                        //return super.onTouchEvent(me);
                    }
                    return true;
                }

            });


			if (!isInited) {
				setPaintScreen(new PaintScreen());
                getMarkerRenderer();

				refreshDownload();
				isInited = true;
			}

			/* check if the application is launched for the first time */
			if (settings.getBoolean(getString(R.string.pref_item_firstacess_key), true)) {
				firstAccess();
			}
		} catch (Exception ex) {
            doError(ex, GENERAL_ERROR);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {


		switch (requestCode) {
			case PERMISSIONS_REQUEST_CAMERA: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					maintainViews();

				} else {

				}
				return;
			}

		}
	}

	@Override
	public MixViewDataHolder getMixViewData() {
		MixContext.setActualMixViewActivity(this);
		return MixViewDataHolder.getInstance();
	}

	/**
	 * Part of Android LifeCycle that gets called when "Activity" MixViewActivity is
	 * being navigated away. <br/>
	 * Does: - Release Screen Lock - Unregister Sensors.
	 * {@link android.hardware.SensorManager SensorManager} - Unregister
	 * Location Manager. {@link org.mixare.mgr.location.LocationFinder
	 * LocationFinder} - Switch off Download Thread.
	 * {@link org.mixare.mgr.downloader.DownloadManager DownloadManager} -
	 * Cancel markerRenderer refresh Timer. <br/>
	 * {@inheritDoc}
	 */
	@Override
	protected void onPause() {
		super.onPause();
		try {
			cameraSurface.surfaceDestroyed(null);
			sensorManager.unregisterListener(this);

			try {
				getMixViewData().getSensorMgr().unregisterListener(this,
						getMixViewData().getSensorGrav());
				getMixViewData().getSensorMgr().unregisterListener(this,
						getMixViewData().getSensorMag());
				getMixViewData().getSensorMgr().unregisterListener(this,
						getMixViewData().getSensorGyro());
				getMixViewData().getSensorMgr().unregisterListener(this);
				getMixViewData().setSensorGrav(null);
				getMixViewData().setSensorMag(null);
				getMixViewData().setSensorGyro(null);

				MixContext.getInstance().getLocationFinder()
						.switchOff();
				MixContext.getInstance().getDownloadManager()
						.switchOff();

				MixContext.getInstance().getNotificationManager()
						.setEnabled(false);
				MixContext.getInstance().getNotificationManager()
						.clear();
				if (getMarkerRenderer() != null) {
					getMarkerRenderer().cancelRefreshTimer();
				}
			} catch (Exception ignore) {
			}

			if (fError) {
				finish();
			}
		} catch (Exception ex) {
            doError(ex, GENERAL_ERROR);
		}
	}

	/**
	 * Mixare Activities Pipe message communication.
	 * Receives results from other launched activities
	 * and base on the result returned, it either refreshes screen or not.
	 * Default value for refreshing is false
	 * <br/>
	 * {@inheritDoc}
	 */
	protected void onActivityResult(final int requestCode,
									final int resultCode, Intent data) {
		//Log.d(TAG + " WorkFlow", "MixViewActivity - onActivityResult Called");
		// check if the returned is request to refresh screen (setting might be
		// changed)

		if (requestCode == Config.INTENT_REQUEST_CODE_PLUGINS) {
			if (resultCode == Config.INTENT_RESULT_PLUGIN_STATUS_CHANGED) {
				final AlertDialog.Builder dialog = new AlertDialog.Builder(this);

				dialog.setTitle(R.string.launch_plugins);
				dialog.setMessage(R.string.plugins_changed);
				dialog.setCancelable(false);

				// Always activate new plugins

//				final CheckBox checkBox = new CheckBox(ctx);
//				checkBox.setText(R.string.remember_this_decision);
//				dialog.setView(checkBox);

				dialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int whichButton) {
						startActivity(new Intent(MixContext.getInstance().getApplicationContext(),
								PluginLoaderActivity.class));
						finish();
					}
				});

				dialog.setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface d, int whichButton) {
						d.dismiss();
					}
				});

				dialog.show();
			}
		}
		try {
			if (data.getBooleanExtra(Config.INTENT_EXTRA_REFRESH_SCREEN, false)) {
				Log.d(Config.TAG + " WorkFlow",
						"MixViewActivity - Received Refresh Screen Request .. about to refresh");
				repaint();
				refreshDownload();
			}
		} catch (Exception ex) {
			// do nothing do to mix of return results.
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * Part of Android LifeCycle that gets called when "MixViewActivity" resumes.
	 * <br/>
	 * Does:
	 * - Acquire Screen Lock
	 * - Refreshes Data and Downloads
	 * - Initiate four Matrixes that holds user's rotation markerRenderer.
	 * - Re-register Sensors. {@link android.hardware.SensorManager SensorManager}
	 * - Re-register Location Manager. {@link org.mixare.mgr.location.LocationFinder LocationFinder}
	 * - Switch on Download Thread. {@link org.mixare.mgr.downloader.DownloadManager DownloadManager}
	 * - restart markerRenderer refresh Timer.
	 * <br />
	 * {@inheritDoc}
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if(openGLAugmentationView != null) {
			//routeRenderer.start();
			openGLAugmentationView.onResume();
		}
        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
		sensorManager.registerListener(openGLAugmentationView, orientationSensor, SensorManager.SENSOR_DELAY_NORMAL);

		try {
			killOnError();
			MixContext.setActualMixViewActivity(this);
			HttpTools.setContext(MixContext.getInstance());

			//repaint(); //repaint when requested
			getMarkerRenderer().doStart();
			getMarkerRenderer().clearEvents();
			MixContext.getInstance().getNotificationManager().setEnabled(true);
			refreshDownload();

			MixContext.getInstance().getDataSourceManager().refreshDataSources();

			float angleX, angleY;

			int marker_orientation = -90;

			int rotation = Compatibility.getRotation(this);

			// display text from left to right and keep it horizontal
			angleX = (float) Math.toRadians(marker_orientation);
			getMixViewData().getM1().set(1f, 0f, 0f, 0f,
					(float) Math.cos(angleX),
					(float) -Math.sin(angleX), 0f,
					(float) Math.sin(angleX),
					(float) Math.cos(angleX));
			angleX = (float) Math.toRadians(marker_orientation);
			angleY = (float) Math.toRadians(marker_orientation);
			if (rotation == 1) {
				getMixViewData().getM2().set(1f, 0f, 0f, 0f,
						(float) Math.cos(angleX),
						(float) -Math.sin(angleX), 0f,
						(float) Math.sin(angleX),
						(float) Math.cos(angleX));
				getMixViewData().getM3().set((float) Math.cos(angleY), 0f,
						(float) Math.sin(angleY), 0f, 1f, 0f,
						(float) -Math.sin(angleY), 0f,
						(float) Math.cos(angleY));
			} else {
				getMixViewData().getM2().set((float) Math.cos(angleX), 0f,
						(float) Math.sin(angleX), 0f, 1f, 0f,
						(float) -Math.sin(angleX), 0f,
						(float) Math.cos(angleX));
				getMixViewData().getM3().set(1f, 0f, 0f, 0f,
						(float) Math.cos(angleY),
						(float) -Math.sin(angleY), 0f,
						(float) Math.sin(angleY),
						(float) Math.cos(angleY));

			}

			getMixViewData().getM4().toIdentity();

			for (int i = 0; i < getMixViewData().getHistR().length; i++) {
				getMixViewData().getHistR()[i] = new Matrix();
			}

			getMixViewData().addListSensors(getMixViewData().getSensorMgr().getSensorList(
					Sensor.TYPE_ACCELEROMETER));
			if (getMixViewData().getSensor(0).getType() == Sensor.TYPE_ACCELEROMETER ) {
				getMixViewData().setSensorGrav(getMixViewData().getSensor(0));
			}//else report error (unsupported hardware)

			getMixViewData().addListSensors(getMixViewData().getSensorMgr().getSensorList(
					Sensor.TYPE_MAGNETIC_FIELD));
			if (getMixViewData().getSensor(1).getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				getMixViewData().setSensorMag(getMixViewData().getSensor(1));
			}//else report error (unsupported hardware)

			if (!getMixViewData().getSensorMgr().getSensorList(Sensor.TYPE_GYROSCOPE).isEmpty()){
				getMixViewData().addListSensors(getMixViewData().getSensorMgr().getSensorList(
						Sensor.TYPE_GYROSCOPE));
				if (getMixViewData().getSensor(2).getType() == Sensor.TYPE_GYROSCOPE) {
					getMixViewData().setSensorGyro(getMixViewData().getSensor(2));
				}
				getMixViewData().getSensorMgr().registerListener(this,
						getMixViewData().getSensorGyro(), SENSOR_DELAY_GAME);
			}

				getMixViewData().getSensorMgr().registerListener(this,
						getMixViewData().getSensorGrav(), SENSOR_DELAY_GAME);
				getMixViewData().getSensorMgr().registerListener(this,
						getMixViewData().getSensorMag(), SENSOR_DELAY_GAME);

			try {
				GeomagneticField gmf = MixContext.getInstance()
						.getLocationFinder().getGeomagneticField();
				angleY = (float) Math.toRadians(-gmf.getDeclination());
				getMixViewData().getM4().set((float) Math.cos(angleY), 0f,
						(float) Math.sin(angleY), 0f, 1f, 0f,
						(float) -Math.sin(angleY), 0f,
						(float) Math.cos(angleY));
			} catch (Exception ex) {
				doError(ex, GPS_ERROR);
			}

			if (!isNetworkAvailable()) {
				Log.d(Config.TAG, "no network");
				doError(null, NO_NETWORK_ERROR);
			} else {
				Log.d(Config.TAG, "network");
			}

			MixContext.getInstance().getDownloadManager().switchOn();
			MixContext.getInstance().getLocationFinder().switchOn();
		} catch (Exception ex) {
            doError(ex, GENERAL_ERROR);
			try {
				if (getMixViewData().getSensorMgr() != null) {
					getMixViewData().getSensorMgr().unregisterListener(this,
							getMixViewData().getSensorGrav());
					getMixViewData().getSensorMgr().unregisterListener(this,
							getMixViewData().getSensorMag());
					getMixViewData().getSensorMgr().unregisterListener(this,
							getMixViewData().getSensorGyro());
					getMixViewData().setSensorMgr(null);
				}

				if (MixContext.getInstance() != null) {
					MixContext.getInstance().getLocationFinder()
							.switchOff();
					MixContext.getInstance().getDownloadManager()
							.switchOff();
				}
			} catch (Exception ignore) {
			}
		} finally {
			//This does not conflict with registered sensors (sensorMag, sensorGrav)
			//This is a place holder to API returned listed of sensors, we registered
			//what we need, the rest is unnecessary.
			getMixViewData().clearAllSensors();
		}

		Log.d(Config.TAG, "resume");
		if (getMarkerRenderer() == null) {
			return;
		}
		if (getMarkerRenderer().isFrozen()
				&& getMixViewData().getSearchNotificationTxt() == null) {
			getMixViewData().setSearchNotificationTxt(new TextView(this));
			getMixViewData().getSearchNotificationTxt().setWidth(
					getPaintScreen().getWidth());
			getMixViewData().getSearchNotificationTxt().setPadding(10, 2, 0, 0);
			getMixViewData().getSearchNotificationTxt().setText(
					getString(R.string.search_active_1) + " "
							+ DataSourceList.getDataSourcesStringList()
							+ getString(R.string.search_active_2));
			;
			getMixViewData().getSearchNotificationTxt().setBackgroundColor(
					Color.DKGRAY);
			getMixViewData().getSearchNotificationTxt().setTextColor(
					Color.WHITE);

			getMixViewData().getSearchNotificationTxt()
					.setOnTouchListener(this);
			addContentView(getMixViewData().getSearchNotificationTxt(),
					new LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT));
		} else if (!getMarkerRenderer().isFrozen()
				&& getMixViewData().getSearchNotificationTxt() != null) {
			getMixViewData().getSearchNotificationTxt()
					.setVisibility(View.GONE);
			getMixViewData().setSearchNotificationTxt(null);
		}
	}

	/**
	 * Customize Activity after switching back to it.
	 * Currently it maintain and ensures markerRenderer creation.
	 * <br/>
	 * {@inheritDoc}
	 */
	protected void onRestart() {
		super.onRestart();
		maintainViews();
	}

	/**
	 * {@inheritDoc}
	 * Deallocate memory and stops threads.
	 * Please don't rely on this function as it's killable,
	 * and might not be called at all.
	 */
	protected void onDestroy(){
		try{

			MixContext.getInstance().getDownloadManager().shutDown();
			getMixViewData().getSensorMgr().unregisterListener(this);
			getMixViewData().setSensorMgr(null);
			/*
			 * Invoked when the garbage collector has detected that this
			 * instance is no longer reachable. The default implementation does
			 * nothing, but this method can be overridden to free resources.
			 *
			 * Do we have to create our own finalize?
			 */
		} catch (Exception e) {
			//do nothing we are shutting down
		} catch (Throwable e) {
			//finalize error. (this function does nothing but call native API and release
			//any synchronization-locked messages and threads deadlocks.
			Log.e(Config.TAG, e.getMessage());
		} finally {
			super.onDestroy();
		}
	}

	private void maintainViews() {
		maintainCamera();
		maintainAugmentedView();
		if (settings.getBoolean(getString(R.string.pref_item_usehud_key), true)) {
			maintainHudView();
		}
        if (settings.getBoolean(getString(R.string.pref_item_routing_key), true)){
            maintainOpenGLView();
        }
	}

	/* ********* Operators ***********/

	/**
	 * View Repainting.
	 * It deletes viewed data and initiate new one. {@link MarkerRenderer MarkerRenderer}
	 */
	public void repaint() {
		// clear stored data
		getMarkerRenderer().clearEvents();
		setPaintScreen(new PaintScreen());
    }

	/**
	 * Checks cameraSurface, if it does not exist, it creates one.
	 */
	private void maintainCamera() {
		cameraView = (FrameLayout) findViewById(R.id.drawermenu_content_camerascreen);
        if (cameraSurface == null) {
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
	//				cameraSurface = new Camera2Surface(this);
                    cameraSurface = new CameraSurface(this);

                } else {
					cameraSurface = new CameraSurface(this);
				}

            cameraView.addView(cameraSurface);
			} else {
				cameraView.removeView(cameraSurface);
				cameraView.addView(cameraSurface);
			}
             Log.d(Config.TAG + " cameraSurface","w="+cameraSurface.getWidth()+ ", h="+cameraSurface.getHeight());
        Log.d(Config.TAG + " camView", "w=" + cameraView.getWidth() + ", h=" + cameraView.getHeight());
       cameraView.getLayoutParams().width=800;
   //     cameraView.getLayoutParams().height=480;

    }

	/**
	 * Checks simpleAugmentationView, if it does not exist, it creates one.
	 */
	private void maintainAugmentedView() {
		if (simpleAugmentationView == null) {
			simpleAugmentationView = new SimpleAugmentationView(this);
			cameraView.addView(simpleAugmentationView, new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			//addContentView(augScreen, new LayoutParams(LayoutParams.WRAP_CONTENT,
			//		LayoutParams.WRAP_CONTENT));
		}
		else{

			((ViewGroup) simpleAugmentationView.getParent()).removeView(simpleAugmentationView);
			//addContentView(simpleAugmentationView, new LayoutParams(LayoutParams.WRAP_CONTENT,
			//		LayoutParams.WRAP_CONTENT));
			cameraView.addView(simpleAugmentationView, new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
		}

	}

	/**
	 * Checks HUD GUI, if it does not exist, it creates one.
	 */
	private void maintainHudView() {
		if (hudView == null) {
            hudView = new HudView(this);
		}
        else {
            ((ViewGroup) hudView.getParent()).removeView(hudView);
        }
        addContentView(hudView, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
    }

	private void maintainOpenGLView() {
        if(openGLAugmentationView==null) {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            //orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            openGLAugmentationView = new OpenGLAugmentationView(this, sensorManager);

            /*
            openGLView.requestFocus();
            openGLView.setFocusableInTouchMode(true);
            openGLView.setZOrderOnTop(true);
            openGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            openGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            openGLView.setRenderer(routeRenderer);
            openGLView.getHolder().setFormat(PixelFormat.RGBA_8888);
            openGLView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
            */

            //			Log.d(Config.TAG, "info 1: aktuelle Postition: " + curLocation.getLongitude() + ", " + curLocation.getLatitude());
            //			Log.i ("Info11",  "OrientatioN" +cameraView.getDisplay().getRotation());
        }
        else {
            ((ViewGroup) openGLAugmentationView.getParent()).removeView(openGLAugmentationView);
        }
        cameraView.addView(openGLAugmentationView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }
	/**
	 * Refreshes Download TODO refresh downloads
	 */
	public void refreshDownload(){
		MixContext.getInstance().getDownloadManager().switchOn();
//		try {
//			if (getMixViewData().getDownloadThread() != null){
//				if (!getMixViewData().getDownloadThread().isInterrupted()){
//					getMixViewData().getDownloadThread().interrupt();
//					MixContext.getInstance().getDownloadManager().restart();
//				}
//			}else { //if no download thread found
//				getMixViewData().setDownloadThread(new Thread(getMixViewData()
//						.getMixContext().getDownloadManager()));
//				//@TODO Syncronize DownloadManager, call Start instead of run.
//				mixViewData.getMixContext().getDownloadManager().run();
//			}
//		}catch (Exception ex){
//		}
	}

	/**
	 * Refreshes Viewed Data.
	 */
	public void refresh(){
		markerRenderer.refresh();
        update3D();
	}

	public void setErrorDialog(int error) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		switch (error) {
		case NO_NETWORK_ERROR:
			builder.setMessage(getString(R.string.connection_error_dialog));
			break;
		case GPS_ERROR:
			builder.setMessage(getString(R.string.gps_error_dialog));
			break;
		case GENERAL_ERROR:
			builder.setMessage(getString(R.string.general_error_dialog));
			break;
		case UNSUPPORTED_HARDWARE:
			builder.setMessage(getString(R.string.unsupportet_hardware_dialog));
			break;
		}

		/*Retry*/
		builder.setPositiveButton(R.string.connection_error_dialog_button1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // "restart" mixare
                startActivity(new Intent(MixContext.getInstance().getApplicationContext(),
                        PluginLoaderActivity.class));
                finish();
            }
        });
		if (error == GPS_ERROR) {
			/* Open settings */
			builder.setNeutralButton(R.string.connection_error_dialog_button2,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							try {
								Intent intent1 = new Intent(
										Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								startActivityForResult(intent1, 42);
							} catch (Exception e) {
								Log.d(Config.TAG, "No Location Settings");
							}
						}
					});
		} else if (error == NO_NETWORK_ERROR) {
			builder.setNeutralButton(R.string.connection_error_dialog_button2,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							try {
								Intent intent1 = new Intent(
										Settings.ACTION_DATA_ROAMING_SETTINGS);
								ComponentName cName = new ComponentName(
										"com.android.phone",
										"com.android.phone.Settings");
								intent1.setComponent(cName);
								startActivityForResult(intent1, 42);
							} catch (Exception e) {
								Log.d(Config.TAG, "No Network Settings");
							}
						}
					});
		}
		/*Close application*/
		builder.setNegativeButton(R.string.connection_error_dialog_button3, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });

		AlertDialog alert = builder.create();
		alert.show();
	}

    /**
	 * Handle First time users. It display license agreement and store user's
	 * acceptance.
	 *
	 */
	private void firstAccess() {
		SharedPreferences.Editor editor = settings.edit();

		AlertDialog licenseDialog = new LicensePreference(this).getDialog();
        licenseDialog.show();
		editor.putBoolean(getString(R.string.pref_item_firstacess_key), false);

		// value for maximum POI for each selected OSM URL to be active by
		// default is 5
		editor.putInt(getString(R.string.pref_item_osmmaxobjects_key), 5);
		editor.apply();

		// add the default datasources to the preferences file
		DataSourceStorage.getInstance().fillDefaultDataSources();
	}



	/**
	 * Checks whether a network is available or not
	 * @return True if connected, false if not
	 */
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	public void selectItem(int position) {
		int menuItemId=getResources().obtainTypedArray(R.array.menu_item_titles).getResourceId(position,-1);
        Intent intent=null;
		switch (menuItemId) {
		/* List markerRenderer */
			case R.string.menu_item_list:
			/*
			 * if the list markers is not empty
			 */
				if (getMarkerRenderer().getDataHandler().getMarkerCount() > 0) {
                    intent = new Intent(MixViewActivity.this, MarkerListActivity.class);
                    intent.setAction(Intent.ACTION_VIEW);
					startActivityForResult(intent, Config.INTENT_REQUEST_CODE_MARKERLIST);
				}
			/* if the list is empty */
				else {
					markerRenderer.getContext().getNotificationManager().
							addNotification(getString(R.string.empty_list));
				}
				break;
			case R.string.menu_item_map:
                intent = new Intent(MixViewActivity.this, MapActivity.class);
				startActivityForResult(intent, Config.INTENT_REQUEST_CODE_MAP);
				break;
			case R.string.menu_item_range:
                hudView.showRangeBar();
                drawerLayout.closeDrawer(drawerList);
                break;
			case R.string.menu_item_search:
				onSearchRequested();
				break;
			case R.string.menu_item_route:
                new MarkerListFragment().show(getFragmentManager(), "TAG");
				break;
			case R.string.menu_item_settings:
                intent = new Intent(MixViewActivity.this, SettingsActivity.class);
				startActivityForResult(intent, Config.INTENT_REQUEST_CODE_SETTINGS);
                break;
			default:
				break;
		}
	}

    public void update3D(){
        Location startLocation = Config.getDefaultFix();
        Location endLocation = Config.getDefaultDestination();

        startLocation = MixViewDataHolder.getInstance().getCurLocation();
        endLocation = MixViewDataHolder.getInstance().getCurDestination();

        /*
        startLocation = new Location("TEST_LOC");
        startLocation.setLatitude(51.50595);
        startLocation.setLongitude(7.44919);

        endLocation = new Location("TEST_DEST");
        endLocation.setLatitude(51.50658);
        endLocation.setLongitude(7.45098);
        */

        if(openGLAugmentationView!=null) {
            RouteManager r = new RouteManager(openGLAugmentationView);
            r.getRoute(startLocation, endLocation);
            openGLAugmentationView.routeRenderer.updatePOIMarker(getMarkerRenderer().getDataHandler().getCopyOfMarkers(OpenGLMarker.class));
        }
    }

	public void onSensorChanged(SensorEvent evt) {
		try {
			if (getMixViewData().getSensorGyro() != null) {
				
				if (evt.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
					getMixViewData().setGyro(evt.values);
				}
				
				if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
					getMixViewData().setGrav(
							getMixViewData().getGravFilter().lowPassFilter(evt.values,
									getMixViewData().getGrav()));
				} else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
					getMixViewData().setMag(
							getMixViewData().getMagFilter().lowPassFilter(evt.values,
									getMixViewData().getMag()));
				}
				getMixViewData().setAngle(
						getMixViewData().getMagFilter().complementaryFilter(
								getMixViewData().getGrav(),
								getMixViewData().getGyro(), 30,
								getMixViewData().getAngle()));
				
				SensorManager.getRotationMatrix(
						getMixViewData().getRTmp(),
						getMixViewData().getI(), 
						getMixViewData().getGrav(),
						getMixViewData().getMag());
			} else {
				if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
					getMixViewData().setGrav(evt.values);
				} else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
					getMixViewData().setMag(evt.values);
				}
				SensorManager.getRotationMatrix(
						getMixViewData().getRTmp(),
						getMixViewData().getI(), 
						getMixViewData().getGrav(),
						getMixViewData().getMag());
			}
			
			simpleAugmentationView.postInvalidate();
			hudView.postInvalidate();

			int rotation = Compatibility.getRotation(this);

			if (rotation == 1) {
				SensorManager.remapCoordinateSystem(getMixViewData().getRTmp(),
						SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z,
						getMixViewData().getRot());
			} else {
				SensorManager.remapCoordinateSystem(getMixViewData().getRTmp(),
						SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z,
						getMixViewData().getRot());
			}
			getMixViewData().getTempR().set(getMixViewData().getRot()[0],
					getMixViewData().getRot()[1], getMixViewData().getRot()[2],
					getMixViewData().getRot()[3], getMixViewData().getRot()[4],
					getMixViewData().getRot()[5], getMixViewData().getRot()[6],
					getMixViewData().getRot()[7], getMixViewData().getRot()[8]);

			getMixViewData().getFinalR().toIdentity();
			getMixViewData().getFinalR().prod(getMixViewData().getM4());
			getMixViewData().getFinalR().prod(getMixViewData().getM1());
			getMixViewData().getFinalR().prod(getMixViewData().getTempR());
			getMixViewData().getFinalR().prod(getMixViewData().getM3());
			getMixViewData().getFinalR().prod(getMixViewData().getM2());
			getMixViewData().getFinalR().invert();
			
			getMixViewData().getHistR()[getMixViewData().getrHistIdx()]
					.set(getMixViewData().getFinalR());
			
			int histRLenght = getMixViewData().getHistR().length;
			
			getMixViewData().setrHistIdx(getMixViewData().getrHistIdx() + 1);
			if (getMixViewData().getrHistIdx() >= histRLenght)
				getMixViewData().setrHistIdx(0);

			getMixViewData().getSmoothR().set(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,
					0f);
			for (int i = 0; i < histRLenght; i++) {
				getMixViewData().getSmoothR().add(
						getMixViewData().getHistR()[i]);
			}
			getMixViewData().getSmoothR().mult(
					1 / (float) histRLenght);

			MixContext.getInstance().updateSmoothRotation(
					getMixViewData().getSmoothR());
		} catch (Exception ex) {
			Log.e(Config.TAG, "MixViewActivity onSensorChanged()",ex);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		hudView.hideRangeBar();
		
		try {
			killOnError();

			float xPress = me.getX();
			float yPress = me.getY();
			if (me.getAction() == MotionEvent.ACTION_UP) {
				getMarkerRenderer().clickEvent(xPress, yPress);
			}

			return true;
		} catch (Exception ex) {
			// doError(ex);
            Log.e(Config.TAG, this.getClass().getName(), ex);
			return super.onTouchEvent(me);
		}
	}

    /*
     * Handler for physical key presses (menu key, back key)
     */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		try {
			killOnError();

			//if range bar was visible, hide it
			if (hudView.isRangeBarVisible()) {
                hudView.hideRangeBar();
				if (keyCode == KeyEvent.KEYCODE_MENU) {
					return super.onKeyDown(keyCode, event);
				}
				return true;
			}

			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (getMarkerRenderer().isDetailsView()) {
					getMarkerRenderer().keyEvent(keyCode);
					getMarkerRenderer().setDetailsView(false);
					return true;
				} else {
					Intent close = new Intent();
					close.putExtra(Config.INTENT_EXTRA_CLOSED_ACTIVITY, this.getLocalClassName());
					setResult(Config.INTENT_RESULT_ACTIVITY, close);
					finish();
					return super.onKeyDown(keyCode, event);
				}
			} else if (keyCode == KeyEvent.KEYCODE_MENU) {
				return super.onKeyDown(keyCode, event);
			} else {
				getMarkerRenderer().keyEvent(keyCode);
				return false;
			}

		} catch (Exception ex) {
            Log.e(Config.TAG, "MixViewActivity", ex);

			return super.onKeyDown(keyCode, event);
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
				&& accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
				&& getMixViewData().getCompassErrorDisplayed() == 0) {
			for (int i = 0; i < 2; i++) {
				markerRenderer.getContext().getNotificationManager().
				addNotification(getString(R.string.compass_unreliable));
			}
			getMixViewData().setCompassErrorDisplayed(
					getMixViewData().getCompassErrorDisplayed() + 1);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		getMarkerRenderer().setFrozen(false);
		if (getMixViewData().getSearchNotificationTxt() != null) {
			getMixViewData().getSearchNotificationTxt()
					.setVisibility(View.GONE);
			getMixViewData().setSearchNotificationTxt(null);
		}
		return true;
	}

	/* ************ Handlers ************ */

	public void doError(Exception ex1, int error) {
		if (!fError) {
			fError = true;

			setErrorDialog(error);

			try {
                Log.e(Config.TAG, "MixViewActivity doError 1", ex1);
			} catch (Exception ex2) {
                Log.e(Config.TAG, "MixViewActivity doError 2", ex2);
			}
		}

		try {
			simpleAugmentationView.invalidate();
		} catch (Exception ignore) {
		}
	}

	public void killOnError() throws Exception {
		if (fError)
			throw new Exception();
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			intent.setClass(this, MarkerListActivity.class);
			startActivity(intent);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}


	/* ******* Getter and Setters ********** */

	/**
	 * @return the paintScreen
	 */
	static PaintScreen getPaintScreen() {
		return paintScreen;
	}

	/**
	 * @param paintScreen the paintScreen to set
	 */
	static void setPaintScreen(PaintScreen paintScreen) {
		MixViewActivity.paintScreen = paintScreen;
	}

	/**
	 * @return the markerRenderer
	 */
	public MarkerRenderer getMarkerRenderer() {
        if(markerRenderer==null){
            markerRenderer=new MarkerRenderer(MixContext.getInstance());
        }
		return markerRenderer;
	}

    /**
     * @return the markerRenderer statically - only to be used in other activities/views
     */
    public static MarkerRenderer getMarkerRendererStatically() {
        if(markerRenderer==null){
            Log.e(Config.TAG, "markerRenderer was null (called statically)");
        }
        return markerRenderer;
    }

    public void updateHud(Location curFix){
        if(settings.getBoolean(getString(R.string.pref_item_usehud_key), true)) {
            hudView.updatePositionStatus(curFix);
            hudView.setDataSourcesStatus(getMarkerRenderer().dataSourceWorking, false, null);
            hudView.setDestinationStatus(getMixViewData().getCurDestination());
        }
    }
}

