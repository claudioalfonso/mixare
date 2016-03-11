package org.mixare.gui.opengl;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;

import org.mixare.gui.opengl.RouteRenderer;


public class OpenGLAugmentationView extends GLSurfaceView implements SensorEventListener {

    public RouteRenderer routeRenderer;
    public SensorManager sensorManager;

    private Sensor mRotationVectorSensor;


    private final float[] mRotationMatrix = new float[16];
    private final float[] mRotationMatrix2 = new float[16];
    private final float[] orientation = new float[3];


    public OpenGLAugmentationView(Context context, SensorManager sensorManager) {
        super(context);

        this.sensorManager=sensorManager;

        mRotationVectorSensor = sensorManager.getDefaultSensor(
                Sensor.TYPE_ROTATION_VECTOR);
        // initialize the rotation matrix to identity
        mRotationMatrix[0] = 1;
        mRotationMatrix[4] = 1;
        mRotationMatrix[8] = 1;
        mRotationMatrix[12] = 1;


        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        routeRenderer = new RouteRenderer();
        setRenderer(routeRenderer);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        requestFocus();
        setFocusableInTouchMode(true);
    }

   /* public void start() {
        // enable our sensor when the activity is resumed, ask for
        // 10 ms updates.
        sensorManager.registerListener(this, mRotationVectorSensor, 10000);
    }

    public void stop() {
        // make sure to turn our sensor off when the activity is paused
        sensorManager.unregisterListener(this);
    } */

    @Override
    public void onSensorChanged(SensorEvent event) {


        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            //Log.i(Config.TAG."Test1", "SensorType" + Sensor.TYPE_ROTATION_VECTOR);
            //Rotationvector as 4x4 matrix. This is interpreted as the inverse of rotation-vector
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, event.values);
            sensorManager.remapCoordinateSystem(
                    mRotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mRotationMatrix2);
            SensorManager.getOrientation(mRotationMatrix2, orientation);
        }

        routeRenderer.setRotationMatrix(mRotationMatrix2);
        requestRender();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
