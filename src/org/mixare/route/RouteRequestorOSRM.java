package org.mixare.route;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.locoslab.api.data.carta.route.direction.Direction;
import com.locoslab.api.data.carta.route.direction.Route;
import com.locoslab.api.net.Connector;

import org.mixare.Config;
import org.mixare.MixContext;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RouteRequestorOSRM  {
    public MyRoute init(Location startLocation, Location endLocation){
        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        RoadManager roadManager = new OSRMRoadManager(MixContext.getInstance());

        if (startLocation!=null && endLocation!= null){
            waypoints.add(new GeoPoint(startLocation));
            waypoints.add(new GeoPoint(endLocation));
        }

        Road road = roadManager.getRoad(waypoints);
        return MyRoute.fromOSRMRoad(road);
    }
}