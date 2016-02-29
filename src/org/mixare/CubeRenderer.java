package org.mixare;

import android.location.Location;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.util.Log;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.util.MercatorProjection;
import org.mixare.lib.marker.Marker;

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

    float startCoordX = 0;
    float startCoordY = 0;

    float currX = 0;
    float currY = 0;

    final Object semaphore = true;

    Location curLocation;

    MixViewDataHolder mixViewDataHolder;

    public CubeRenderer() {
        mixViewDataHolder = MixViewDataHolder.getInstance();
    }

    public  void onDrawFrame(GL10 gl) {
        /* clear screen*/
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        /* set MatrixMode to model view*/
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glMultMatrixf(rotationMatrix, 0);
        gl.glTranslatef(0, 0, -3f);





        synchronized (semaphore) {
            renderCubes(gl, routeCubes);
        }

        synchronized (semaphore) {
            renderCubes(gl, poiCubes);
        }


        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
    }

    public void renderCubes(GL10 gl, List<Cube> cubes){
        float previousX = 0;
        float previousY = 0;

        float startCoordX = 0;
        float startCoordY = 0;

        if (cubes != null) {
            gl.glRotatef(0, 1, 0, 0);
            gl.glRotatef(0, 0, 1, 0);
            gl.glRotatef(0, 0, 0, 1);

            for (Cube cube : cubes) {
                if (curLocation != null) {
                    if (currX != startCoordX || currY != startCoordY) {
                        cube.setRelativeX(cube.getAbsoluteX() - currX);
                        cube.setRelativeY(currY - cube.getAbsoluteY());
                    }
                }

                if (cubes.indexOf(cube) == 0) {

                } else if (cubes.indexOf(cube) == 1) {
                    gl.glTranslatef(cube.getRelativeX(), cube.getRelativeY(), 0);
                } else {
                //   gl.glTranslatef((float) relativeEnd2CoordX - (float) relativeEndCoordX, (float) relativeEnd2CoordY - (float) relativeEndCoordY, 0);
                    gl.glTranslatef(cube.getRelativeX() - previousX, cube.getRelativeY() - previousY, 0);
                }

                cube.draw(gl);
                previousX = cube.getRelativeX();
                previousY = cube.getRelativeY();

                // Log.i("Info3", "X Wert des LocatioNVektors:" + cube.getRelativeX());
                // Log.i("Info3", "Y Wert des LocatioNVektors:" + cube.getRelativeY());
            }
            //GLU.gluLookAt(gl, 0.0F, 2.0F, 0.0F,pitch,roll, 0.0F, 0.0F,1.0F,0.0F);
        }
    }

    public void updateCubes(List<?> geoObjects){
        synchronized (semaphore) {

            float startCoordX = 0;
            float startCoordY = 0;

            Cube newCube = null;
            final List<Cube> cubeList;
            double lat = 0;
            double lon = 0;

            if (geoObjects.size() > 0 && geoObjects.get(0) instanceof Marker) {
                cubeList = poiCubes;
            } else if (geoObjects.size() > 0 && geoObjects.get(0) instanceof LatLong) {
                cubeList = routeCubes;
            } else {
                cubeList = new ArrayList<>();
            }

            updateCurLocation(null);
//            Log.d(Config.TAG, "cubeList.clear();");
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
//        Log.d(Config.TAG, "updatePOIMarker "+pois.size());
        updateCubes(pois);
    }

    public void updateRoute(List<LatLong> coordinateList){
//        Log.d(Config.TAG, "updateRoute "+coordinateList.size());
        updateCubes(coordinateList);
    }

    public Cube createCube(double lat, double lon, int index){
        float absoluteX =(float) MercatorProjection.longitudeToPixelX(lon, MERCATOR_SCALE);
        float absoluteY =(float) MercatorProjection.latitudeToPixelY(lat, MERCATOR_SCALE);

        float relativeX = 0;
        float relativeY = 0;

        float startCoordX = 0;
        float startCoordY = 0;

        if(index == 0){
            startCoordX = relativeX = absoluteX;
            startCoordY = relativeY = absoluteY;
        }
        else {
            relativeX = absoluteX - startCoordX;
            relativeY = startCoordY - absoluteY;
        }
        return new Cube(relativeX, relativeY, absoluteX, absoluteY);
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

    public void logState(String additional){
        String state="";
        state+=" startCoordX: "+startCoordX;
        state+=" startCoordY: "+startCoordY;

        Log.d(Config.TAG+" CR State", additional+": "+ state);
    }
}
