package org.mixare.gui.opengl;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.util.MercatorProjection;
import org.mixare.MixContext;
import org.mixare.R;
import org.mixare.route.MyRoute;
import org.mixare.lib.marker.Marker;
import org.mixare.route.RouteManager;

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

    public TargetMarker getTargetMarker() {
        return targetMarker;
    }

    public void setTargetMarker(TargetMarker targetMarker) {
        this.targetMarker = targetMarker;
    }

    private TargetMarker targetMarker;

    float startCoordX = 0;
    float startCoordY = 0;

    float currX = 0;
    float currY = 0;

    private MyRoute currentRoute = null;

    Location curLocation;

    MixContext mixContext=MixContext.getInstance();

    MyVectorOperations myVectorOperations = new MyVectorOperations();

    public RouteRenderer() {

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                RouteSegment nearestIntersectionSegment;
                RouteSegment nearestPointSegment;
                float minDistance;
                RouteManager routeManager;

                updateCurLocation(location);

                //relative waypoints should be updated if the current position changes
                updateWaypointsRelative( routeWaypoints );
                updateWaypointsRelative(poiWaypoints);

                //routesegements should be updated if the curent position changes
                updateRouteSegments(routeWaypoints);

                if(!routeWaypoints.isEmpty() &&currX != 0 && currY!= 0){
                        nearestIntersectionSegment = calculateNearestIntersectionSegment();
                        nearestPointSegment = calculateNearestPointSegment();
                        minDistance = calculateMinimalDistance(nearestIntersectionSegment, nearestPointSegment);


                    //if distance from current position to current route is too high, a new route will be requested
                    if (hasLowDistance(minDistance)== false){

                        Location targetLoc = new Location("Target");
                        targetLoc.setLatitude(getCurrentRoute().getTargetCoordinate().getLatitude());
                        targetLoc.setLongitude(getCurrentRoute().getTargetCoordinate().getLongitude());

                        routeManager = MixContext.getInstance().getRouteManager();
                        routeManager.getRoute(curLocation,targetLoc);
                 }
                }

                // the route segement color changes if the current location changes
                updateRouteSegementColor(routeSegments);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        MixContext.getInstance().getLocationFinder().addLocationListerner(locationListener);

    }

    private float calculateMinimalDistance(RouteSegment nearestIntersectionSegment, RouteSegment nearestPointSegment) {

        MyVector tempVector = new MyVector();

        float minPointDistance = Float.MAX_VALUE;

        float minIntersectionDistance = Float.MAX_VALUE;

        if (nearestPointSegment.getStartVector().getDistance() >= nearestPointSegment.getEndVector().getDistance()) {
            minPointDistance = nearestPointSegment.getStartVector().getDistance();
        } else minPointDistance = nearestPointSegment.getEndVector().getDistance();


        // when there is no intersection with routeSegements, the distance to the nearest waypoint is used. Koordinates of waypoints = startvektor and endvektor of routeSegements.
        if (nearestIntersectionSegment == null) {

            nearestPointSegment.setIsNearestRouteSegment(true);

            Log.d("RR", "point distance " + minPointDistance);
            return minPointDistance;


        } else {
            minIntersectionDistance = myVectorOperations.getDirectionVectorLength(nearestIntersectionSegment.getIntersectionPoint());

            Log.d("RR", "point distance " + minPointDistance);
            Log.d("RR", "intersection distance " + minIntersectionDistance);

            if (minIntersectionDistance > minPointDistance) { //Punkt näher als Gerade
                nearestPointSegment.setIsNearestRouteSegment(true);
                return minPointDistance;
            } else {
                nearestIntersectionSegment.setIsNearestRouteSegment(true);
                // nearestIntersectionSegment.setIntersectionPoint(nearestIntersectionPoint);
                nearestIntersectionSegment.update();
                return minIntersectionDistance;
            }
        }
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
                            targetMarker = new TargetMarker(routeSegment);
                            targetMarker.draw(gl);

                            setTargetMarker(targetMarker);
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

    public boolean hasLowDistance(float minDistance){

            if( minDistance < 100 )
                return true;
        else
                return false;
    }


    public RouteSegment calculateNearestIntersectionSegment(){

        MyVector tempVector = new MyVector();

        float minIntersectionDistance = Float.MAX_VALUE;
        RouteSegment nearestIntersectionSegment = null;
        MyVector nearestIntersectionPoint = null;

        if(routeSegments != null && currY != 0 && currY != 0) {
            for(RouteSegment routeSegment : routeSegments) {

                routeSegment.setIntersectionPoint(null);
                routeSegment.update();

                //get intersectionPoint of current position to routeSegement.
                tempVector = myVectorOperations.lineIntersection(routeSegment,0, 0);

                if(tempVector!= null){

                    float length = myVectorOperations.getDirectionVectorLength(tempVector);
                    if( length < minIntersectionDistance ) {
                        minIntersectionDistance = length;
                        nearestIntersectionSegment = routeSegment;
                        nearestIntersectionPoint = tempVector;
                    }
                }
            }
            nearestIntersectionSegment.setIntersectionPoint(nearestIntersectionPoint);
        }
        return nearestIntersectionSegment;
    }

    public RouteSegment calculateNearestPointSegment(){

        float minPointDistance = Float.MAX_VALUE;
        RouteSegment nearestPointSegment = null;

        if(routeSegments != null && currY != 0 && currY != 0) {
            for (RouteSegment routeSegment : routeSegments) {


                float distStart = myVectorOperations.getDirectionVectorLength(routeSegment.getStartVector());
                routeSegment.getStartVector().setDistance(distStart);
                float distEnd = myVectorOperations.getDirectionVectorLength(routeSegment.getEndVector());
                routeSegment.getStartVector().setDistance(distEnd);

                if (minPointDistance > distStart) {
                    minPointDistance = distStart;
                    nearestPointSegment = routeSegment;
                }

                if (minPointDistance > distEnd) {
                    minPointDistance = distEnd;
                    nearestPointSegment = routeSegment;
                }
            }
        }
        return nearestPointSegment;
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
            setCurrentRoute(myRoute);

            showCustomToast();

            updateWaypoints(myRoute.getCoordinateList(), routeWaypoints);
            updateRouteSegments(routeWaypoints);
            }

    }

    private void showCustomToast() {

        LayoutInflater inflater = LayoutInflater.from(MixContext.getInstance().getActualMixViewActivity());

        View mainLayout = inflater.inflate(R.layout.toast_layout, null);
        View toastLayout = mainLayout.findViewById(R.id.toast_layout_root);

        TextView text = (TextView) toastLayout.findViewById(R.id.route_info);

        String infoText = mixContext.getString(R.string.timeToDestination)+": " + getCurrentRoute().getDurationInMinutes()+ "\n" +mixContext.getString(R.string.distanceToDestination) +": " + getCurrentRoute().getDistanceInKMandMeters();
        text.setText(infoText);

        final Toast toast = new Toast(MixContext.getInstance().getActualMixViewActivity());

        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.setView(toastLayout);


        new CountDownTimer(6000, 1000) {

            public void onTick(long millisUntilFinished) {
                toast.show();
            }

            public void onFinish() {
            }

        }.start();
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

        getTargetMarker().setColor(walkedColor);


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
        //
        this.rotationMatrix = rotationMatrix;
    }

   /* public float getStartCoordX() {
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
 */
    public float getCurrX(){
        return currX;
    }
    public float getCurrY(){
        return currY;
    }

    public MyRoute getCurrentRoute() {
        return currentRoute;
    }

    public void setCurrentRoute(MyRoute currentRoute) {
        this.currentRoute = currentRoute;
    }
}