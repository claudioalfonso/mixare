package org.mixare.gui.opengl;

import android.location.Location;
import android.util.Log;

/**
 * Created by MelanieW on 25.03.2016.
 */
public class MyVectorOperations {


    public MyVector getDirectionVector(MyVector vector1, MyVector vector2) {

        MyVector result = new MyVector();

        result.setXCoordinate(vector1.getXCoordinate() - vector2.getXCoordinate());
        result.setYCoordinate(vector1.getYCoordinate() - vector2.getYCoordinate());
        return result;
    }

    public MyVector getOrthogonalDirectionVector(MyVector directionVector) {

        MyVector orthogonalDirectionVector = new MyVector();


        orthogonalDirectionVector.setXCoordinate(-directionVector.getYCoordinate());
        orthogonalDirectionVector.setYCoordinate(directionVector.getXCoordinate());
        return orthogonalDirectionVector;
    }


    public float getDirectionVectorLength(MyVector directionVector) {

        return (float) Math.sqrt(Math.pow(directionVector.getYCoordinate(), 2) + Math.pow(directionVector.getXCoordinate(), 2));
    }

    public MyVector lineIntersection(RouteSegement routeSegement, float currentX, float currentY) {

        MyVector intersectionPoint = new MyVector();

        MyVector directionVector = getDirectionVector(routeSegement.getStartVector(), routeSegement.getEndVector());
        MyVector orthogonalVector = getOrthogonalDirectionVector(directionVector);

        double[][] matrix = {
                {directionVector.getXCoordinate(), -orthogonalVector.getXCoordinate(), currentX - routeSegement.getStartVector().getXCoordinate()},
                {directionVector.getYCoordinate(), -orthogonalVector.getYCoordinate(), currentY - routeSegement.getStartVector().getYCoordinate()}
        };


        int i = 0;

        double f1 = matrix[1][0];
        double f2 = matrix[0][0];
        for (int j = 0; j < 3; j++) {
            matrix[0][j] *= f1;
            matrix[1][j] *= f2;
        }

        matrix[1][0] -= matrix[0][0];
        matrix[1][1] -= matrix[0][1];
        matrix[1][2] -= matrix[0][2];

        f1 = matrix[1][1];
        f2 = matrix[0][1];
        for (int j = 0; j < 3; j++) {
            matrix[0][j] *= f1;
            matrix[1][j] *= f2;
        }
        matrix[0][0] -= matrix[1][0];
        matrix[0][1] -= matrix[1][1];
        matrix[0][2] -= matrix[1][2];

        double x = matrix[0][2] / matrix[0][0];
        double y = matrix[1][2] / matrix[1][1];

        intersectionPoint.setXCoordinate(routeSegement.getStartVector().getXCoordinate() + (float) x * directionVector.getXCoordinate());
        intersectionPoint.setYCoordinate(routeSegement.getStartVector().getYCoordinate() + (float) y * directionVector.getYCoordinate());
        Log.i("Test3", "Faktor x " + x + "Faktor y" + y + "sollte null sein" + matrix[0][1] + " " + matrix[1][0] + "schnittpunkt" + intersectionPoint.getXCoordinate() + " " + intersectionPoint.getYCoordinate());


        if (routeSegement.getStartVector().getXCoordinate() < intersectionPoint.getXCoordinate() && intersectionPoint.getXCoordinate() < routeSegement.getEndVector().getXCoordinate() ||
                routeSegement.getStartVector().getXCoordinate() > intersectionPoint.getXCoordinate() && intersectionPoint.getXCoordinate() > routeSegement.getEndVector().getXCoordinate()){
            if (routeSegement.getStartVector().getYCoordinate() < intersectionPoint.getYCoordinate() && intersectionPoint.getYCoordinate() < routeSegement.getEndVector().getYCoordinate() ||
                    routeSegement.getStartVector().getYCoordinate() > intersectionPoint.getYCoordinate() && intersectionPoint.getYCoordinate() > routeSegement.getEndVector().getYCoordinate()){
                return intersectionPoint;
            }
        }
         return null;
    }

}
