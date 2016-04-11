package org.mixare.gui.opengl;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;

import org.mixare.MixContext;

/**
 * This class is a View for drawing and manipulating objects using OpelGL ES.
 * A GLSurfaceView Renderer is added here. Furthermore a sensorEventListener is implemented
 * to capture sensor events
 */

public class OpenGLAugmentationView extends GLSurfaceView implements SensorEventListener {

    public RouteRenderer routeRenderer;
    public SensorManager sensorManager;

    private Sensor rotatioVectorSensor;


    private final float[] rotationMatrix = new float[16];
    private final float[] rotationMatrix2 = new float[16];


    public OpenGLAugmentationView(Context context, SensorManager sensorManager) {
        super(context);

        this.sensorManager=sensorManager;

        rotatioVectorSensor = sensorManager.getDefaultSensor(
                Sensor.TYPE_ROTATION_VECTOR);
        // initialize the rotation matrix to identity
        rotationMatrix[0] = 1;
        rotationMatrix[4] = 1;
        rotationMatrix[8] = 1;
        rotationMatrix[12] = 1;


        //use 8 bits per color, 16bits for depth, 0 for stencil
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        routeRenderer = new RouteRenderer();
        MixContext.getInstance().setRouteRenderer(routeRenderer);
        setRenderer(routeRenderer);
        setZOrderOnTop(true);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);

        requestFocus();
        setFocusableInTouchMode(true);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            //Rotationvector as 4x4 matrix. This is interpreted as the inverse of rotation-vector
            SensorManager.getRotationMatrixFromVector(
                    rotationMatrix, event.values);
            //Remap of CoordinateSystem is necessary because of landscape-mode.
            // To map sensor coordinates to screen coordinates change X-Axis to Y-Axis and Y-Axis to -X-Axis.
            // Output is the transformed rotation matrix, rotationMatrix2
            sensorManager.remapCoordinateSystem(
                    rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, rotationMatrix2);
        }

        routeRenderer.setRotationMatrix(rotationMatrix2);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

}
