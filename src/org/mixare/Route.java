package org.mixare;

import android.location.Location;
import android.util.Log;

import org.mapsforge.core.model.LatLong;
import org.mixare.marker.RouteMarker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MelanieW on 09.02.2016.
 */
public class Route {

    private List<RouteMarker> routeMarkerList;
    private List<LatLong> coordinateList;
    private RouteView cubeView = null;

    public Route(RouteView cubeView) {

        coordinateList = new ArrayList<>();
        this.cubeView = cubeView;
    }

    public List<LatLong> getRoute(Location startLocation,Location endLocation) {
        coordinateList = new ArrayList();
        RouteDataAsyncTask asyncTask = (RouteDataAsyncTask) new RouteDataAsyncTask(new AsyncResponse() {
            @Override
            public void processFinish(List<LatLong> latLong) {
                for (LatLong lat : latLong) {
                    Log.i("Info1", "LatLongs" + lat.latitude);
                    coordinateList.add(lat);
                }
                cubeView.routeRenderer.updateRoute(coordinateList);
                //cubeView.requestRender();
            }
        }).execute(startLocation, endLocation);
        return coordinateList;
    }
}