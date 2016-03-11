package org.mixare.route;

import android.location.Location;

import org.mixare.gui.opengl.OpenGLView;

/**
 * Created by MelanieW on 09.02.2016.
 */
public class RouteManager {

    private OpenGLView cubeView = null;

    public RouteManager(OpenGLView cubeView) {


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