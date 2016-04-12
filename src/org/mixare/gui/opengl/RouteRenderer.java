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
import java.util.Iterator;
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

    private Waypoint targetWaypoint;

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
                createRouteSegments(routeWaypoints);

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



    public  void onDrawFrame(GL10 gl) {

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);

      //  renderPOIMarker(gl, poiWaypoints);
        renderRouteSegments(gl, routeSegments);

    }


    /**
     * renders RouteSegements and targetDestination Point on the screen
     * @param gl
     * @param routeSegments
     */
    public void renderRouteSegments(GL10 gl, List<RouteSegment> routeSegments){
        RouteSegment previousRouteSegment= null;

        //identity matrix, has to be loaded each time route Segments are rendered
        gl.glLoadIdentity();
        gl.glMultMatrixf(rotationMatrix, 0);
        //Translate CameraView to positon:
        gl.glTranslatef(0, 0, -3f);

        if (routeSegments != null) {
            synchronized (routeSegments) {
                gl.glRotatef(0, 1, 0, 0);
                gl.glRotatef(0, 0, 1, 0);
                gl.glRotatef(0, 0, 0, 1);

                for (RouteSegment routeSegment : routeSegments) {

                    //Translate along the direction Vector of earch route segment and draw the route segment from this position backwards.
                    MyVector directionVektor = myVectorOperations.getDirectionVector(routeSegment.getEndVector(), routeSegment.getStartVector());

                    if(previousRouteSegment!= null){
                        Triangle triangle =   new Triangle(previousRouteSegment,routeSegment);
                        triangle.setColor(previousRouteSegment.getColor());
                        triangle.draw(gl);
                    }

                    gl.glTranslatef(directionVektor.getXCoordinate(), directionVektor.getYCoordinate(), 0);


                    //if it is the last route segment also draw a targetWaypoint to show to the destination.
                    if(routeSegments.indexOf(routeSegment)== routeSegments.size()-1){
                            targetWaypoint = new Waypoint();
                            targetWaypoint.draw(gl);
                            setTargetWaypoint(targetWaypoint);
                        }

                            //draw the route segment
                            routeSegment.draw(gl);
                    previousRouteSegment=routeSegment;
                }
            }
        }
    }


    /**
     * render PoiMarkers equivalent to render Route segments
     * @param gl
     * @param waypoints
     */
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


    /**
     * create Waypointlist from geoObjects
     * @param geoObjects
     * @param waypointList
     */
    public void createWaypoints(List<?> geoObjects, List<Waypoint> waypointList){
        Waypoint newWaypoint = null;
        double lat=0;
        double lon=0;

        updateCurLocation(null);
        synchronized(routeWaypoints) {
            waypointList.clear();
            for (Object curObj : geoObjects) {
                if (curObj instanceof Marker) {
                    lat = ((Marker) curObj).getLatitude();
                    lon = ((Marker) curObj).getLongitude();
                } else if (curObj instanceof LatLong) {
                    lat = ((LatLong) curObj).latitude;
                    lon = ((LatLong) curObj).longitude;
                }
                newWaypoint = new Waypoint(lat, lon, geoObjects.indexOf(curObj), this);

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

    /**
     * creates Route segments with defined color
     *
     * @param waypoints
     */
    public void createRouteSegments(List<Waypoint> waypoints){

        synchronized (routeSegments) {

            Waypoint lastWaypoint = null;
            RouteSegment tempRouteSegment;

            String routeColorString = mixContext.getSettings().getString(mixContext.getString(R.string.pref_item_routecolor_key), mixContext.getString(R.string.color_hint));
            int routeColor = Color.parseColor(routeColorString);

            routeSegments.clear();
            synchronized (waypoints) {

                for (Waypoint waypoint : waypoints) {

                    if (lastWaypoint != null) {

                        //create RouteSegment with coordinates of current and last waypoint as start vector and end vector of recantgle
                        tempRouteSegment = new RouteSegment(lastWaypoint.relativeX, lastWaypoint.relativeY, waypoint.relativeX, waypoint.relativeY);
                        tempRouteSegment.setColor(routeColor);

                        routeSegments.add(tempRouteSegment);
                    }
                    lastWaypoint = waypoint;
                }

               Iterator<RouteSegment> rsIt = routeSegments.iterator();
                while( rsIt.hasNext() ) {

                    RouteSegment rs = rsIt.next();
                    //Remove route segments with the same start and end vector
                    if( rs.getStartVector().getXCoordinate() == rs.getEndVector().getXCoordinate() &&
                            rs.getStartVector().getYCoordinate() == rs.getEndVector().getYCoordinate() ) {
                        rsIt.remove();
                        Log.d("RR", "Segment removed");
                    }

                }
            }
        }
    }


    /**
     * calculate nearest Route segment with an intersection point from current position
     * @return
     */
    public RouteSegment calculateNearestIntersectionSegment(){

        MyVector tempVector = new MyVector();

        float minIntersectionDistance = Float.MAX_VALUE;
        RouteSegment nearestIntersectionSegment = null;
        MyVector nearestIntersectionPoint = null;

        if(routeSegments != null) {
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
            if(nearestIntersectionSegment!= null) {
                nearestIntersectionSegment.setIntersectionPoint(nearestIntersectionPoint);
            }
        }
        return nearestIntersectionSegment;
    }

    /**
     * Calculate nearest point segment. checks if distance from current position to starVector is < or > distance from end Vector for each
     * route segment and returns the nearest point segment of all route segments.
     * @return
     */
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

    /**
     * determines if minimal distance is distance to nearest intersection segment or to nearest point segment
     * and returns the relevant value
     * @param nearestIntersectionSegment
     * @param nearestPointSegment
     * @return minimal distance
     */
    private float calculateMinimalDistance(RouteSegment nearestIntersectionSegment, RouteSegment nearestPointSegment) {

        MyVector tempVector = new MyVector();

        float minPointDistance = Float.MAX_VALUE;

        float minIntersectionDistance = Float.MAX_VALUE;

        //get minimal distance from Waypoint
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
            } else { //Gerade näher als Punkt
                nearestIntersectionSegment.setIsNearestRouteSegment(true);
                nearestIntersectionSegment.update();
                return minIntersectionDistance;
            }
        }
    }

    /**
     *
     * @param minDistance
     * @return boolean for low distance
     */
    public boolean hasLowDistance(float minDistance){

        if( minDistance < 100 )
            return true;
        else
            return false;
    }



    public void updatePOIMarker(List<Marker> pois) {
        createWaypoints(pois, poiWaypoints);
    }


    /**
     * updates values relevant for route Rendering. Should be called when a new route is retrieved.
     * @param myRoute
     */
    public void updateRoute(MyRoute myRoute){
        if(myRoute != null) {
            setCurrentRoute(myRoute);

            showCustomToast();

                createWaypoints(myRoute.getCoordinateList(), routeWaypoints);


            createRouteSegments(routeWaypoints);
            }

    }

    //shows a Toast which includes distance and duration of current position to destination
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

    /**
     * updates Route segment Color. Should be updated if the current postion is changed.
     * @param routeSegments
     */
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

        //Destination Waypoint should have the same color as wolked route segments.
        getTargetWaypoint().setColor(walkedColor);
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

    public Waypoint getTargetWaypoint() {
        return targetWaypoint;
    }

    public void setTargetWaypoint(Waypoint targetWaypoint) {
        this.targetWaypoint = targetWaypoint;
    }
}