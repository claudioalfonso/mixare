package org.mixare;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //killOnError();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getSupportActionBar() != null) {
        	getSupportActionBar().hide();
        }

        setContentView(R.layout.menu);

        // Get the Title
        mTitle = drawerTitle = getTitle();

        // Generate title
        title = new String[]{getString(R.string.menu_item_1), getString(R.string.menu_item_2),
                getString(R.string.menu_item_3),getString(R.string.menu_item_4),getString(R.string.menu_item_5),
                getString(R.string.menu_item_6),getString(R.string.menu_item_7),getString(R.string.menu_item_8)};

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
}