package org.mixare;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.mixare.map.MapActivity;
import org.mixare.settings.SettingsActivity;

public class MaterialDrawerMenuActivity extends Activity {
    private CharSequence mTitle;
    private Drawer materialDrawer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.drawermenu_screen);

            mTitle = getTitle();

            materialDrawer = new DrawerBuilder()
                    .withActivity(this)
                    .inflateMenu(R.menu.drawer_menu)
                    .withOnDrawerItemClickListener(new DrawerItemClickListener())
                    .build();

            //move settings item to sticky footer
            IDrawerItem settingsItem = materialDrawer.getDrawerItem(R.id.menuitem_settings);
            materialDrawer.removeItems(R.id.menuitem_settings);
            materialDrawer.addStickyFooterItem(settingsItem);
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

    public MixViewDataHolder getMixViewData() {
        return MixViewDataHolder.getInstance();
    }

    public class DrawerItemClickListener implements Drawer.OnDrawerItemClickListener {
        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            selectItem(position, drawerItem);
            return false;
        }
    }
}