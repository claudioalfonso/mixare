package org.mixare;

import android.location.Location;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import org.mapsforge.core.util.MercatorProjection;
import org.mixare.lib.marker.Marker;
import org.mixare.marker.POIMarker;
import org.mixare.marker.RouteMarker;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class CubeRenderer implements GLSurfaceView.Renderer{
    private final List<Cube> cubes;

    private static final int MERCATOR_SCALE = 1000000;

    private float pitch = 0.0F;
    private float roll = 0.0F;
    private float azimuth = 0.0F;

    float x;
    float y;
    float z;

    float xx =0;
    float yy =0;
    float zz =0;

    private  float[] rotationMatrix = new float[16];

    double startCoordX;
    double startCoordY;
    double startCoordZ;
    double end1CoordX;
    double end1CoordY;
    double endCoordZ;
    double end2CoordX;
    double end2CoordY;

    MixViewDataHolder mixViewDataHolder;

    public CubeRenderer() {

        cubes = new ArrayList<>();

        mixViewDataHolder=MixViewDataHolder.getInstance();

        double lat1 =51.50595;
        double lat2 = 51.50694;
        double lat3 =51.49895;
        double lon1 = 7.44921;
        double lon2 =7.45076;
        double lon3 = 7.45303;

        lat1 = 51.4618;
        lon1 = 7.0166;


        lat2 = 51.46153;
        lon2 = 7.01621;
        lat3 = 51.4622912;
        lon3 = 7.0113985;

        startCoordZ = -3f;
        endCoordZ = 0f;
        endCoordZ = 0f;

        startCoordY = MercatorProjection.latitudeToPixelY(lat1, MERCATOR_SCALE);
        startCoordX =  MercatorProjection.longitudeToPixelX(lon1, MERCATOR_SCALE);

        end1CoordY = MercatorProjection.latitudeToPixelY(lat2, MERCATOR_SCALE);
        end1CoordX =  MercatorProjection.longitudeToPixelX(lon2, MERCATOR_SCALE);

        end2CoordY = MercatorProjection.latitudeToPixelY(lat3, MERCATOR_SCALE);
        end2CoordX =  MercatorProjection.longitudeToPixelX(lon3, MERCATOR_SCALE);
    }

    public  void onDrawFrame(GL10 gl) {
        /* clear screen*/
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        /* set MatrixMode to model view*/
        gl.glMatrixMode(GL10.GL_MODELVIEW);

        gl.glLoadIdentity();

        gl.glMultMatrixf(rotationMatrix, 0);

        gl.glTranslatef(0, 0, -3f);

        Location curLocation = mixViewDataHolder.getCurLocation();
        if(curLocation!=null) {
            startCoordX = MercatorProjection.longitudeToPixelX(curLocation.getLongitude(), MERCATOR_SCALE);
            startCoordY = MercatorProjection.latitudeToPixelY(curLocation.getLatitude(), MERCATOR_SCALE);
        }

        if (cubes != null) {
            synchronized (cubes) {

                gl.glRotatef(0, 1, 0, 0);
                gl.glRotatef(0, 0, 1, 0);
                gl.glRotatef(0, 0, 0, 1);

                if (cubes.isEmpty()){
                    Log.d(Config.TAG+" CubeRenderer","no cubes set");
                }

                for (Cube curCube:cubes ) {
                    gl.glTranslatef( ((float) curCube.getX() - (float) startCoordX), ((float) startCoordY - (float) curCube.getY()), 0);
                    if (cubes.size()>0) {
                        cubes.get(0).draw(gl);
                    }
                    gl.glTranslatef(-((float) curCube.getX() - (float) startCoordX),-((float) startCoordY - (float) curCube.getY()),0);
                }

                /*
                gl.glTranslatef( ((float) end1CoordX - (float) startCoordX), ((float) startCoordY - (float) end1CoordY), 0);
                if (cubes.size()>0) {
                    cubes.get(0).draw(gl);
                }
                gl.glTranslatef(-((float) end1CoordX - (float) startCoordX),-((float) startCoordY - (float) end1CoordY),0);



                gl.glTranslatef( ((float) end2CoordX - (float) startCoordX), ((float) startCoordY - (float) end2CoordY), 0);
                if (cubes.size()>0) {
                    cubes.get(1).draw(gl);
                }
                gl.glTranslatef(-((float) end2CoordX - (float) startCoordX),-((float) startCoordY - (float) end2CoordY),0);
                */

                gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
                gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
            }
        }
    }

    public void updatePOIMarker(List<Marker> pois){
        Cube newCube=null;
        double coordX=0;
        double coordY=0;
        double coordZ=0;
        synchronized (cubes) {
            this.cubes.clear();
            for (Marker curPoi:pois ) {
                coordX =  MercatorProjection.longitudeToPixelX(curPoi.getLongitude(), MERCATOR_SCALE);
                coordY = MercatorProjection.latitudeToPixelY(curPoi.getLatitude(), MERCATOR_SCALE);

                newCube = new Cube(coordX,coordY,coordZ);
                this.cubes.add(newCube);
            }
        }
    }

    public void updateRoute(List<RouteMarker> routeMarkerList){
/*
        for(RouteMarker routeMarker : routeMarkerList){
                xx = routeMarker.getLocationVector().getX();
                zz =  routeMarker.getLocationVector().getZ();
                yy = routeMarker.getLocationVector().getY();;
                synchronized (cubes) {
                    cubes.add(new Cube(xx, yy, zz));

                    x= routeMarkerList.get(0).getLocationVector().getX();
                    y= routeMarkerList.get(0).getLocationVector().getY();
                    z= routeMarkerList.get(0).getLocationVector().getZ();
                }
        }
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
        GLU.gluPerspective(gl, 45f, ratio, 1, 6000000);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
     /*
     * By default, OpenGL enables features that improve quality
     * but reduce performance. One might want to tweak that
     * especially on software renderer.
     */
        gl.glDisable(GL10.GL_DITHER);

    /*
     * Some one-time OpenGL initialization can be made here
     * probably based on features of this particular context
     */
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,  GL10.GL_FASTEST);

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
