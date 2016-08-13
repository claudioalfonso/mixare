package org.mixare.route;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.mixare.Config;
import org.mixare.MixContext;
import org.mixare.R;
import org.mixare.gui.opengl.OpenGLAugmentationView;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class RouteManager {

    public RouteManager() {
    }
    public void getRoute(Location startLocation,Location endLocation) {
        AsyncTask asyncTask = null;

        String routingService=MixContext.getInstance().getSettings().getString(MixContext.getInstance().getString(R.string.pref_item_routing_service_key), MixContext.getInstance().getString(R.string.routing_service_value_locoslab));

        if(routingService.equals(MixContext.getInstance().getString(R.string.routing_service_value_locoslab))){
            asyncTask = new RouteDataAsyncTask(new AsyncResponse() {
                @Override
                public void processFinish(MyRoute route) {
                    MixContext.getInstance().getRouteRenderer().updateRoute(route);
                    Log.d(Config.TAG,"Routing: Locoslab");
                }
            }).execute(startLocation, endLocation);

        } else if(routingService.equals(MixContext.getInstance().getString(R.string.routing_service_value_osrm))){
            asyncTask = (RouteDataAsyncTaskOSRM) new RouteDataAsyncTaskOSRM(new AsyncResponse() {
                @Override
                public void processFinish(MyRoute route) {
                    MixContext.getInstance().getRouteRenderer().updateRoute(route);
                    Log.d(Config.TAG,"Routing: OSRM");
                }
            }).execute(startLocation, endLocation);
        }
    }
}