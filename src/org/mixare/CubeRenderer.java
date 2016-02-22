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
import org.mapsforge.core.util.MercatorProjection;


/**
 * Render a cube.
 */
class CubeRenderer implements GLSurfaceView.Renderer{


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

    private  float[] rotationMatrix = new float[16];

    private Sensor mRotationVectorSensor;

    private SensorManager mSensorManager;
    double startCoordX;
    double startCoordY;
    double startCoordZ;
    double endCoordX;
    double endCoordY;
    double endCoordZ;
    double end2CoordX;
    double end2CoordY;




    public CubeRenderer() {



      /*  for(RouteMarker routeMarker : routeMarkerList){
            cubes.add(new Cube());
        } */


        cubes = new ArrayList<>();


        double lat1 =51.50595;
        double lat2 = 51.50694;
        double lat3 =51.49895;
        double lon1 = 7.44921;
        double lon2 =7.45076;
        double lon3 = 7.45303;


        Log.d(Config.TAG, "vorher");


       //startCoordY =  MercatorProjection.latitudeToTileY(lat1,10000);
       // endCoordY = MercatorProjection.latitudeToPixelY(lat1, 10);
       // endCoordX = MercatorProjection.longitudeToPixelX(lon1,10);
       // endCoordZ = -3f;




        Log.d(Config.TAG, "nachher" + endCoordX +" "+ endCoordY);
       // double earthRadius = 6.371;
        double earthRadius = 6378137.0;




        startCoordZ = -3f;
        endCoordZ = 0f;
        endCoordZ = 0f;

        startCoordY = MercatorProjection.latitudeToPixelY(lat1, 1000000);
        startCoordX =  MercatorProjection.longitudeToPixelX(lon1, 1000000);
        Log.i("Info7", "startX"+ startCoordX+ "StartY"+ startCoordY+ "startZ"+ startCoordZ);

        endCoordY = MercatorProjection.latitudeToPixelY(lat2, 1000000);
        endCoordX =  MercatorProjection.longitudeToPixelX(lon2, 1000000);

        end2CoordY = MercatorProjection.latitudeToPixelY(lat3, 1000000);
        end2CoordX =  MercatorProjection.longitudeToPixelX(lon3, 1000000);

        Log.i("Info7", "endX"+ endCoordX+ "StartY"+ endCoordY+ "startZ"+ endCoordZ);
        Log.i("Info7", "endX"+ end2CoordX+ "StartY"+ end2CoordY+ "startZ"+ endCoordZ);


    }

    public  void onDrawFrame(GL10 gl) {



        /* clear screen*/
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        /* set MatrixMode to model view*/
        gl.glMatrixMode(GL10.GL_MODELVIEW);

        //gl.glPushMatrix();

        gl.glLoadIdentity();

        gl.glMultMatrixf(rotationMatrix,0);

        //for View (Camera)
        /*gl.glScalef(1, 1, 1);
        gl.glRotatef(-roll, 0, 0, 1);
        gl.glRotatef(-azimuth, 0, 1, 0);
        gl.glRotatef(pitch, 1, 0, 0);
        */
        //gl.glTranslatef(1, -1, -4f);
       // gl.glTranslatef(0, 0, -3f);



        gl.glTranslatef(0, 0, -3f);

         //gl.glTranslatef((float) startCoordX , (float)startCoordY ,(float) startCoordZ);
        //gl.glTranslatef((float) startCoordX, (float) startCoordZ, (float) startCoordY);







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

            /*    for (Cube cube : cubes) {


                    // for Model
                    //gl.glTranslatef(-pitch, 0, -8f);
                   // gl.glLoadIdentity();

                  //  gl.glTranslatef(cube.getX(),cube.getY() , cube.getZ());

                    gl.glTranslatef((float)endCoordX,(float)endCoordY ,(float) endCoordZ);
                    //gl.glTranslatef((float)endCoordX, (float) endCoordZ ,(float)endCoordY);



                    gl.glRotatef(0, 1, 0, 0);
                    gl.glRotatef(0, 0, 1, 0);
                    gl.glRotatef(0, 0, 0, 1);


                    //  gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                    //  gl.glEnableClientState(GL10.GL_COLOR_ARRAY);


                    cube.draw(gl);

                    //gl.glPopMatrix();

                    // z = z+1;
                 //   Log.i("Info2", "Z Wert des LocatioNVektors:" + cube.getZ());
                  //  Log.i("Info2", "y Wert des LocatioNVektors:" + cube.getY());

                } */

               // gl.glTranslatef((float) endCoordX, (float) endCoordY, (float)endCoordZ);
                //gl.glTranslatef((float)endCoordX, (float) endCoordZ ,(float)endCoordY);



                gl.glRotatef(0, 1, 0, 0);
                gl.glRotatef(0, 0, 1, 0);
                gl.glRotatef(0, 0, 0, 1);

                gl.glTranslatef((float) endCoordX - (float) startCoordX, (float) startCoordY - (float) endCoordY, 0);

                if (cubes.size()>0) {
                    cubes.get(0).draw(gl);
                }
                gl.glTranslatef(-((float)endCoordX- (float)startCoordX),-((float)startCoordY-(float)endCoordY),0);

                gl.glTranslatef(((float)end2CoordX- (float)startCoordX),((float)startCoordY-(float)end2CoordY),0);


                if (cubes.size()>0) {
                    cubes.get(1).draw(gl);
                }

                gl.glTranslatef(-((float)end2CoordX- (float)startCoordX),-((float)startCoordY-(float)end2CoordY),0);

                // gl.glLoadIdentity();
                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

                Log.i("Info4", "Z EndWert des LocatioNVektors:" + z);


                //GLU.gluLookAt(gl, 0.0F, 2.0F, 0.0F,pitch,roll, 0.0F, 0.0F,1.0F,0.0F);


            }
        }
    }


    public void redraw(List<RouteMarker> routeMarkerList){

        for(RouteMarker routeMarker : routeMarkerList){
                xx= routeMarker.getLocationVector().getX();
                //  yy= routeMarker.getLocationVector().getY();
                zz=  routeMarker.getLocationVector().getZ();
                //  xx = xx+1;
                yy = routeMarker.getLocationVector().getY();;
                 // zz = -3;
                synchronized (cubes) {
                    cubes.add(new Cube(xx, yy, zz));

                    x= routeMarkerList.get(0).getLocationVector().getX();
                    Log.i("Info1", "X Wert des LocatioNVektors:" + xx);
                    y= routeMarkerList.get(0).getLocationVector().getY();
                    Log.i("Info1", "Y Wert des LocatioNVektors:" + yy);
                    z= routeMarkerList.get(0).getLocationVector().getZ();
                    Log.i("Info1", "Z Wert des LocatioNVektors:" + zz);
                }
        }
        /*for(RouteMarker routeMarker : routeMarkerList) {
            xx = 0;
            yy = yy+3;
            zz = -4;
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
        */
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



    public void setAzimuth(Float azimuth){
        this.azimuth = azimuth;
    }

    public void setRoll(Float roll){
        this.roll = roll;
    }

    public void setPitch(Float pitch) {
        this.pitch = pitch;
    }

    public void setRotationMatrix(float[] rotationMatrix) {
        this.rotationMatrix = rotationMatrix;
    }
}
