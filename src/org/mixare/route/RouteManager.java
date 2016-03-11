package org.mixare.route;

import android.location.Location;

import org.mixare.gui.opengl.OpenGLAugmentationView;

/**
 * Created by MelanieW on 09.02.2016.
 */
public class RouteManager {

    private OpenGLAugmentationView openGLAugmentationView = null;

    public RouteManager(OpenGLAugmentationView openGLAugmentationView) {


        this.openGLAugmentationView = openGLAugmentationView;
    }

    public void getRoute(Location startLocation,Location endLocation) {
        RouteDataAsyncTask asyncTask = (RouteDataAsyncTask) new RouteDataAsyncTask(new AsyncResponse() {
            @Override
            public void processFinish(MyRoute route) {
                openGLAugmentationView.routeRenderer.updateRoute(route);
                //openGLView.requestRender();
            }
        }).execute(startLocation, endLocation);
    }
}