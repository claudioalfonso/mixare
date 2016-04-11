package org.mixare.route;

import android.location.Location;
import android.util.Log;

import com.locoslab.api.data.carta.route.direction.Route;
import com.locoslab.api.data.carta.route.direction.Step;
import com.locoslab.api.data.maps.model.Coordinate;

import org.mapsforge.core.model.LatLong;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Extends Route from Locoslab with methods to getDurationInMinutes and to getDistanceInKMandMeters and getter /
 * setter for a coordinatelist
 *
 * Created by MelanieW on 05.03.2016.
 */


public class MyRoute extends com.locoslab.api.data.carta.route.direction.Route {

    private List<LatLong> coordinateList;

    public MyRoute(Route route) {

        coordinateList = new ArrayList<>();
        this.setDistance(route.getDistance());
        this.setDuration(route.getDuration());
        this.setSourceCoordinate(route.getSourceCoordinate());
        this.setTargetCoordinate(route.getTargetCoordinate());
        this.setSteps(route.getSteps());

        coordinateList.add(new LatLong(getSourceCoordinate().getLatitude(), getSourceCoordinate().getLongitude()));

        for (Step s: getSteps()){
            for(Coordinate c : s.getCoordinates()){
                coordinateList.add(new LatLong(c.getLatitude(), c.getLongitude()));
            }
        }
        coordinateList.add(new LatLong(getTargetCoordinate().getLatitude(), getTargetCoordinate().getLongitude()));
    }

    public List<LatLong> getCoordinateList() {
        return coordinateList;
    }

    public void setCoordinateList(List<LatLong> coordinateList) {
        this.coordinateList = coordinateList;
    }

    public String getDurationInMinutes(){

        String durationInHourAndMinutes = "";


        Calendar calendar =  Calendar.getInstance();
        calendar.set(0, 0, 0, 0, 0, 0);
        calendar.set(calendar.SECOND, getDuration());
        int hour = calendar.get(calendar.HOUR_OF_DAY);
        int minute = calendar.get(calendar.MINUTE);
        int seconds = calendar.get(calendar.SECOND);

        if(hour>0){
            durationInHourAndMinutes = hour + " h ";
        }

        durationInHourAndMinutes += minute + " min";

        return durationInHourAndMinutes;


    }

    public String getDistanceInKMandMeters(){

        int km = (int)getDistance() / 1000;
        int meters = (int)getDistance() % 1000;


        return  km+ " km " + meters +" m";
    }


}
