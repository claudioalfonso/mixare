package org.mixare.gui.opengl;

import android.util.Log;

/**
 * MyVectorOperations includes Methods to work with Vectors. It includes a Method to get a direction Vector,
 * a orthogonal direction Vector, to get the length of a directionVector and to get an intersection point on a route segment
 * from the current position.
 *
 * Created by MelanieW on 25.03.2016.
 */
public class MyVectorOperations {


    //Direction Vector from two position Vectors
    public MyVector getDirectionVector(MyVector vector1, MyVector vector2) {

        MyVector result = new MyVector();

        result.setXCoordinate(vector1.getXCoordinate() - vector2.getXCoordinate());
        result.setYCoordinate(vector1.getYCoordinate() - vector2.getYCoordinate());
        return result;
    }

    //Vector which is orthogonal to direction Vector (from two position Vectors)
    public MyVector getOrthogonalDirectionVector(MyVector directionVector) {

        MyVector orthogonalDirectionVector = new MyVector();


        orthogonalDirectionVector.setXCoordinate(-directionVector.getYCoordinate());
        orthogonalDirectionVector.setYCoordinate(directionVector.getXCoordinate());
        return orthogonalDirectionVector;
    }


    //length of direction Vector
    public float getDirectionVectorLength(MyVector directionVector) {

        return (float) Math.sqrt(Math.pow(directionVector.getYCoordinate(), 2) + Math.pow(directionVector.getXCoordinate(), 2));
    }

    //returns vector as intersection point on a route segment from the current position
    public MyVector lineIntersection(RouteSegment routeSegment, float currentX, float currentY) {

        MyVector intersectionPoint = new MyVector();

        MyVector directionVector = getDirectionVector(routeSegment.getStartVector(), routeSegment.getEndVector());
        MyVector orthogonalVector = getOrthogonalDirectionVector(directionVector);


        //Matrix for using the "Gaussche Elminationsverfahren" with two unknown variables.
        //Start Equation:
        //PositionVektor (e.g. StartVektor) + directionVector = PositionVektor (CurrentPosition, which is (0|0),
        //because current position is always in the point of origin) + directionVector(which is the orthogonal directionVector)
        //Reposition of equation leads to matrix:
        double[][] matrix = {
                {directionVector.getXCoordinate(), -orthogonalVector.getXCoordinate(), currentX - routeSegment.getStartVector().getXCoordinate()},
                {directionVector.getYCoordinate(), -orthogonalVector.getYCoordinate(), currentY - routeSegment.getStartVector().getYCoordinate()}
        };


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

        double x = matrix[0][0] != 0 ? matrix[0][2] / matrix[0][0] : 0;
        double y = matrix[1][1] != 0 ? matrix[1][2] / matrix[1][1] : 0;


        intersectionPoint.setXCoordinate(routeSegment.getStartVector().getXCoordinate() + (float) x * directionVector.getXCoordinate());
        intersectionPoint.setYCoordinate(routeSegment.getStartVector().getYCoordinate() + (float) x * directionVector.getYCoordinate());


// it is necessary to check if the intersection point is located between the StartVector and the EndVector of the given RouteSegment
// it is possible that there exist intersection Points between the two lines, which aren't on the route segment
        if (routeSegment.getStartVector().getXCoordinate() <= intersectionPoint.getXCoordinate() && intersectionPoint.getXCoordinate() <= routeSegment.getEndVector().getXCoordinate() ||
                routeSegment.getStartVector().getXCoordinate() >= intersectionPoint.getXCoordinate() && intersectionPoint.getXCoordinate() >= routeSegment.getEndVector().getXCoordinate()){
            if (routeSegment.getStartVector().getYCoordinate() <= intersectionPoint.getYCoordinate() && intersectionPoint.getYCoordinate() <= routeSegment.getEndVector().getYCoordinate() ||
                    routeSegment.getStartVector().getYCoordinate() >= intersectionPoint.getYCoordinate() && intersectionPoint.getYCoordinate() >=
                            routeSegment.getEndVector().getYCoordinate()){

                return intersectionPoint;
            }
        }
         return null;
    }

}
