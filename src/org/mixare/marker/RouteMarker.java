package org.mixare.marker;

import org.mixare.lib.gui.PaintScreen;

/**
 * Created by MelanieW on 03.02.2016.
 */
public class RouteMarker extends POIMarker {
    public static final int MAX_OBJECTS=100;

    public RouteMarker(String id, String title, double latitude, double longitude, double altitude, String link, int type, int color) {
        super(id, title, latitude, longitude, altitude, link, type, color);
    }

    @Override
    public int getMaxObjects() {
        return MAX_OBJECTS;
    }

    @Override
    public void draw(PaintScreen paintScreen){

    }



}
