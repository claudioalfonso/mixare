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

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.layer.cache.TileCache;

import org.mapsforge.map.layer.download.TileDownloadLayer;
import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik;
import org.mixare.MixView;
import org.mixare.R;
import org.mixare.lib.MixUtils;

import org.mixare.lib.marker.Marker;
import org.mixare.mgr.location.LocationFinder;

public class MixMap extends Activity {
	private MapView mapView;
	private TileCache tileCache;
    protected TileDownloadLayer downloadLayer;

    // the search keyword
    protected String searchKeyword = "";

    protected float screenRatio = 1.0f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidGraphicFactory.createInstance(this.getApplication());

        this.mapView = new MapView(this);
        setContentView(this.mapView);

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
        setContentView(mapView);

        // Retrieve the search query
        Intent intent = this.getIntent();
        searchKeyword = intent.getStringExtra("search");

        // Set center of the Map to your position or a Position out of the
        // IntentExtras
        if (intent.getBooleanExtra("center", false)) {
            setCenterZoom(intent.getDoubleExtra("latitude", LocationFinder.default_lat),
                    intent.getDoubleExtra("longitude", LocationFinder.default_lon), LocationFinder.default_zoom);
        } else {
            setOwnLocationToCenter();
            setZoomLevelBasedOnRadius();
        }
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
        int limit = MixView.getDataView().getDataHandler().getMarkerCount();

        for (int i = 0; i < limit; i++) {
            Drawable icon=markerLink;
            marker = MixView.getDataView().getDataHandler().getMarker(i);
            // if a searchKeyword is specified
            if (searchKeyword != null) {
                // the Keyword is not Empty
                if (!searchKeyword.isEmpty()) {
                    // the title of the Marker contains the searchKeyword
                    if (marker.getTitle().toLowerCase()
                            .indexOf(searchKeyword.toLowerCase().trim()) == -1) {
                        marker = null;
                        continue;
                    }
                }
            }
            // reaches this part of code if no keyword is specified, the keyword
            // is empty or does match

            // Creates a new GeoPoint of the markers Location
            final LatLong point = new LatLong(marker.getLatitude(),
                    marker.getLongitude());
            // Creates a new OverlayItem with the markers Location, the Title
            // and the Url

            // If no URL is specified change the icon
            if (marker.getURL() == null || marker.getURL().isEmpty()) {
                icon=markerNoLink;
            }

            this.mapView.getLayerManager().getLayers().add(new org.mapsforge.map.layer.overlay.Marker(point, AndroidGraphicFactory.convertToBitmap(icon), 0, -icon.getIntrinsicHeight() / 2));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        setCenterZoom(LocationFinder.default_lat,LocationFinder.default_lon,LocationFinder.default_zoom);
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
     * Sets the center of the map to the specified point with the specified zoom
     * level
     *
     * @param zoom
     *            The zoom level
     */
    private void setZoom(int zoom) {
        this.mapView.getModel().mapViewPosition.setZoomLevel((byte) zoom);
    }

    /**
     * Sets the center of the map to the specified point with the specified zoom
     * level
     *
     * @param lat
     *            The latitude of the point
     * @param lng
     *            The longitude of the point
     * @param zoom
     *            The zoom level
     */
    private void setCenterZoom(double lat, double lng, int zoom) {
        setZoom(zoom);
        setCenter(lat, lng);
    }



    /**
     * Sets the Zoomlevel of the Map based on the Radius using
     *
     */
    private void setZoomLevelBasedOnRadius() {
        float mapZoomLevel = (MixView.getDataView().getRadius() / 2f);
        mapZoomLevel = MixUtils
                .earthEquatorToZoomLevel((mapZoomLevel < 2f) ? 2f
                        : mapZoomLevel);
        setZoom((int) mapZoomLevel);

    }

    	/* Getter and Setter */

    /**
     * Returns the Point of the current Own Location
     *
     * @return My current Location
     */
    private Location getOwnLocation() {
        return MixView.getDataView().getContext().getLocationFinder()
                .getCurrentLocation();
    }

    /**
     * Receives the Location and sets the MapCenter to your position
     */
    private void setOwnLocationToCenter() {
        Location location = getOwnLocation();
        setCenter(location.getLatitude(), location.getLongitude());
    }


}