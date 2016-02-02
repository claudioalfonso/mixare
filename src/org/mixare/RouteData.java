package org.mixare;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.locoslab.api.data.carta.route.direction.Direction;
import com.locoslab.api.data.carta.route.direction.Route;
import com.locoslab.api.data.carta.route.direction.Step;
import com.locoslab.api.data.carta.route.direction.StreetSegment;
import com.locoslab.api.data.maps.model.Coordinate;
import com.locoslab.api.data.maps.util.Coordinates;
import com.locoslab.api.net.Connector;

import org.mapsforge.core.model.LatLong;
import org.mixare.map.MixMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MelanieW on 19.01.2016.
 */
public class RouteData extends Connector {

    private String url= "https://cloud.locoslab.com/carta/route?origin=sch%C3%BCtzenbahn%2070,%20essen&destination=universit%C3%A4tsstra%C3%9Fe,%20essen&mode=walking";
    private Direction tmp = null;

    public List<LatLong> init(LatLong destination){

        if (destination!= null){
            changeUrl(destination);
        }

        List<LatLong> latLongs = new ArrayList<>();

        try {
            Direction result = this.executeObject(Connector.METHOD_GET, url, Direction.class, tmp);

            for (Route r : result.getRoutes()) {

                latLongs.add(new LatLong(r.getSourceCoordinate().getLatitude(), r.getSourceCoordinate().getLongitude()));
                for (Step s: r.getSteps()){
                    for(Coordinate c : s.getCoordinates()){
                       latLongs.add(new LatLong(c.getLatitude(), c.getLongitude()));
                    }
                }
                latLongs.add(new LatLong(r.getTargetCoordinate().getLatitude(), r.getTargetCoordinate().getLongitude()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    return latLongs;
    }

    public String changeUrl(LatLong destination){
        Double latitude= destination.latitude;
        Double longitude= destination.longitude;
        url="https://cloud.locoslab.com/carta/route?origin=sch%C3%BCtzenbahn%2070,%20essen&destination="+latitude+",%20+"+longitude+"&mode=walking";
        return url;
    }
}
