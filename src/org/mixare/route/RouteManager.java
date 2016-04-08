package org.mixare.route;

import android.location.Location;

import org.mixare.MixContext;
import org.mixare.gui.opengl.OpenGLAugmentationView;

/**
 * Created by MelanieW on 09.02.2016.
 */
public class RouteManager {


    public RouteManager() {
    }

    public void getRoute(Location startLocation,Location endLocation) {
        RouteDataAsyncTask asyncTask = (RouteDataAsyncTask) new RouteDataAsyncTask(new AsyncResponse() {
            @Override
            public void processFinish(MyRoute route) {
                MixContext.getInstance().getRouteRenderer().updateRoute(route);
            }
        }).execute(startLocation, endLocation);
    }
}