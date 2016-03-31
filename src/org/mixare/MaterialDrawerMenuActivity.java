package org.mixare;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.crossfader.Crossfader;
import com.mikepenz.crossfader.util.UIUtils;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.MiniDrawer;
import com.mikepenz.materialdrawer.interfaces.ICrossfader;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.mixare.map.MapActivity;
import org.mixare.settings.SettingsActivity;

public class MaterialDrawerMenuActivity extends Activity {
    private CharSequence mTitle;
    private Drawer materialDrawer;

    private MiniDrawer miniDrawer = null;
    private Crossfader crossFader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.drawermenu_screen);

            mTitle = getTitle();

            materialDrawer = new DrawerBuilder()
                    .withGenerateMiniDrawer(true)
                    .withActivity(this)
                    .inflateMenu(R.menu.drawer_menu)
                    .withSavedInstance(savedInstanceState)
                    .withOnDrawerItemClickListener(new DrawerItemClickListener())
                    .buildView(); // build only the view of the Drawer (don't inflate it automatically with .build())
//                    .build();

            //move settings item to sticky footer
            IDrawerItem settingsItem = materialDrawer.getDrawerItem(R.id.menuitem_settings);
            materialDrawer.removeItems(R.id.menuitem_settings);
            materialDrawer.addStickyFooterItem(settingsItem);

            miniDrawer = materialDrawer.getMiniDrawer(); //get miniDrawer managed by the materialDrawer and hook it into Crossfader




            //get the widths in px for the first and second panel
            int firstWidth = (int) UIUtils.convertDpToPixel(300, this);
            int secondWidth = (int) UIUtils.convertDpToPixel(72, this);

            //create and build crossfader (MiniDrawer is also built here, as the build method returns the view to be used in the crossfader)
            crossFader = new Crossfader()
                    .withContent(findViewById(R.id.drawermenu_content_camerascreen))
                    .withFirst(materialDrawer.getSlider(), firstWidth)
                    .withSecond(miniDrawer.build(this), secondWidth)
                    .withSavedInstance(savedInstanceState)
                    .build();

            //define the crossfader to be used with the miniDrawer to automatically toggle open / close
            miniDrawer.withCrossFader(new CrossfadeWrapper(crossFader));


        } catch (Exception ex) {
            Log.d(Config.TAG, "MaterialDrawerMenuActivity onCreate", ex);
            // doError(ex, GENERAL_ERROR);
        }
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                if (materialDrawer.isDrawerOpen()) {
                    materialDrawer.closeDrawer();
                } else {
                    materialDrawer.openDrawer();
                }
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d(Config.TAG, "MaterialDrawerMenuActivity onOptionsItemSelected home selected");
        }
        return super.onOptionsItemSelected(item);
    }

    public void selectItem(int position, IDrawerItem drawerItem) {
        Class<? extends Activity> activityClass = null;
        int requestCode = 0;
        Intent intent;

        switch ((int) drawerItem.getIdentifier()) {
            // Marker List View
            case R.id.menuitem_route: //fall-through intended
            case R.id.menuitem_markerlist:
                activityClass = MarkerListActivity.class;
                requestCode = Config.INTENT_REQUEST_CODE_MARKERLIST;
                break;
            // Map View
            case R.id.menuitem_map:
                activityClass = MapActivity.class;
                requestCode = Config.INTENT_REQUEST_CODE_MAP;
                break;
            // Search
            case R.id.menuitem_search:
                onSearchRequested();
                break;
            case R.id.menuitem_settings:
                activityClass = SettingsActivity.class;
                requestCode = Config.INTENT_REQUEST_CODE_SETTINGS;
                break;
            default:
                break;
        }

        if (activityClass != null) {
            intent = new Intent(MaterialDrawerMenuActivity.this, activityClass);
            startActivityForResult(intent, requestCode);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        if (getActionBar() != null) {
            getActionBar().setTitle(mTitle);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //add the values which need to be saved from the materialDrawer to the bundle
        outState = materialDrawer.saveInstanceState(outState);
        //add the values which need to be saved from the crossFader to the bundle
        outState = crossFader.saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        //close the materialDrawer first and if the materialDrawer is closed close the activity
        if (materialDrawer != null && materialDrawer.isDrawerOpen()) {
            materialDrawer.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    public MixViewDataHolder getMixViewDataHolder() {
        return MixViewDataHolder.getInstance();
    }

    public class DrawerItemClickListener implements Drawer.OnDrawerItemClickListener {
        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            selectItem(position, drawerItem);
            return false;
        }
    }

    public class CrossfadeWrapper implements ICrossfader {
        private Crossfader mCrossfader;

        public CrossfadeWrapper(Crossfader crossfader) {
            this.mCrossfader = crossfader;
        }

        @Override
        public void crossfade() {
            mCrossfader.crossFade();
        }

        @Override
        public boolean isCrossfaded() {
            return mCrossfader.isCrossFaded();
        }
    }
}