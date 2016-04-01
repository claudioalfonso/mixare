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

import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.mixare.data.DataSourceList;
import org.mixare.data.DataSourceStorage;
import org.mixare.gui.HudView;
import org.mixare.gui.LicensePreference;
import org.mixare.gui.opengl.OpenGLAugmentationView;
import org.mixare.gui.opengl.OpenGLMarker;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.render.Matrix;
import org.mixare.mgr.HttpTools;
import org.mixare.route.RouteManager;

import static android.hardware.SensorManager.SENSOR_DELAY_GAME;


/**
 * This class is the main application which uses the other classes for different
 * functionalities.
 * It sets up the camera screen and the augmented screen which is in front of the
 * camera screen.
 * It also handles the main sensor events, touch events and location events.
 */
public class MixViewActivity extends MaterialDrawerMenuActivity implements SensorEventListener, OnTouchListener {

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

			getMixViewDataHolder().setSensorMgr((SensorManager) getSystemService(SENSOR_SERVICE));

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
			if (MixContext.getInstance().getSettings().getBoolean(getString(R.string.pref_item_firstacess_key), true)) {
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
	public MixViewDataHolder getMixViewDataHolder() {
		MixContext.setActualMixViewActivity(this);
		return super.getMixViewDataHolder();
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
				getMixViewDataHolder().getSensorMgr().unregisterListener(this,
						getMixViewDataHolder().getSensorGrav());
				getMixViewDataHolder().getSensorMgr().unregisterListener(this,
						getMixViewDataHolder().getSensorMag());
				getMixViewDataHolder().getSensorMgr().unregisterListener(this,
						getMixViewDataHolder().getSensorGyro());
				getMixViewDataHolder().getSensorMgr().unregisterListener(this);
				getMixViewDataHolder().setSensorGrav(null);
				getMixViewDataHolder().setSensorMag(null);
				getMixViewDataHolder().setSensorGyro(null);

				MixContext.getInstance().getLocationFinder().switchOff();
				MixContext.getInstance().getDownloadManager().switchOff();

				MixContext.getInstance().getNotificationManager().setEnabled(false);
				MixContext.getInstance().getNotificationManager().clear();
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
			getMixViewDataHolder().getM1().set(1f, 0f, 0f, 0f,
					(float) Math.cos(angleX),
					(float) -Math.sin(angleX), 0f,
					(float) Math.sin(angleX),
					(float) Math.cos(angleX));
			angleX = (float) Math.toRadians(marker_orientation);
			angleY = (float) Math.toRadians(marker_orientation);
			if (rotation == 1) {
				getMixViewDataHolder().getM2().set(1f, 0f, 0f, 0f,
						(float) Math.cos(angleX),
						(float) -Math.sin(angleX), 0f,
						(float) Math.sin(angleX),
						(float) Math.cos(angleX));
				getMixViewDataHolder().getM3().set((float) Math.cos(angleY), 0f,
						(float) Math.sin(angleY), 0f, 1f, 0f,
						(float) -Math.sin(angleY), 0f,
						(float) Math.cos(angleY));
			} else {
				getMixViewDataHolder().getM2().set((float) Math.cos(angleX), 0f,
						(float) Math.sin(angleX), 0f, 1f, 0f,
						(float) -Math.sin(angleX), 0f,
						(float) Math.cos(angleX));
				getMixViewDataHolder().getM3().set(1f, 0f, 0f, 0f,
						(float) Math.cos(angleY),
						(float) -Math.sin(angleY), 0f,
						(float) Math.sin(angleY),
						(float) Math.cos(angleY));

			}

			getMixViewDataHolder().getM4().toIdentity();

			for (int i = 0; i < getMixViewDataHolder().getHistR().length; i++) {
				getMixViewDataHolder().getHistR()[i] = new Matrix();
			}

			getMixViewDataHolder().addListSensors(getMixViewDataHolder().getSensorMgr().getSensorList(
					Sensor.TYPE_ACCELEROMETER));
			if (getMixViewDataHolder().getSensor(0).getType() == Sensor.TYPE_ACCELEROMETER ) {
				getMixViewDataHolder().setSensorGrav(getMixViewDataHolder().getSensor(0));
			}//else report error (unsupported hardware)

			getMixViewDataHolder().addListSensors(getMixViewDataHolder().getSensorMgr().getSensorList(
					Sensor.TYPE_MAGNETIC_FIELD));
			if (getMixViewDataHolder().getSensor(1).getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				getMixViewDataHolder().setSensorMag(getMixViewDataHolder().getSensor(1));
			}//else report error (unsupported hardware)

			if (!getMixViewDataHolder().getSensorMgr().getSensorList(Sensor.TYPE_GYROSCOPE).isEmpty()){
				getMixViewDataHolder().addListSensors(getMixViewDataHolder().getSensorMgr().getSensorList(
						Sensor.TYPE_GYROSCOPE));
				if (getMixViewDataHolder().getSensor(2).getType() == Sensor.TYPE_GYROSCOPE) {
					getMixViewDataHolder().setSensorGyro(getMixViewDataHolder().getSensor(2));
				}
				getMixViewDataHolder().getSensorMgr().registerListener(this,
						getMixViewDataHolder().getSensorGyro(), SENSOR_DELAY_GAME);
			}

				getMixViewDataHolder().getSensorMgr().registerListener(this,
						getMixViewDataHolder().getSensorGrav(), SENSOR_DELAY_GAME);
				getMixViewDataHolder().getSensorMgr().registerListener(this,
						getMixViewDataHolder().getSensorMag(), SENSOR_DELAY_GAME);

			try {
				GeomagneticField gmf = MixContext.getInstance().getLocationFinder().getGeomagneticField();
				angleY = (float) Math.toRadians(-gmf.getDeclination());
				getMixViewDataHolder().getM4().set((float) Math.cos(angleY), 0f,
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
			//MixContext.getInstance().getLocationFinder().switchOn();
			MixContext.getInstance().startLocationManager();

		} catch (Exception ex) {
            doError(ex, GENERAL_ERROR);
			try {
				if (getMixViewDataHolder().getSensorMgr() != null) {
					getMixViewDataHolder().getSensorMgr().unregisterListener(this,
							getMixViewDataHolder().getSensorGrav());
					getMixViewDataHolder().getSensorMgr().unregisterListener(this,
							getMixViewDataHolder().getSensorMag());
					getMixViewDataHolder().getSensorMgr().unregisterListener(this,
							getMixViewDataHolder().getSensorGyro());
					getMixViewDataHolder().setSensorMgr(null);
				}

				if (MixContext.getInstance() != null) {
					MixContext.getInstance().getLocationFinder().switchOff();
					MixContext.getInstance().getDownloadManager().switchOff();
				}
			} catch (Exception ignore) {
			}
		} finally {
			//This does not conflict with registered sensors (sensorMag, sensorGrav)
			//This is a place holder to API returned listed of sensors, we registered
			//what we need, the rest is unnecessary.
			getMixViewDataHolder().clearAllSensors();
		}

		Log.d(Config.TAG, "resume");
		if (getMarkerRenderer() == null) {
			return;
		}
		if (getMarkerRenderer().isFrozen()
				&& getMixViewDataHolder().getSearchNotificationTxt() == null) {
			getMixViewDataHolder().setSearchNotificationTxt(new TextView(this));
			getMixViewDataHolder().getSearchNotificationTxt().setWidth(
					getPaintScreen().getWidth());
			getMixViewDataHolder().getSearchNotificationTxt().setPadding(10, 2, 0, 0);
			getMixViewDataHolder().getSearchNotificationTxt().setText(
					getString(R.string.search_active_1) + " "
							+ DataSourceList.getDataSourcesStringList()
							+ getString(R.string.search_active_2));
			;
			getMixViewDataHolder().getSearchNotificationTxt().setBackgroundColor(
					Color.DKGRAY);
			getMixViewDataHolder().getSearchNotificationTxt().setTextColor(
					Color.WHITE);

			getMixViewDataHolder().getSearchNotificationTxt()
					.setOnTouchListener(this);
			addContentView(getMixViewDataHolder().getSearchNotificationTxt(),
					new LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT));
		} else if (!getMarkerRenderer().isFrozen()
				&& getMixViewDataHolder().getSearchNotificationTxt() != null) {
			getMixViewDataHolder().getSearchNotificationTxt()
					.setVisibility(View.GONE);
			getMixViewDataHolder().setSearchNotificationTxt(null);
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
			getMixViewDataHolder().getSensorMgr().unregisterListener(this);
			getMixViewDataHolder().setSensorMgr(null);
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
		if (MixContext.getInstance().getSettings().getBoolean(getString(R.string.pref_item_usehud_key), true)) {
			maintainHudView();
		}
        if (MixContext.getInstance().getSettings().getBoolean(getString(R.string.pref_item_routing_key), true)){
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
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP && MixContext.getInstance().getSettings().getBoolean(getString(R.string.pref_item_camera2_key), false)) {
					Log.d(Config.TAG,"Camera2");
					cameraSurface = new Camera2Surface(this);
                } else {
					Log.d(Config.TAG,"legacy camera");
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
//			if (getMixViewDataHolder().getDownloadThread() != null){
//				if (!getMixViewDataHolder().getDownloadThread().isInterrupted()){
//					getMixViewDataHolder().getDownloadThread().interrupt();
//					MixContext.getInstance().getDownloadManager().restart();
//				}
//			}else { //if no download thread found
//				getMixViewDataHolder().setDownloadThread(new Thread(getMixViewDataHolder()
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
		SharedPreferences.Editor editor = MixContext.getInstance().getSettings().edit();

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

	@Override
	public void selectItem(int position, IDrawerItem drawerItem) {
		switch ((int) drawerItem.getIdentifier()) {
			case R.id.menuitem_range:
                hudView.showRangeBar();
                //drawerLayout.closeDrawer(drawerList);
                break;
			case R.id.menuitem_route:
                new MarkerListFragment().show(getFragmentManager(), "TAG");
				break;
			default:
				super.selectItem(position,drawerItem);
				break;
		}
	}

    public void update3D(){
        Location startLocation = MixContext.getInstance().getCurLocation();
        Location endLocation = MixContext.getInstance().getCurDestination();

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
			if (getMixViewDataHolder().getSensorGyro() != null) {
				
				if (evt.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
					getMixViewDataHolder().setGyro(evt.values);
				}
				
				if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
					getMixViewDataHolder().setGrav(
							getMixViewDataHolder().getGravFilter().lowPassFilter(evt.values,
									getMixViewDataHolder().getGrav()));
				} else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
					getMixViewDataHolder().setMag(
							getMixViewDataHolder().getMagFilter().lowPassFilter(evt.values,
									getMixViewDataHolder().getMag()));
				}
				getMixViewDataHolder().setAngle(
						getMixViewDataHolder().getMagFilter().complementaryFilter(
								getMixViewDataHolder().getGrav(),
								getMixViewDataHolder().getGyro(), 30,
								getMixViewDataHolder().getAngle()));
				
				SensorManager.getRotationMatrix(
						getMixViewDataHolder().getRTmp(),
						getMixViewDataHolder().getI(),
						getMixViewDataHolder().getGrav(),
						getMixViewDataHolder().getMag());
			} else {
				if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
					getMixViewDataHolder().setGrav(evt.values);
				} else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
					getMixViewDataHolder().setMag(evt.values);
				}
				SensorManager.getRotationMatrix(
						getMixViewDataHolder().getRTmp(),
						getMixViewDataHolder().getI(),
						getMixViewDataHolder().getGrav(),
						getMixViewDataHolder().getMag());
			}
			
			simpleAugmentationView.postInvalidate();
			hudView.postInvalidate();

			int rotation = Compatibility.getRotation(this);

			if (rotation == 1) {
				SensorManager.remapCoordinateSystem(getMixViewDataHolder().getRTmp(),
						SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z,
						getMixViewDataHolder().getRot());
			} else {
				SensorManager.remapCoordinateSystem(getMixViewDataHolder().getRTmp(),
						SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z,
						getMixViewDataHolder().getRot());
			}
			getMixViewDataHolder().getTempR().set(getMixViewDataHolder().getRot()[0],
					getMixViewDataHolder().getRot()[1], getMixViewDataHolder().getRot()[2],
					getMixViewDataHolder().getRot()[3], getMixViewDataHolder().getRot()[4],
					getMixViewDataHolder().getRot()[5], getMixViewDataHolder().getRot()[6],
					getMixViewDataHolder().getRot()[7], getMixViewDataHolder().getRot()[8]);

			getMixViewDataHolder().getFinalR().toIdentity();
			getMixViewDataHolder().getFinalR().prod(getMixViewDataHolder().getM4());
			getMixViewDataHolder().getFinalR().prod(getMixViewDataHolder().getM1());
			getMixViewDataHolder().getFinalR().prod(getMixViewDataHolder().getTempR());
			getMixViewDataHolder().getFinalR().prod(getMixViewDataHolder().getM3());
			getMixViewDataHolder().getFinalR().prod(getMixViewDataHolder().getM2());
			getMixViewDataHolder().getFinalR().invert();
			
			getMixViewDataHolder().getHistR()[getMixViewDataHolder().getrHistIdx()]
					.set(getMixViewDataHolder().getFinalR());
			
			int histRLenght = getMixViewDataHolder().getHistR().length;
			
			getMixViewDataHolder().setrHistIdx(getMixViewDataHolder().getrHistIdx() + 1);
			if (getMixViewDataHolder().getrHistIdx() >= histRLenght)
				getMixViewDataHolder().setrHistIdx(0);

			getMixViewDataHolder().getSmoothR().set(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f,
					0f);
			for (int i = 0; i < histRLenght; i++) {
				getMixViewDataHolder().getSmoothR().add(
						getMixViewDataHolder().getHistR()[i]);
			}
			getMixViewDataHolder().getSmoothR().mult(
					1 / (float) histRLenght);

			MixContext.getInstance().updateSmoothRotation(
					getMixViewDataHolder().getSmoothR());
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
				&& getMixViewDataHolder().getCompassErrorDisplayed() == 0) {
			for (int i = 0; i < 2; i++) {
				markerRenderer.getContext().getNotificationManager().
				addNotification(getString(R.string.compass_unreliable));
			}
			getMixViewDataHolder().setCompassErrorDisplayed(
					getMixViewDataHolder().getCompassErrorDisplayed() + 1);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		getMarkerRenderer().setFrozen(false);
		if (getMixViewDataHolder().getSearchNotificationTxt() != null) {
			getMixViewDataHolder().getSearchNotificationTxt()
					.setVisibility(View.GONE);
			getMixViewDataHolder().setSearchNotificationTxt(null);
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
        if(MixContext.getInstance().getSettings().getBoolean(getString(R.string.pref_item_usehud_key), true)) {
            hudView.updatePositionStatus(curFix);
            hudView.setDataSourcesStatus(getMarkerRenderer().dataSourceWorking, false, null);
            hudView.setDestinationStatus(MixContext.getInstance().getCurDestination());
        }
    }
}

