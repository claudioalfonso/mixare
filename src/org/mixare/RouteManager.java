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
public class RouteManager {

    private RouteView cubeView = null;

    public RouteManager(RouteView cubeView) {


        this.cubeView = cubeView;
    }

    public void getRoute(Location startLocation,Location endLocation) {
        RouteDataAsyncTask asyncTask = (RouteDataAsyncTask) new RouteDataAsyncTask(new AsyncResponse() {
            @Override
            public void processFinish(MyRoute route) {
                cubeView.routeRenderer.updateRoute(route);
                //cubeView.requestRender();
            }
        }).execute(startLocation, endLocation);
    }
}