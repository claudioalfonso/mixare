package org.mixare;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import org.mapsforge.core.model.LatLong;
import org.mixare.marker.RouteMarker;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;
import org.mapsforge.core.util.MercatorProjection;


/**
 * Render a cube.
 */
class CubeRenderer implements GLSurfaceView.Renderer{


    private List<Cube> cubes;

    float xx =0;
    float yy =0;

    float previousX = 0;
    float previousY = 0;

    private  float[] rotationMatrix = new float[16];

    float startCoordX;
    float startCoordY;



    public CubeRenderer() {

      /*  for(RouteMarker routeMarker : routeMarkerList){
            cubes.add(new Cube());
        } */

        cubes = new ArrayList<>();

    }

    public  void onDrawFrame(GL10 gl) {

        /* clear screen*/
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        /* set MatrixMode to model view*/
        gl.glMatrixMode(GL10.GL_MODELVIEW);

        gl.glLoadIdentity();

        gl.glMultMatrixf(rotationMatrix, 0);


        gl.glTranslatef(0, 0, -3f);


        if (cubes != null) {
            synchronized (cubes) {


                gl.glRotatef(0, 1, 0, 0);
                gl.glRotatef(0, 0, 1, 0);
                gl.glRotatef(0, 0, 0, 1);

                for (Cube cube: cubes) {


                    if (cubes.indexOf(cube) == 0) {

                    }
                   else if (cubes.indexOf(cube) == 1) {
                        gl.glTranslatef(cube.getX(), cube.getY(), 0);
                        cube.draw(gl);
                        previousX = cube.getX();
                        previousY = cube.getY();

                    } else {


                     //   gl.glTranslatef((float) relativeEnd2CoordX - (float) relativeEndCoordX, (float) relativeEnd2CoordY - (float) relativeEndCoordY, 0);
                       gl.glTranslatef(cube.getX()-previousX, cube.getY()-previousY, 0);
                        cube.draw(gl);
                        previousX = cube.getX();
                        previousY = cube.getY();


                    }
                    Log.i("Info3", "X Wert des LocatioNVektors:" + cube.getX());
                    Log.i("Info3", "Y Wert des LocatioNVektors:" + cube.getY());
                }

                //GLU.gluLookAt(gl, 0.0F, 2.0F, 0.0F,pitch,roll, 0.0F, 0.0F,1.0F,0.0F);


                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
            }
        }
    }


    public void redraw(List<LatLong> coordinateList){

        for (LatLong latLong : coordinateList){

          yy= (float)  MercatorProjection.latitudeToPixelY(latLong.latitude, 10000000);
          xx= (float) MercatorProjection.longitudeToPixelX(latLong.longitude,10000000);

            if(coordinateList.indexOf(latLong) == 0){
                startCoordX = xx;
                startCoordY = yy;
            }
            else {

                xx = xx- startCoordX;
                yy = startCoordY - yy;

            }

            synchronized (cubes) {

                cubes.add(new Cube(xx, yy));

                Log.i("Info1", "X Wert des LocatioNVektors:" + xx);
                Log.i("Info1", "Y Wert des LocatioNVektors:" + yy);



            }

        }

    }


    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);

     /*
      * Set our projection matrix. This doesn't have to be done
      * each time we draw, but usually a new projection needs to
      * be set when the viewport is resized.
      */

        float ratio = (float) width / (float) height;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        //gl.glFrustumf(-ratio, ratio, -1, 1, 1, 6000);
       // GLU.gluPerspective(gl, 45f, ratio, 1, 1000);
        GLU.gluPerspective(gl, 45f, ratio, 1, 6000000);


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



    public void setRotationMatrix(float[] rotationMatrix) {
        this.rotationMatrix = rotationMatrix;
    }
}
