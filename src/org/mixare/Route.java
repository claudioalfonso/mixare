package org.mixare;

import android.location.Location;
import android.util.Log;

import org.mapsforge.core.model.LatLong;
import org.mixare.data.MarkerBuilder;
import org.mixare.lib.HtmlUnescape;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.render.MixVector;
import org.mixare.marker.RouteMarker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MelanieW on 09.02.2016.
 */
public class Route {

    private List<RouteMarker> routeMarkerList;
    private List<LatLong> coordinateList;
    private TouchSurfaceView cubeView = null;

    public Route(TouchSurfaceView cubeView) {

        routeMarkerList = new ArrayList<>();
        coordinateList = new ArrayList<>();
        this.cubeView = cubeView;
    }


    public List<LatLong> getRoute() {

        //Location curLocation = MixViewDataHolder.getInstance().getCurLocation();
        //Location curDestination = MixViewDataHolder.getInstance().getCurDestination();

        Location curLocation1 = new Location("CUR_LOC");
        curLocation1.setLatitude(51.50544);
        curLocation1.setLongitude(7.45175);

        Location curDestination = new Location("CUR_Dest");
        //curDestination=Config.getDefaultFix();
        curDestination.setLatitude(51.51017);
        curDestination.setLongitude(7.45083);



        coordinateList = new ArrayList();
        routeMarkerList = new ArrayList();
        RouteDataAsyncTask asyncTask = (RouteDataAsyncTask) new RouteDataAsyncTask(new AsyncResponse() {
            @Override
            public void processFinish(List<LatLong> latLong) {
                for (LatLong lat : latLong) {
                    Log.i("Info1", "LatLongs" + lat.latitude);
                    coordinateList.add(lat);
                }
                routeMarkerList = convertIntoMarker (coordinateList);
                cubeView.cubeRenderer.redraw(routeMarkerList);
                cubeView.requestRender();
            }

        }).execute(curLocation1, curDestination);
        return coordinateList;
    }

    public List<RouteMarker> convertIntoMarker(List<LatLong> coordinateList){

        Log.i("Test1", "routeMarker"+routeMarkerList.size());

        for( LatLong latLong: coordinateList){
            RouteMarker routeMarker = new RouteMarker("1","title", latLong.latitude,latLong.longitude,0.0,null,0,2);
            routeMarker.getLocationVector().calculateRelative(MixViewDataHolder.getInstance().getCurLocation(), routeMarker.getGeoLocation());
            Log.i("Test2", "routeMarker"+routeMarkerList.size());
            routeMarkerList.add(routeMarker);

          /*  routeMarker = new MarkerBuilder().setId("1")
                    .setTitle("title")
                    .setLatitude(latLong.latitude)
                    .setLongitude(latLong.longitude)
                    .setAltitude(0.0)
                    .setDisplayType(null)
                    .setPageURL(null)
                    .setColor()
                    .build();
                    */
        }
        return routeMarkerList;
    }


    public void getVektorsForMarkers() {

        float vektor [] = new float[3];
        for (RouteMarker routeMarker : routeMarkerList) {
            vektor[0] = routeMarker.getLocationVector().getX();
            vektor[1] = routeMarker.getLocationVector().getY();
            vektor[2] = routeMarker.getLocationVector().getZ();
            addRouteSegment(vektor);
        }
    }

    public void addRouteSegment(float[] vektor) {


    }


}
