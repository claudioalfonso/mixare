package org.mixare;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;

import org.mixare.marker.RouteMarker;

import java.util.List;


class TouchSurfaceView extends GLSurfaceView implements SensorEventListener {

    public CubeRenderer cubeRenderer;


    public TouchSurfaceView(Context context, SensorManager sensorManager) {
        super(context);

        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        cubeRenderer = new CubeRenderer();
        setRenderer(cubeRenderer);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        requestFocus();
        setFocusableInTouchMode(true);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
       float azimuth = event.values[0]; //angle around z-axis
        float pitch = event.values[1];//angle around x-axis
        float roll = event.values[2];//angle around y-axis

    /*    float azimuth = event.values[0]; //angle around z-axis
        float pitch = event.values[2];//angle around x-axis
        float roll = -event.values[1];//angle around y-axis */

        cubeRenderer.setAzimuth(azimuth);
        cubeRenderer.setPitch(pitch);
        cubeRenderer.setRoll(roll);
        requestRender();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
