package org.mixare.gui.opengl;

import java.util.Vector;

/*
 * Vector with (X|Y) Coordinates, unsed as Points in a 3 Dimensional Coordinate System. Z Coordinate is currently 0 in any
 * use case, because it isn't needed for outdoor routing functionality.
 *
 * Created by MelanieW on 01.03.2016.
 */
public class MyVector {

    private float xCoordinate;
    private float yCoordinate;
    private float distance;

    public MyVector() {};
    public MyVector( float x, float y ) { xCoordinate = x; yCoordinate = y; };

    public void setXCoordinate(float xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public float getXCoordinate(){
        return xCoordinate;
    }

    public void setYCoordinate (float yCoordinate){
        this.yCoordinate = yCoordinate;
    }

    public float getYCoordinate (){
        return yCoordinate;
    }

    public void setDistance (float distance){this.distance = distance;}

    public float getDistance (){return distance;}

}
