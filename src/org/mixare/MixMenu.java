package org.mixare;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

import org.mixare.data.DataSourceList;
import org.mixare.map.MixMap;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Random;

/**
 * Created by MelanieW on 30.12.2015.
 */
public class MixMenu extends SherlockActivity {

    DrawerLayout drawerLayout;
    ListView drawerList;
    ActionBarDrawerToggle drawerToggle;

    MenuListAdapter menuListAdapter;

    String[] title;
    private CharSequence drawerTitle;
    private CharSequence mTitle;
    int[] icon;
    private MixViewDataHolder mixViewData;
    private static boolean isBackground;




    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            //killOnError();
            //requestWindowFeature(Window.FEATURE_NO_TITLE);

            setContentView(R.layout.menu);

            // Get the Title
            mTitle = drawerTitle = getTitle();

            // Generate title
            title = new String[]{getString(R.string.menu_item_1), getString(R.string.menu_item_2),
                    getString(R.string.menu_item_3), getString(R.string.menu_item_4), getString(R.string.menu_item_5),
                    getString(R.string.menu_item_6), getString(R.string.menu_item_7), getString(R.string.menu_item_8)};

            // Generate icon
            icon = new int[]{R.drawable.icon_datasource,
                    R.drawable.icon_datasource,
                    android.R.drawable.ic_menu_view,
                    android.R.drawable.ic_menu_mapmode,
                    android.R.drawable.ic_menu_zoom,
                    android.R.drawable.ic_menu_search,
                    android.R.drawable.ic_menu_info_details,
                    android.R.drawable.ic_menu_share};
            //         R.drawable.collections_cloud};

            drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawerList = (ListView) findViewById(R.id.listview_drawer);

            menuListAdapter = new MenuListAdapter(MixMenu.this, title, icon);
            drawerList.setAdapter(menuListAdapter);
            drawerList.setOnItemClickListener(new DrawerItemClickListener());
            drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                    R.drawable.ic_launcher, R.string.drawer_open,
                    R.string.drawer_close) {

                public void onDrawerClosed(View view) {
                    // TODO Auto-generated method stub
                    super.onDrawerClosed(view);
                }

                public void onDrawerOpened(View drawerView) {
                    // TODO Auto-generated method stub
                    super.onDrawerOpened(drawerView);
                }
            };

            drawerLayout.setDrawerListener(drawerToggle);

        }
        catch (Exception ex) {
          //  doError(ex, GENERAL_ERROR);
        }
    }


  public boolean onKeyDown(int keyCode, KeyEvent event) {

      switch(keyCode) {
          case KeyEvent.KEYCODE_MENU:
              if (drawerLayout.isDrawerOpen(drawerList)) {
                  drawerLayout.closeDrawer(drawerList);
              } else {
                  drawerLayout.openDrawer(drawerList);
              }
              return true;
        }
          return super.onKeyDown(keyCode, event);
      }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {

            if (drawerLayout.isDrawerOpen(drawerList)) {
                drawerLayout.closeDrawer(drawerList);
            } else {
                drawerLayout.openDrawer(drawerList);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public class DrawerItemClickListener implements
            ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            selectItem(position);
        }
    }

    public void selectItem(int position) {

        switch (position) {
		/* Data sources */
            case 0:
               // if (!getMarkerRenderer().getIsLauncherStarted()) {
                    Intent intent = new Intent(MixMenu.this, DataSourceList.class);
                    startActivityForResult(intent, 40);
               // } else {
                //    markerRenderer.getContext().getNotificationManager()
                 //           .addNotification(getString(R.string.no_website_available));
               // }
                break;
			/* Plugin View */
            case 1:
               // if (!getMarkerRenderer().getIsLauncherStarted()) {
                    Intent intent2 = new Intent(MixMenu.this,
                            PluginListActivity.class);
                    startActivityForResult(intent2, 35);
                //} else {
                //    markerRenderer.getContext().getNotificationManager()
                //            .addNotification(getString(R.string.no_website_available));
                //}
                break;
		/* List markerRenderer */
            case 2:
			/*
			 * if the list of titles to show in alternative list markerRenderer is not
			 * empty
			 */
               // if (getMarkerRenderer().getDataHandler().getMarkerCount() > 0) {
                    Intent intent3 = new Intent(MixMenu.this, MixListView.class);
                    intent3.setAction(Intent.ACTION_VIEW);
                    startActivityForResult(intent3, 42);
              //  }
			/* if the list is empty */
             //   else {
              //      markerRenderer.getContext().getNotificationManager().
               //             addNotification(getString(R.string.empty_list));
                //}
                break;
		/* Map View */
            case 3:
                Intent intent4 = new Intent(MixMenu.this, MixMap.class);
                startActivityForResult(intent4, 20);
                break;
		/* range level */
            case 4:
                getMixViewData().getRangeBar().setVisibility(View.VISIBLE);
                getMixViewData().setRangeBarProgress(
                        getMixViewData().getRangeBar().getProgress());
                break;
		/* Search */
            case 5:
                onSearchRequested();
                break;
		/* GPS Information */
            case 6:
                Location currentGPSInfo = getMixViewData().getMixContext()
                        .getLocationFinder().getCurrentLocation();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.general_info_text) + "\n\n"
                        + getString(R.string.longitude)
                        + currentGPSInfo.getLongitude() + "\n"
                        + getString(R.string.latitude)
                        + currentGPSInfo.getLatitude() + "\n"
                        + getString(R.string.altitude)
                        + currentGPSInfo.getAltitude() + "m\n"
                        + getString(R.string.speed) + currentGPSInfo.getSpeed()
                        + "km/h\n" + getString(R.string.accuracy)
                        + currentGPSInfo.getAccuracy() + "m\n"
                        + getString(R.string.gps_last_fix)
                        + new Date(currentGPSInfo.getTime()).toString() + "\n");
                builder.setNegativeButton(getString(R.string.close_button),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.setTitle(getString(R.string.general_info_title));
                alert.show();
                break;
		/* Case 6: license agreements */
            case 7:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage(getString(R.string.license));
			/* Retry */
                builder1.setNegativeButton(getString(R.string.close_button),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alert1 = builder1.create();
                alert1.setTitle(getString(R.string.license_title));
                alert1.show();
                break;
            case 8:
               // doError(null, new Random().nextInt(3));
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);

    }

    public MixViewDataHolder getMixViewData() {
        return mixViewData;
    }
}