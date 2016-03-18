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
package org.mixare.map;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.FrameLayout;

import android.view.MenuItem;

import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mixare.MaterialDrawerMenuActivity;
import org.mixare.MixContext;
import org.mixare.route.AsyncResponse;
import org.mixare.Config;
import org.mixare.MixViewActivity;
import org.mixare.MixViewDataHolder;
import org.mixare.R;
import org.mixare.route.MyRoute;
import org.mixare.route.RouteDataAsyncTask;
import org.mixare.lib.MixUtils;
import org.mixare.lib.marker.Marker;
import org.mixare.marker.LocalMarker;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends MaterialDrawerMenuActivity {
    public final static byte ZOOM_LEVEL_DEFAULT = 12;
    public final static byte ZOOM_LEVEL_CLOSE = 18;

    private MapView mapView;
	private TileCache tileCache;
    protected TileDownloadLayer downloadLayer;
    private FrameLayout contentFrame;
    List<LatLong> routeResult = new ArrayList<>();
    Polyline polyline;
    List<LatLong> coordinateList;
    LatLong target;

    // the search keyword
    protected String searchKeyword = "";

    protected float screenRatio = 1.0f;


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Location curLocation = getMixViewData().getCurLocation();
        Location curDestination = getMixViewData().getCurDestination();
        if (curDestination != null) {
            target = new LatLong(curDestination.getLatitude(), curDestination.getLongitude());
        }

        AndroidGraphicFactory.createInstance(this.getApplication());
        contentFrame = (FrameLayout)findViewById(R.id.drawermenu_content_framelayout);

        this.mapView = new MapView(this);
        //setContentView(this.mapView);

        this.mapView.setClickable(true);
        this.mapView.getMapScaleBar().setVisible(true);
        this.mapView.setBuiltInZoomControls(true);

        this.tileCache=AndroidUtil.createTileCache(this, this.getClass().getSimpleName(),
                this.mapView.getModel().displayModel.getTileSize(), this.screenRatio,
                this.mapView.getModel().frameBufferModel.getOverdrawFactor(), false);

        this.downloadLayer = new TileDownloadLayer(this.tileCache,
                this.mapView.getModel().mapViewPosition, OpenStreetMapMapnik.INSTANCE,
                AndroidGraphicFactory.INSTANCE);
        mapView.getLayerManager().getLayers().add(this.downloadLayer);

        mapView.getModel().mapViewPosition.setZoomLevelMin(OpenStreetMapMapnik.INSTANCE.getZoomLevelMin());
        mapView.getModel().mapViewPosition.setZoomLevelMax(OpenStreetMapMapnik.INSTANCE.getZoomLevelMax());
        mapView.getMapZoomControls().setZoomLevelMin(OpenStreetMapMapnik.INSTANCE.getZoomLevelMin());
        mapView.getMapZoomControls().setZoomLevelMax(OpenStreetMapMapnik.INSTANCE.getZoomLevelMax());

        // Add mapView to View
        //setContentView(mapView);
        //contentFrame.removeAllViews();
        contentFrame.addView(this.mapView);
        if(getActionBar()!=null){
           getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Retrieve the search query
        Intent intent = this.getIntent();
        searchKeyword = intent.getStringExtra(Config.INTENT_EXTRA_SEARCH_QUERY);

        // Set center of the Map to your position or a Position out of the
        // IntentExtras
        if (intent.getBooleanExtra(Config.INTENT_EXTRA_DO_CENTER, false)) {
            setCenterZoom(intent.getDoubleExtra(Config.INTENT_EXTRA_LATITUDE, Config.DEFAULT_FIX_LAT),
                    intent.getDoubleExtra(Config.INTENT_EXTRA_LONGITUDE, Config.DEFAULT_FIX_LON), ZOOM_LEVEL_CLOSE);
        } else {
            setOwnLocationToCenter();
            setZoomLevelBasedOnRadius();
        }
        paintRoute(curLocation,curDestination);
    }


    /**
     * Creates the Overlay and adds the markers
     */
    private void createOverlay() {
        // create a default marker for the overlay
        Drawable markerLink = getResources().getDrawable(
                R.drawable.icon_map_link);
        markerLink.setBounds(-markerLink.getIntrinsicWidth() / 2,
                -markerLink.getIntrinsicHeight(),
                markerLink.getIntrinsicWidth() / 2, 0);

        // Create marker if no link is specified
        Drawable markerNoLink = this.getResources().getDrawable(
                R.drawable.icon_map_nolink);
        markerNoLink.setBounds(-markerNoLink.getIntrinsicWidth() / 2,
                -markerNoLink.getIntrinsicHeight(),
                markerNoLink.getIntrinsicWidth() / 2, 0);
        // a marker to show at the position
       Marker marker;
        int limit = MixViewActivity.getMarkerRendererStatically().getDataHandler().getMarkerCount();

        for (int i = 0; i < limit; i++) {
            Drawable icon=markerLink;
            marker = MixViewActivity.getMarkerRendererStatically().getDataHandler().getMarker(i);
            // if a searchKeyword is specified
            if (searchKeyword != null) {
                // the Keyword is not Empty
                if (!searchKeyword.isEmpty()) {
                    // the title of the Marker contains the searchKeyword
                    if (!marker.getTitle().toLowerCase().contains(searchKeyword.toLowerCase().trim())) {
                        marker = null;
                        continue;
                    }
                }
            }
            // reaches this part of code if no keyword is specified, the keyword
            // is empty or does match


            // If no URL is specified change the icon
            if (marker.getURL() == null || marker.getURL().isEmpty()) {
                icon=markerNoLink;
            }

            MixMapMarker mapMarker = new MixMapMarker(marker, icon);

            this.mapView.getLayerManager().getLayers().add(mapMarker);
        }
    }

    class MixMapMarker extends org.mapsforge.map.layer.overlay.Marker  {
        Marker marker;
        /**
         * @param latLong          the initial geographical coordinates of this marker (may be null).
         * @param bitmap           the initial {@code Bitmap} of this marker (may be null).
         * @param horizontalOffset the horizontal marker offset.
         * @param verticalOffset
         */
        public MixMapMarker(LatLong latLong, Bitmap bitmap, int horizontalOffset, int verticalOffset) {
            super(latLong, bitmap, horizontalOffset, verticalOffset);
        }

        public MixMapMarker(Marker marker, Drawable icon) {
            this(new LatLong(marker.getLatitude(),
                    marker.getLongitude()), AndroidGraphicFactory.convertToBitmap(icon), 0, -icon.getIntrinsicHeight() / 2);
            this.marker=marker;
            // Creates a new GeoPoint of the markers Location
            // Creates a new OverlayItem with the markers Location, the Title
            // and the Url
        }

        @Override
        public boolean onTap(LatLong tapLatLong, Point layerXY, Point tapXY){
            if(marker instanceof LocalMarker){
                LocalMarker localMarker = (LocalMarker) marker;
                localMarker.retrieveActionPopupMenu(MapActivity.this,mapView).show();
            }
            return true;
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        createOverlay();
    }

    @Override
    public void onPause() {
        this.downloadLayer.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        this.downloadLayer.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mapView.destroyAll();
    }

    /**
     * Sets the center of the map to the specified point
     *
     * @param lat
     *            The latitude of the point
     * @param lng
     *            The longitude of the point
     */
    private void setCenter(double lat, double lng) {
        this.mapView.getModel().mapViewPosition.setCenter(new LatLong(lat, lng));
    }

    /**
     * Sets the center of the map to the specified point with the specified zoomLevel
     * level
     *
     * @param zoomLevel
     *            The zoomLevel level
     */
    private void setZoomLevel(int zoomLevel) {
        this.mapView.setZoomLevel((byte) zoomLevel);
    }

    /**
     * Sets the center of the map to the specified point with the specified zoomLevel
     * level
     *
     * @param lat
     *            The latitude of the point
     * @param lng
     *            The longitude of the point
     * @param zoomLevel
     *            The zoomLevel level
     */
    private void setCenterZoom(double lat, double lng, int zoomLevel) {
        setZoomLevel(zoomLevel);
        setCenter(lat, lng);
    }



    /**
     * Sets the Zoomlevel of the Map based on the Radius using
     *
     */
    private void setZoomLevelBasedOnRadius() {
        float mapZoomLevel = (MixViewActivity.getMarkerRendererStatically().getRadius() / 2f);
        mapZoomLevel = MixUtils
                .earthEquatorToZoomLevel((mapZoomLevel < 2f) ? 2f
                        : mapZoomLevel);

        setZoomLevel((int) mapZoomLevel);

    }

    	/* Getter and Setter */

    /**
     * Returns the Point of the current Own Location
     *
     * @return My current Location
     */
    private Location getOwnLocation() {
        return MixViewDataHolder.getInstance().getCurLocation();
    }

    /**
     * Receives the Location and sets the MapCenter to your position
     */
    private void setOwnLocationToCenter() {
        Location location = getOwnLocation();
        setCenter(location.getLatitude(), location.getLongitude());
    }

    public void paintRoute(Location routeStart, Location routeEnd) {
        Paint paint = AndroidGraphicFactory.INSTANCE.createPaint();
        paint.setColor(Color.parseColor(MixContext.getInstance().getSettings().getString(getString(R.string.pref_item_routecolor_key),getString(R.string.color_hint))));
        paint.setStrokeWidth(6);
        paint.setStyle(Style.STROKE);

        polyline = new Polyline(paint, AndroidGraphicFactory.INSTANCE);
        coordinateList = polyline.getLatLongs();

        RouteDataAsyncTask asyncTask = (RouteDataAsyncTask) new RouteDataAsyncTask(new AsyncResponse() {
            @Override
            public void processFinish(MyRoute route) {
                coordinateList.addAll(route.getCoordinateList());
                mapView.getLayerManager().getLayers().add(polyline);
            }

        }).execute(routeStart,routeEnd);
    }
}