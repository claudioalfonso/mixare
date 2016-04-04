package org.mixare.gui.opengl;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.util.Log;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.util.MercatorProjection;
import org.mixare.MixContext;
import org.mixare.R;
import org.mixare.route.AsyncResponse;
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
    private List<RouteSegment> routeSegments = new ArrayList<>();

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

    MixContext mixContext=MixContext.getInstance();

    MyVectorOperations myVectorOperations = new MyVectorOperations();

    public RouteRenderer() {

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateCurLocation(location);

                updateWaypointsRelative( routeWaypoints );
                updateWaypointsRelative(poiWaypoints);

                updateRouteSegments(routeWaypoints);

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


                updateRouteSegementColor(routeSegments);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        MixContext.getInstance().getLocationFinder().addLocationListerner(locationListener);


    }


    public  void onDrawFrame(GL10 gl) {

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);

       // gl.glPushMatrix();

        renderPOIMarker(gl, poiWaypoints);
        renderRouteSegements(gl, routeSegments);

    }

    public void renderRouteSegements(GL10 gl, List<RouteSegment> routeSegments){

      //  String routeColorString=mixContext.getSettings().getString(mixContext.getString(R.string.pref_item_routecolor_key), mixContext.getString(R.string.color_hint));
       // int routeColor = Color.parseColor(routeColorString);
        gl.glLoadIdentity();
        gl.glMultMatrixf(rotationMatrix, 0);
        gl.glTranslatef(0, 0, -3f);

        float previousX = 0;
        float previousY = 0;
        TargetMarker targetMarker = null;

        if (routeSegments != null) {
            synchronized (routeSegments) {
                gl.glRotatef(0, 1, 0, 0);
                gl.glRotatef(0, 0, 1, 0);
                gl.glRotatef(0, 0, 0, 1);

                for (RouteSegment routeSegment : routeSegments) {
              //      routeSegement.setColor(routeColor);

                    gl.glTranslatef(routeSegment.getEndVector().getXCoordinate() - previousX, routeSegment.getEndVector().getYCoordinate() - previousY, 0);

                        if(routeSegments.indexOf(routeSegment)== routeSegments.size()-1){
                            //targetMarker = new TargetMarker(waypoint.relativeX,waypoint.relativeY);
                            targetMarker = new TargetMarker();
                            targetMarker.draw(gl);
                        }
                            routeSegment.draw(gl);

                    previousX = routeSegment.getEndVector().getXCoordinate();
                    previousY = routeSegment.getEndVector().getYCoordinate();
                }
            }
        }
    }

    public void renderPOIMarker(GL10 gl, List<Waypoint> waypoints){
        gl.glLoadIdentity();
        gl.glMultMatrixf(rotationMatrix, 0);
        gl.glTranslatef(0, 0, -3f);

        float previousX = 0;
        float previousY = 0;


        if (waypoints != null) {
            synchronized (waypoints) {
                gl.glRotatef(0, 1, 0, 0);
                gl.glRotatef(0, 0, 1, 0);
                gl.glRotatef(0, 0, 0, 1);

                for (Waypoint waypoint : waypoints) {

                    gl.glTranslatef(waypoint.getRelativeX() - previousX, waypoint.getRelativeY() - previousY, 0);

                    waypoint.draw(gl);

                    previousX = waypoint.getRelativeX();
                    previousY = waypoint.getRelativeY();
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

    public void updateWaypointsRelative( List<Waypoint> waypoints ) {

        for (Waypoint waypoint : waypoints) {

            if (curLocation != null) {
                waypoint.setRelativeX(waypoint.getAbsoluteX() - currX);
                waypoint.setRelativeY(currY - waypoint.getAbsoluteY());
            }

        }

    }

    public void updateRouteSegments(List<Waypoint> waypoints){

        synchronized (routeSegments) {

            Waypoint lastWaypoint = null;
            RouteSegment tempRouteSegment;

            String routeColorString = mixContext.getSettings().getString(mixContext.getString(R.string.pref_item_routecolor_key), mixContext.getString(R.string.color_hint));
            int routeColor = Color.parseColor(routeColorString);



            routeSegments.clear();
            synchronized (waypoints) {

                for (Waypoint waypoint : waypoints) {

                    if (lastWaypoint != null) {

                        tempRouteSegment = new RouteSegment(lastWaypoint.relativeX, lastWaypoint.relativeY, waypoint.relativeX, waypoint.relativeY);
                        tempRouteSegment.setColor(routeColor);

                        routeSegments.add(tempRouteSegment);
                    }
                    lastWaypoint = waypoint;
                }
            }
        }
    }


    /*public RouteSegment getNearestRouteSegement(List<RouteSegment> routeSegments){
        RouteSegment nearestRouteSegment = null;
        float distance= 100000000;
        float tempDistance = 0;
        //   synchronized (waypoints) {
        for (RouteSegment routeSegment : routeSegments) {

            if (routeSegment.getIntersectionPoint() != null) {
                MyVector direction = new MyVector();
                //direction.setXCoordinate(routeSegment.getIntersectionPoint().getXCoordinate() - currX);
                //direction.setYCoordinate(routeSegment.getIntersectionPoint().getYCoordinate() - currY);

                direction.setXCoordinate(routeSegment.getIntersectionPoint().getXCoordinate() - 0);
                direction.setYCoordinate(routeSegment.getIntersectionPoint().getYCoordinate() - 0);

                tempDistance = myVectorOperations.getDirectionVectorLength(direction);
                routeSegment.getIntersectionPoint().setDistance(tempDistance);
                Log.i("Test55", "Distance" + routeSegment.getIntersectionPoint().getDistance());

                if (tempDistance < distance) {
                    distance = tempDistance;
                    nearestRouteSegment = routeSegment;
                }
            }
        }
        if(nearestRouteSegment!= null) {
            nearestRouteSegment.setIsNearestRouteSegment(true);
        }
        // }
        return nearestRouteSegment;
    }
    */

    public boolean hasLowDistance(){

        ArrayList<MyVector> myVectors = new ArrayList<>();
        MyVector tempVector = new MyVector();

        float minPointDistance = Float.MAX_VALUE;
        RouteSegment nearestPointSegment = null;

        float minIntersectionDistance = Float.MAX_VALUE;
        RouteSegment nearestIntersectionSegment = null;
        MyVector nearestIntersection = null;



        if(routeSegments != null && currY != 0 && currY != 0) {
            for(RouteSegment routeSegment : routeSegments) {

                routeSegment.setIntersectionPoint(null);
                routeSegment.update();

               tempVector = myVectorOperations.lineIntersection(routeSegment,0, 0);


                if(tempVector!= null){

                    float length = myVectorOperations.getDirectionVectorLength(tempVector);
                    if( length < minIntersectionDistance ) {
                        minIntersectionDistance = length;
                        nearestIntersectionSegment = routeSegment;
                        nearestIntersection = tempVector;
                    }

                    myVectors.add(tempVector);
                    //routeSegment.setIntersectionPoint(tempVector);

                }

                float distStart = myVectorOperations.getDirectionVectorLength( routeSegment.getStartVector() );
                float distEnd = myVectorOperations.getDirectionVectorLength( routeSegment.getEndVector() );

                if( minPointDistance > distStart ) {
                    minPointDistance = distStart;
                    nearestPointSegment = routeSegment;
                }

                if( minPointDistance > distEnd ) {
                    minPointDistance = distEnd;
                    nearestPointSegment = routeSegment;
                }
            }

            }

        if( myVectors.isEmpty() ) {

            nearestPointSegment.setIsNearestRouteSegment(true);

            Log.d("RR", "point distance "+minPointDistance );

            if( minPointDistance < 100 )
                return true;

        } else if( !myVectors.isEmpty() ) {

            float distance = Float.MAX_VALUE;

            Log.d("RR", "point distance "+minPointDistance );
            Log.d("RR", "intersection distance "+minIntersectionDistance );

            if( minIntersectionDistance > minPointDistance ) { //Punkt näher als Gerade
                distance = minPointDistance;
                nearestPointSegment.setIsNearestRouteSegment(true);
            } else {
                distance = minIntersectionDistance;
                nearestIntersectionSegment.setIsNearestRouteSegment(true);
                nearestIntersectionSegment.setIntersectionPoint(nearestIntersection);
                nearestIntersectionSegment.update();
            }

            Log.d("RR", "distance "+distance );

            if( distance < 100 )
                return true;

            /*RouteSegment routeSegment =  getNearestRouteSegement( routeSegments );

            MyVector myVector = getNearestVector(myVectors);
            Log.i("NearestVektor: ", "X: " + myVector.getXCoordinate() + "Y: " + myVector.getYCoordinate() + "Distance" + myVector.getDistance());
            if (myVector.getDistance() < 500) {
                return true;
            }
            Log.i("NearestVektor: ", "X: " + routeSegment.getIntersectionPoint().getXCoordinate() + "Y: " + routeSegment.getIntersectionPoint().getYCoordinate() + "Distance" + routeSegment.getIntersectionPoint().getDistance());

            if(routeSegment.getIntersectionPoint().getDistance()<5000000){
                return true;
            }*/


        }


      /*  Waypoint waypoint = getNearestWaypoint(routeWaypoints);
//        Log.d("Waypoint", "Nearest Waypoint, distance" + waypoint.distanceToCurrentPosition());
        if(waypoint.distanceToCurrentPosition()<50) {
            return true;
        }
        else{
            return false;
        } */
       // return true;
        return false;
    }



    public void updatePOIMarker(List<Marker> pois) {
        updateWaypoints(pois, poiWaypoints);
    }

    /*public void updateRoute(List<LatLong> coordinateList){
        updateWaypoints(coordinateList, routeWaypoints);
    }*/
    public void updateRoute(MyRoute myRoute){
        if(myRoute != null) {
            //       Log.i("Info3", "Steps" + myRoute.getCoordinateList().size());
            setActualRoute(myRoute);
            updateWaypoints(myRoute.getCoordinateList(), routeWaypoints);
            updateRouteSegments(routeWaypoints);
        }
    }

    public void updateCurLocation(Location newLocation){
        curLocation = newLocation;
        if(curLocation==null){
            curLocation=MixContext.getInstance().getCurLocation();
        }
        if (curLocation != null) {
            currX = (float) MercatorProjection.longitudeToPixelX(curLocation.getLongitude(), MERCATOR_SCALE);
            currY = (float) MercatorProjection.latitudeToPixelY(curLocation.getLatitude(), MERCATOR_SCALE);
        }
    }

    public void updateRouteSegementColor(List<RouteSegment> routeSegments){

        String walkedRouteColorString = mixContext.getSettings().getString(mixContext.getString(R.string.pref_item_walkedroutecolor_key), mixContext.getString(R.string.color_hint2));;
        int walkedColor = Color.parseColor(walkedRouteColorString);


        boolean walked = true;

        for( RouteSegment routeSegment : routeSegments ) {

            if( routeSegment.isNearestRouteSegment() )
                walked = false;

            if( walked )
                routeSegment.setColor( walkedColor );
            else
                break;


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

    public float getCurrX(){
        return currX;
    }
    public float getCurrY(){
        return currY;
    }
}