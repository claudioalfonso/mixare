package org.mixare;

import android.location.Location;
import android.util.Log;

import org.mapsforge.core.model.LatLong;
import org.mixare.lib.marker.Marker;
import org.mixare.marker.RouteMarker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MelanieW on 09.02.2016.
 */
public class Route {

    private List<RouteMarker> routeMarkerList;
    private List<LatLong> coordinateList;
    private List<Marker> poiMarkerList;
    private TouchSurfaceView cubeView = null;

    public Route(TouchSurfaceView cubeView, List<Marker> markers) {

        coordinateList = new ArrayList<>();
        this.cubeView = cubeView;
        this.poiMarkerList = markers;
    }


    public List<LatLong> getRoute() {

        //Location curLocation = MixViewDataHolder.getInstance().getCurLocation();
        //Location curDestination = MixViewDataHolder.getInstance().getCurDestination();

        Location curLocation1 = new Location("CUR_LOC");
        curLocation1.setLatitude(51.4618);
        curLocation1.setLongitude(7.0166);

        Location curDestination = new Location("CUR_Dest");
        //curDestination=Config.getDefaultFix();
        curDestination.setLatitude(51.4585);
        curDestination.setLongitude(6.9996);

        coordinateList = new ArrayList();
        RouteDataAsyncTask asyncTask = (RouteDataAsyncTask) new RouteDataAsyncTask(new AsyncResponse() {
            @Override
            public void processFinish(List<LatLong> latLong) {

                for (LatLong lat : latLong) {
                    Log.i("Info1", "LatLongs" + lat.latitude);
                    coordinateList.add(lat);
                }
                cubeView.cubeRenderer.updateRoute(coordinateList);
                cubeView.cubeRenderer.updatePOIMarker(poiMarkerList);
                cubeView.requestRender();
            }

        }).execute(curLocation1, curDestination);

        return coordinateList;
    }



}
