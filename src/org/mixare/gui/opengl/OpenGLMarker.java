package org.mixare.gui.opengl;

import org.mixare.lib.gui.PaintScreen;
import org.mixare.marker.POIMarker;

/**
 * Created  on 15.03.2016.
 */
public class OpenGLMarker extends POIMarker {
    public OpenGLMarker(String id, String title, double latitude, double longitude, double altitude, String URL, int type, int color) {
        super(id, title, latitude, longitude, altitude, URL, type, color);
    }

    @Override
    public void draw(PaintScreen paintScreen){

    }

    @Override
    public void drawCircle(PaintScreen paintScreen){

    }
}
