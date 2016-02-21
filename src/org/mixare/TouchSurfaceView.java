package org.mixare;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.Log;

import org.mixare.marker.RouteMarker;

import java.util.List;


class TouchSurfaceView extends GLSurfaceView implements SensorEventListener {

    public CubeRenderer cubeRenderer;
    public SensorManager sensorManager;

    private Sensor mRotationVectorSensor;


    private final float[] mRotationMatrix = new float[16];
    private final float[] mRotationMatrix2 = new float[16];
    private final float[] orientation = new float[3];


    public TouchSurfaceView(Context context, SensorManager sensorManager) {
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
        cubeRenderer = new CubeRenderer();
        setRenderer(cubeRenderer);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        requestFocus();
        setFocusableInTouchMode(true);
    }

    public void start() {
        // enable our sensor when the activity is resumed, ask for
        // 10 ms updates.
        sensorManager.registerListener(this, mRotationVectorSensor, 10000);
    }

    public void stop() {
        // make sure to turn our sensor off when the activity is paused
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {




    /*    float azimuth = event.values[0]; //angle around z-axis
        float pitch = event.values[1];//angle around x-axis
        float roll = event.values[2];//angle around y-axis */

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            Log.i("Test1", "SensorType" + Sensor.TYPE_ROTATION_VECTOR);
            // convert the rotation-vector to a 4x4 matrix. the matrix
            // is interpreted by Open GL as the inverse of the
            // rotation-vector, which is what we want.
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, event.values);
            sensorManager.remapCoordinateSystem(
                    mRotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, mRotationMatrix2);
            SensorManager.getOrientation(mRotationMatrix2, orientation);

            orientation[0]= (float)Math.toDegrees(orientation[0]);
            orientation[1]= (float)Math.toDegrees(orientation[1]);
            orientation[2]= (float)Math.toDegrees(orientation[2]);
        }

        float azimuth = orientation[0]; //angle around x-axis
        float pitch = orientation[1];//angle around y-axis
        float roll = orientation[2];//angle around z-axis

        cubeRenderer.setAzimuth(azimuth);
        cubeRenderer.setPitch(pitch);
        cubeRenderer.setRoll(roll);
        cubeRenderer.setRotationMatrix(mRotationMatrix2);

        requestRender();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
