package org.mixare.map;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.model.DisplayModel;
import org.mapsforge.map.model.MapViewPosition;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mixare.MixContext;

/**
 * Created by MelanieW on 03.04.2016.
 */
public class MyLocationOverlay extends Layer implements LocationListener {

    private final MapViewPosition mapViewPosition;
    private final Circle circle;
    private boolean myLocationEnabled = true;



    private static final GraphicFactory GRAPHIC_FACTORY = AndroidGraphicFactory.INSTANCE;


    private static Paint getDefaultCircleFill() {
        return getPaint(GRAPHIC_FACTORY.createColor(48, 0, 0, 255), 0, Style.FILL);
    }

    private static Paint getDefaultCircleStroke() {
        return getPaint(GRAPHIC_FACTORY.createColor(160, 0, 0, 255), 2, Style.STROKE);
    }

    private static Paint getPaint(int color, int strokeWidth, Style style) {
        Paint paint = GRAPHIC_FACTORY.createPaint();
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(style);
        return paint;
    }

    public synchronized boolean isMyLocationEnabled() {
        return this.myLocationEnabled;
    }

    public MyLocationOverlay(Context context, MapViewPosition mapViewPosition, DisplayModel displayModel) {


        this.mapViewPosition = mapViewPosition;
        this.circle = new Circle(null, 5, getDefaultCircleFill(), getDefaultCircleStroke());
        this.circle.setLatLong(new LatLong(MixContext.getInstance().getCurLocation().getLatitude(), MixContext.getInstance().getCurLocation().getLongitude()));

        this.circle.setDisplayModel(displayModel);

       // requestRedraw();
    }

    @Override
    public void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {

        if (!this.myLocationEnabled) {
            return;
        }

        this.circle.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
    }

    @Override
    public void onLocationChanged(Location location) {

        this.circle.setLatLong(new LatLong(location.getLatitude(),location.getLongitude()));

        requestRedraw();


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {


    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
