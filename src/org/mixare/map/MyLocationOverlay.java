package org.mixare.map;

import android.content.Context;
import android.graphics.Color;
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
import org.mixare.R;

/**
 * Overlay for drawing a GPS position point on the map
 *
 * Created by MelanieW on 03.04.2016.
 */
public class MyLocationOverlay extends Layer implements LocationListener {

    private final MapViewPosition mapViewPosition;
    private final Circle circle;
    private final Circle innerCircle;
    private boolean myLocationEnabled = true;
    private static final int alpha=Color.argb(128, 0, 0, 0);




    private static final GraphicFactory GRAPHIC_FACTORY = AndroidGraphicFactory.INSTANCE;


    private static Paint getPaint(int color, int strokeWidth, Style style, Context context) {
        Paint paint = GRAPHIC_FACTORY.createPaint();
        paint.setColor(0x00ffffff & Color.parseColor(MixContext.getInstance().getSettings().getString(context.getString(R.string.pref_item_routecolor_key),context.getString(R.string.color_hint)))|alpha);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(style);
        return paint;
    }

    public MyLocationOverlay(Context context, MapViewPosition mapViewPosition, DisplayModel displayModel) {


        this.mapViewPosition = mapViewPosition;
        this.circle = new Circle(null, 4, getPaint(GRAPHIC_FACTORY.createColor(48, 0, 0, 255), 0, Style.FILL, context), getPaint(GRAPHIC_FACTORY.createColor(160, 0, 0, 255), 8, Style.STROKE, context));
        this.circle.setLatLong(new LatLong(MixContext.getInstance().getCurLocation().getLatitude(), MixContext.getInstance().getCurLocation().getLongitude()));



        this.innerCircle = new Circle(null, 2f, null, getPaint(GRAPHIC_FACTORY.createColor(160, 0, 0, 255), 2, Style.FILL, context));
        this.innerCircle.setLatLong(new LatLong(MixContext.getInstance().getCurLocation().getLatitude(), MixContext.getInstance().getCurLocation().getLongitude()));


        this.circle.setDisplayModel(displayModel);
        this.innerCircle.setDisplayModel(displayModel);


    }

    @Override
    public void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {

        if (!this.myLocationEnabled) {
            return;
        }

        this.circle.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
        this.innerCircle.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
    }

    @Override
    public void onLocationChanged(Location location) {

        this.circle.setLatLong(new LatLong(location.getLatitude(),location.getLongitude()));
        this.innerCircle.setLatLong(new LatLong(location.getLatitude(),location.getLongitude()));
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
