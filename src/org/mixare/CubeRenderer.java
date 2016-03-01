package org.mixare;

import android.location.Location;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.util.MercatorProjection;
import org.mixare.lib.marker.Marker;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Render geographic objects (markers or routes) as cubes.
 */
class CubeRenderer implements GLSurfaceView.Renderer{
    private  float[] rotationMatrix = new float[16];

    private static final int MERCATOR_SCALE = 10000000;

    private final List<Cube> routeCubes = new ArrayList<>();
    private final List<Cube> poiCubes = new ArrayList<>();

    float currX = 0;
    float currY = 0;

    Location curLocation;

    MixViewDataHolder mixViewDataHolder;

    public CubeRenderer() {
        mixViewDataHolder = MixViewDataHolder.getInstance();
    }

    float x;
    float y;


    public  void onDrawFrame(GL10 gl) {

        x =(float) MercatorProjection.longitudeToPixelX(7.44919, MERCATOR_SCALE);
        y =(float) MercatorProjection.latitudeToPixelY(51.50595, MERCATOR_SCALE);

        Rectangle tempRectangle = null;

        /* clear screen*/
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        /* set MatrixMode to model view*/
        gl.glMatrixMode(GL10.GL_MODELVIEW);

        gl.glLoadIdentity();
        gl.glMultMatrixf(rotationMatrix, 0);
        gl.glTranslatef(0, 0, -3f);

        // gl.glRotatef(0, 1, 0, 0);
        // gl.glRotatef(0, 0, 1, 0);
        // gl.glRotatef(0, 0, 0, 1);

        // gl.glTranslatef(0,0,0);

        // gl.glPushMatrix();

        renderCubes(gl, poiCubes, false);

        // renderRecangle(gl);

        renderCubes(gl, routeCubes, true);

        // gl.glPopMatrix();
        // renderRectangles(gl,routeCubes);


        //nach oben verschoben für rectangle! ggf rückgängig machen
       // gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
       // gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
    }

    public void renderCubes(GL10 gl, List<Cube> cubes, Boolean renderRect){
        float previousX = 0;
        float previousY = 0;
        Cube tempCube = null;
        Rectangle tempRectangle = null;

        gl.glLoadIdentity();
        gl.glMultMatrixf(rotationMatrix, 0);
        gl.glTranslatef(0, 0, -3f);

        if (cubes != null) {
            synchronized (cubes) {
                gl.glRotatef(0, 1, 0, 0);
                gl.glRotatef(0, 0, 1, 0);
                gl.glRotatef(0, 0, 0, 1);

                for (Cube cube : cubes) {
                    cube.setRelativeX(cube.getAbsoluteX() - currX);
                    cube.setRelativeY(currY - cube.getAbsoluteY());

                    gl.glTranslatef(cube.getRelativeX() - previousX, cube.getRelativeY() - previousY, 0);

                    cube.draw(gl);

                    if(tempCube!=null && renderRect) {
                        tempRectangle = new Rectangle(tempCube.relativeX, tempCube.relativeY, cube.relativeX, cube.relativeY);
                        tempRectangle.draw(gl);
                    }
                    previousX = cube.getRelativeX();
                    previousY = cube.getRelativeY();
                    tempCube = cube;
                }
            }
        }
    }

    public void renderRecangle(GL10 gl){
        Rectangle tempRectangle = null;

        float x;
        float y;

        float zielX;
        float zielY;

        x =(float) MercatorProjection.longitudeToPixelX(7.44919, MERCATOR_SCALE);
        y =(float) MercatorProjection.latitudeToPixelY(51.50595, MERCATOR_SCALE);


        zielX = (float) MercatorProjection.longitudeToPixelX(7.45098, MERCATOR_SCALE);
        zielY = (float) MercatorProjection.latitudeToPixelY(51.50658, MERCATOR_SCALE);

        tempRectangle= new Rectangle(x,y,zielX,zielY);
        tempRectangle.draw(gl);
    }

    public void updateCubes(List<?> geoObjects, List<Cube> cubeList){
        Cube newCube = null;
        double lat = 0;
        double lon = 0;

        updateCurLocation(null);
        synchronized(cubeList){
            cubeList.clear();
            for (Object curObj : geoObjects) {
                if (curObj instanceof Marker) {
                    lat = ((Marker) curObj).getLatitude();
                    lon = ((Marker) curObj).getLongitude();
                } else if (curObj instanceof LatLong) {
                    lat = ((LatLong) curObj).latitude;
                    lon = ((LatLong) curObj).longitude;
                }
                newCube = createCube(lat, lon, geoObjects.indexOf(curObj));
                cubeList.add(newCube);
            }
        }
    }

    public void updatePOIMarker(List<Marker> pois) {
        updateCubes(pois, poiCubes);
    }

    public void updateRoute(List<LatLong> coordinateList){
        updateCubes(coordinateList, routeCubes);
    }

    public Cube createCube(double lat, double lon, int index){
        float absoluteX =(float) MercatorProjection.longitudeToPixelX(lon, MERCATOR_SCALE);
        float absoluteY =(float) MercatorProjection.latitudeToPixelY(lat, MERCATOR_SCALE);

        return new Cube(absoluteX, absoluteY, absoluteX, absoluteY);
    }

    public void updateCurLocation(Location newLocation){
        curLocation = newLocation;
        if(curLocation==null){
            curLocation=mixViewDataHolder.getCurLocation();
        }
        if (curLocation != null) {
            currX = (float) MercatorProjection.longitudeToPixelX(curLocation.getLongitude(), MERCATOR_SCALE);
            currY = (float) MercatorProjection.latitudeToPixelY(curLocation.getLatitude(), MERCATOR_SCALE);
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

    public void setRotationMatrix(float[] rotationMatrix) {
        this.rotationMatrix = rotationMatrix;
    }
}