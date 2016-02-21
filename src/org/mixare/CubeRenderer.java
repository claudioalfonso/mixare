package org.mixare;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import org.mixare.marker.RouteMarker;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

/**
 * Render a cube.
 */
class CubeRenderer implements GLSurfaceView.Renderer{

    private Cube mCube;
    private Cube bCube;
    private float pitch = 0.0F;
    private float roll = 0.0F;
    private float azimuth = 0.0F;
    private List<Cube> cubes;
    float x;
    float y;
    float z;

    float xx =0;
    float yy =0;
    float zz =0;

    private final float[] mRotationMatrix = new float[16];
    private final float[] mRotationMatrix2 = new float[16];


    public CubeRenderer() {

      /*  for(RouteMarker routeMarker : routeMarkerList){
            cubes.add(new Cube());
        } */

        //  mCube = new Cube();
        //  bCube = new Cube();
        cubes = new ArrayList<>();
    }

    public  void onDrawFrame(GL10 gl) {

        /* clear screen*/
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        /* set MatrixMode to model view*/
        gl.glMatrixMode(GL10.GL_MODELVIEW);

        //gl.glPushMatrix();

        gl.glLoadIdentity();

        //for View (Camera)
       gl.glScalef(1, 1, 1);
        gl.glRotatef(azimuth, 0, 0, 1);
        gl.glRotatef(-pitch, 0, 1, 0);
        gl.glRotatef(roll, 1, 0, 0);
        gl.glTranslatef(1, -1, -4f);



/*
        gl.glScalef(1, 1, 1);
        gl.glRotatef(-roll, 0, 0, 1);
        gl.glRotatef(-0, 0, 1, 0);
        gl.glRotatef(-pitch, 1, 0, 0);
        gl.glTranslatef(-1, 2, 4);
        */



        Log.i("Info4", "roll:" + roll + "pitch:" + pitch + "azim" + azimuth);

        //  gl.glRotatef(0, 0, 0, 1);
        //  gl.glRotatef(roll, 0, 1, 0);
        //  gl.glRotatef(-pitch, 1, 0, 0);
        //  gl.glTranslatef(-0, -3, -6f);

        Log.i("Info3", "Größe der Cubeliste" + cubes.size());

        if (cubes != null) {
            synchronized (cubes) {

                for (Cube cube : cubes) {


                    // for Model
                    //gl.glTranslatef(-pitch, 0, -8f);

                   // gl.glLoadIdentity();

                    gl.glTranslatef(cube.getX(), cube.getY(), cube.getZ());

                    gl.glRotatef(0, 1, 0, 0);
                    gl.glRotatef(0, 0, 1, 0);
                    gl.glRotatef(0, 0, 0, 1);


                    //  gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                    //  gl.glEnableClientState(GL10.GL_COLOR_ARRAY);


                    cube.draw(gl);

                    //gl.glPopMatrix();

                    // z = z+1;
                    Log.i("Info2", "Z Wert des LocatioNVektors:" + cube.getZ());
                    Log.i("Info2", "y Wert des LocatioNVektors:" + cube.getY());

                }

               // gl.glLoadIdentity();
                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

                Log.i("Info4", "Z EndWert des LocatioNVektors:" + z);


                //GLU.gluLookAt(gl, 0.0F, 2.0F, 0.0F,pitch,roll, 0.0F, 0.0F,1.0F,0.0F);

        /*
        mCube.draw(gl);

        gl.glTranslatef(0, 5, -5);
        gl.glRotatef(0, 1, 0, 0);
        gl.glRotatef(0, 0, 1, 0);
        gl.glRotatef(0, 0, 0, 1);

        bCube.draw(gl);

*/

            }
        }
    }


    public void redraw(List<RouteMarker> routeMarkerList){

      /*  for(RouteMarker routeMarker : routeMarkerList){
                xx= routeMarker.getLocationVector().getX();
                //  yy= routeMarker.getLocationVector().getY();
                zz=  routeMarker.getLocationVector().getZ();
                //  xx = xx+1;
                yy = 0;
                //  zz = 0;
                synchronized (cubes) {
                    cubes.add(new Cube(xx, yy, zz));

                    x= routeMarkerList.get(0).getLocationVector().getX();
                    Log.i("Info1", "X Wert des LocatioNVektors:" + xx);
                    y= routeMarkerList.get(0).getLocationVector().getY();
                    Log.i("Info1", "Y Wert des LocatioNVektors:" + yy);
                    z= routeMarkerList.get(0).getLocationVector().getZ();
                    Log.i("Info1", "Z Wert des LocatioNVektors:" + zz);
                }
        } */

        for(RouteMarker routeMarker : routeMarkerList) {
            xx = 0;
            yy = yy+2;
            zz = 0;
            //  zz = 0;
            synchronized (cubes) {
                cubes.add(new Cube(xx, yy, zz));

                x = routeMarkerList.get(0).getLocationVector().getX();
                Log.i("Info1", "X Wert des LocatioNVektors:" + xx);
                y = routeMarkerList.get(0).getLocationVector().getY();
                Log.i("Info1", "Y Wert des LocatioNVektors:" + yy);
                z = routeMarkerList.get(0).getLocationVector().getZ();
                Log.i("Info1", "Z Wert des LocatioNVektors:" + zz);
            }


        }


        //drawNewCube(gl);
    }


    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);

     /*
      * Set our projection matrix. This doesn't have to be done
      * each time we draw, but usually a new projection needs to
      * be set when the viewport is resized.
      */

        float ratio = (float) width / height;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 6000);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
     /*
     * By default, OpenGL enables features that improve quality
     * but reduce performance. One might want to tweak that
     * but reduce performance. One might want to tweak that
     * especially on software renderer.
     */
        gl.glDisable(GL10.GL_DITHER);

    /*
     * Some one-time OpenGL initialization can be made here
     * probably based on features of this particular context
     */
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                GL10.GL_FASTEST);


        gl.glClearColor(0, 0, 0, 0);

        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);

    }



    public void setAzimuth(Float azimuth){
        this.azimuth = azimuth;
    }

    public void setRoll(Float roll){
        this.roll = roll;
    }

    public void setPitch(Float pitch){
        this.pitch = pitch;
    }
}
