package org.mixare.gui.opengl;

import android.graphics.Color;
import android.location.Location;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.util.MercatorProjection;
import org.mixare.MixContext;
import org.mixare.R;
import org.mixare.route.AsyncResponse;
import org.mixare.MixViewDataHolder;
import org.mixare.route.MyRoute;
import org.mixare.route.RouteDataAsyncTask;
import org.mixare.lib.marker.Marker;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Render geographic objects (markers or routes)
 */
public class RouteRenderer implements GLSurfaceView.Renderer{
    private  float[] rotationMatrix = new float[16];

    private static final int MERCATOR_SCALE = 10000000;

    private List<Waypoint> routeWaypoints = new ArrayList<>();
    private List<Waypoint> poiWaypoints = new ArrayList<>();

    float startCoordX = 0;
    float startCoordY = 0;

    float currX = 0;
    float currY = 0;

    public MyRoute getActualRoute() {
        return actualRoute;
    }

    public void setActualRoute(MyRoute actualRoute) {
        this.actualRoute = actualRoute;
    }

    private MyRoute actualRoute = null;

    Location curLocation;

    MixViewDataHolder mixViewDataHolder;

    MixContext mixContext=MixContext.getInstance();

    public RouteRenderer() {
        mixViewDataHolder = MixViewDataHolder.getInstance();
    }


    public  void onDrawFrame(GL10 gl) {

        updateCurLocation(curLocation = mixViewDataHolder.getCurLocation());

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);

       // gl.glPushMatrix();

        renderRouteSegements(gl, poiWaypoints, true);
        renderRouteSegements(gl, routeWaypoints, false);

        if(!routeWaypoints.isEmpty() &&currX != 0 && currY!= 0){
            if (hasLowDistance()== false){
                Location targetLoc = new Location("Target");
                targetLoc.setLatitude(getActualRoute().getTargetCoordinate().getLatitude());
                targetLoc.setLongitude(getActualRoute().getTargetCoordinate().getLongitude());
                RouteDataAsyncTask asyncTask = (RouteDataAsyncTask) new RouteDataAsyncTask(new AsyncResponse() {
                    @Override
                    public void processFinish(MyRoute route) {
                        updateRoute(route);
                    }
                }).execute(curLocation,targetLoc);
            }
        }
        //gl.glPopMatrix();
    }

    public void renderRouteSegements(GL10 gl, List<Waypoint> waypoints, Boolean pointsItself){
        String routeColorString=mixContext.getSettings().getString(mixContext.getString(R.string.pref_item_routecolor_key), mixContext.getString(R.string.color_hint));
        int routeColor = Color.parseColor(routeColorString);
        gl.glLoadIdentity();
        gl.glMultMatrixf(rotationMatrix, 0);
        gl.glTranslatef(0, 0, -3f);

        float previousX = 0;
        float previousY = 0;
        Waypoint lastWaypoint = null;
        RouteSegement tempRouteSegement = null;
        TargetMarker targetMarker = null;

        if (waypoints != null) {
            synchronized (waypoints) {
                gl.glRotatef(0, 1, 0, 0);
                gl.glRotatef(0, 0, 1, 0);
                gl.glRotatef(0, 0, 0, 1);

                for (Waypoint waypoint : waypoints) {
                    
                    if (curLocation != null) {
                            waypoint.setRelativeX(waypoint.getAbsoluteX() - currX);
                            waypoint.setRelativeY(currY - waypoint.getAbsoluteY());
                    }

                    gl.glTranslatef(waypoint.getRelativeX() - previousX, waypoint.getRelativeY() - previousY, 0);

                    if(pointsItself) {
                        waypoint.draw(gl);
                    }
                    if(!pointsItself) {
                        if(waypoints.indexOf(waypoint)==waypoints.size()-1){
                            //targetMarker = new TargetMarker(waypoint.relativeX,waypoint.relativeY);
                            targetMarker = new TargetMarker();
                            targetMarker.draw(gl);
                        }
                        if(lastWaypoint!=null) {
                            tempRouteSegement = new RouteSegement(lastWaypoint.relativeX, lastWaypoint.relativeY, waypoint.relativeX, waypoint.relativeY);
                            tempRouteSegement.setColor(routeColor);
                            tempRouteSegement.draw(gl);
                        }
                    }

                    previousX = waypoint.getRelativeX();
                    previousY = waypoint.getRelativeY();
                    lastWaypoint = waypoint;
                }
            }
        }
    }

    public void updateWaypoints(List<?> geoObjects, List<Waypoint> waypointList){
        Waypoint newWaypoint = null;
        double lat=0;
        double lon=0;

        updateCurLocation(null);
        synchronized(waypointList){
            waypointList.clear();
            for (Object curObj : geoObjects) {
                if (curObj instanceof Marker){
                    lat=((Marker)curObj).getLatitude();
                    lon=((Marker)curObj).getLongitude();
                } else if (curObj instanceof LatLong){
                    lat=((LatLong)curObj).latitude;
                    lon=((LatLong)curObj).longitude;
                }
                newWaypoint = new Waypoint(lat, lon, geoObjects.indexOf(curObj),this);

                if (curObj instanceof Marker) {
                    newWaypoint.setColor(((Marker) curObj).getColor());
                }
                waypointList.add(newWaypoint);
            }
        }
    }

    public Waypoint getNearestWaypoint(List<Waypoint> waypoints){
        Waypoint waypoint = null;
        float distance= 100000000;
        synchronized (waypoints) {
            for (Waypoint w : waypoints) {
                if (w.distanceToCurrentPosition() < distance) {
                    distance = w.distanceToCurrentPosition();
                    waypoint = w;
                }
            }
        }
        return waypoint;
    }

    public boolean hasLowDistance(){
        Waypoint waypoint = getNearestWaypoint(routeWaypoints);
//        Log.d("Waypoint", "Nearest Waypoint, distance" + waypoint.distanceToCurrentPosition());
        if(waypoint.distanceToCurrentPosition()<50) {
            return true;
        }
        else{
            return false;
        }
    }

    public void updatePOIMarker(List<Marker> pois) {
        updateWaypoints(pois, poiWaypoints);
    }

    /*public void updateRoute(List<LatLong> coordinateList){
        updateWaypoints(coordinateList, routeWaypoints);
    }*/
    public void updateRoute(MyRoute myRoute){
 //       Log.i("Info3", "Steps" + myRoute.getCoordinateList().size());
        setActualRoute(myRoute);
        updateWaypoints(myRoute.getCoordinateList(), routeWaypoints);
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
        float ratio = (float) width / (float) height;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45f, ratio, 1, 6000000);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
  
        gl.glDisable(GL10.GL_DITHER);
        
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,  GL10.GL_FASTEST);
        gl.glClearColor(0, 0, 0, 0);
        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glShadeModel(GL10.GL_SMOOTH);
        gl.glEnable(GL10.GL_DEPTH_TEST);
    }

    public void setRotationMatrix(float[] rotationMatrix) {
        this.rotationMatrix = rotationMatrix;
    }

    public float getStartCoordX() {
        return startCoordX;
    }

    public void setStartCoordX(float startCoordX) {
        this.startCoordX = startCoordX;
    }

    public float getStartCoordY() {
        return startCoordY;
    }

    public void setStartCoordY(float startCoordY) {
        this.startCoordY = startCoordY;
    }
}