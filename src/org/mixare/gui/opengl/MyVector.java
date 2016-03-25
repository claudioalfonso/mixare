package org.mixare.gui.opengl;

import java.util.Vector;

/**
 * Created by MelanieW on 01.03.2016.
 */
public class MyVector {

    private float xCoordinate;
    private float yCoordinate;
    private float distance;


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
