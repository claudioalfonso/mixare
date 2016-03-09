package org.mixare;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.locoslab.api.data.carta.route.direction.Direction;
import com.locoslab.api.data.carta.route.direction.Route;
import com.locoslab.api.data.carta.route.direction.Step;
import com.locoslab.api.data.maps.model.Coordinate;
import com.locoslab.api.net.Connector;

import org.mapsforge.core.model.LatLong;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 19.01.2016 by MelanieW.
 */
public class RouteRequestor extends Connector {

    private static final String LOCOSLAB_API_URL_BASE ="https://cloud.locoslab.com/carta/route?mode=walking";
    private static final String LOCOSLAB_API_PARAM_ORIGIN="origin";
    private static final String LOCOSLAB_API_PARAM_DESTINATION="destination";

    private static final Uri LOCOSLAB_API_URI = Uri.parse(LOCOSLAB_API_URL_BASE);

    private String currentRouteUrl = "";
    private Direction direction = null;

    public MyRoute init(Location startLocation, Location endLocation){

        if (startLocation!=null && endLocation!= null){
            currentRouteUrl= buildRouteUrl(startLocation, endLocation);
        }

        try {
            Direction resultDirection = this.executeObject(Connector.METHOD_GET, currentRouteUrl, Direction.class, direction);
            Log.i("Info4", "Steps" + resultDirection.getRoutes().get(resultDirection.getRoutes().size()-1).getSteps().size());
            return new MyRoute(resultDirection.getRoutes().get(0));


        } catch (IOException ex) {
            Log.e(Config.TAG, this.getClass().getName(), ex);
            return null;
        }
    }

    public String buildRouteUrl(Location origin, Location destination){
        Uri.Builder routeUrl = LOCOSLAB_API_URI.buildUpon()
                .appendQueryParameter(LOCOSLAB_API_PARAM_ORIGIN, origin.getLatitude() + "," + origin.getLongitude())
                .appendQueryParameter(LOCOSLAB_API_PARAM_DESTINATION, destination.getLatitude() + "," + destination.getLongitude());
        return routeUrl.build().toString();
    }
}
