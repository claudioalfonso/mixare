package org.mixare.gui.opengl;

import android.graphics.Color;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

/**
 *
 * Created by MelanieW on 27.02.2016.
 */
public class RouteSegment {

    String routeColorString = "#ADFF2F";
    int walkedColor = (0x00ffffff &Color.parseColor(routeColorString)| alpha);

    private FloatBuffer rectVerticesBuffer;
    private FloatBuffer rectVerticesBuffer2;
    private ShortBuffer rectTrianglesBuffer;

    private MyVector startVector = new MyVector();

    public void setStartVector(MyVector startVector) {
        this.startVector = startVector;
    }

    public void setEndVector(MyVector endVector) {
        this.endVector = endVector;
    }

    private MyVector endVector = new MyVector();

    public boolean isNearestRouteSegment() {
        return isNearestRouteSegment;
    }

    public void setIsNearestRouteSegment(boolean isNearestRouteSegment) {
        this.isNearestRouteSegment = isNearestRouteSegment;
    }

    boolean isNearestRouteSegment = false;

    float resultTemp1;

    private int color = Color.argb(128, 255, 0, 255); //128, 51, 153, 255= light blue
    private static final int alpha=Color.argb(128, 0, 0, 0);

    public MyVector getLeftStartVector() {
        return leftStartVector;
    }

    public MyVector getRightStartVector() {
        return rightStartVector;
    }

    public MyVector getLeftEndVector() {
        return leftEndVector;
    }

    public MyVector getRightEndVector() {
        return rightEndVector;
    }

    MyVector leftStartVector = new MyVector();
    MyVector rightStartVector = new MyVector();
    MyVector leftEndVector = new MyVector();
    MyVector rightEndVector = new MyVector();
    MyVector leftMidVector = new MyVector();
    MyVector rightMidVector = new MyVector();

    private MyVector intersectionPoint = null;

    MyVectorOperations myVectorOperations = new MyVectorOperations();

    short[] rectTriangles = new short[]{
            0, 1, 2,
            2, 3, 0
    };

    //Start & End Vector of Routesegement is relative to
    public RouteSegment(float startX, float startY, float endX, float endY){

        startVector.setXCoordinate(startX);
        startVector.setYCoordinate(startY);
        endVector.setXCoordinate(endX);
        endVector.setYCoordinate(endY);

        update();

    }

    /**
     * updates route segments. If there is a leftMidVector, the route segment is drawn as two rectangles with two different colors,
     * for the already walked part and for the part, which wasn't walked yet.
     * If not there is just one rectangle.
     */
    public void update() {

        calculateRecVertices();

        if( leftMidVector != null ) {

            Log.d( "RS", "segment mid ("+intersectionPoint.getXCoordinate()+","+intersectionPoint.getYCoordinate()+")" );

            float rectVertices[]= {
                    leftStartVector.getXCoordinate(),leftStartVector.getYCoordinate(),0,
                    rightStartVector.getXCoordinate(), rightStartVector.getYCoordinate(),0,
                    rightMidVector.getXCoordinate(), rightMidVector.getYCoordinate(),0,
                    leftMidVector.getXCoordinate(), leftMidVector.getYCoordinate(),0,
            };

            float rectVertices2[]= {
                    leftMidVector.getXCoordinate(),leftMidVector.getYCoordinate(),0,
                    rightMidVector.getXCoordinate(), rightMidVector.getYCoordinate(),0,
                    rightEndVector.getXCoordinate(), rightEndVector.getYCoordinate(),0,
                    leftEndVector.getXCoordinate(), leftEndVector.getYCoordinate(),0,
            };

            rectVerticesBuffer
                    = getDirectFloatBuffer(rectVertices);

            rectVerticesBuffer2
                    = getDirectFloatBuffer(rectVertices2);

            rectTrianglesBuffer
                    = getDirectShortBuffer(rectTriangles);

        } else {
            //

            float rectVertices[]= {
                    leftStartVector.getXCoordinate(),leftStartVector.getYCoordinate(),0,
                    rightStartVector.getXCoordinate(), rightStartVector.getYCoordinate(),0,
                    rightEndVector.getXCoordinate(), rightEndVector.getYCoordinate(),0,
                    leftEndVector.getXCoordinate(), leftEndVector.getYCoordinate(),0,
            };

            rectVerticesBuffer
                    = getDirectFloatBuffer(rectVertices);

            rectTrianglesBuffer
                    = getDirectShortBuffer(rectTriangles);

            rectVerticesBuffer2 = null;

        }

    }


    /**
     * calculates coorindates of the route segment, from given start vector and end vector
     */
    public void calculateRecVertices(){

        MyVector directionVector= myVectorOperations.getDirectionVector(startVector, endVector);
        MyVector orthogonalDirectionVector = myVectorOperations.getOrthogonalDirectionVector(directionVector);


        resultTemp1 = myVectorOperations.getDirectionVectorLength(directionVector);


        leftStartVector.setXCoordinate(orthogonalDirectionVector.getXCoordinate() / resultTemp1);
        leftStartVector.setYCoordinate((orthogonalDirectionVector.getYCoordinate() / resultTemp1));


        rightStartVector.setXCoordinate(-orthogonalDirectionVector.getXCoordinate() / resultTemp1);
        rightStartVector.setYCoordinate(-orthogonalDirectionVector.getYCoordinate() / resultTemp1);

        leftEndVector.setXCoordinate(leftStartVector.getXCoordinate() + directionVector.getXCoordinate());
        leftEndVector.setYCoordinate(leftStartVector.getYCoordinate() + directionVector.getYCoordinate());

        rightEndVector.setXCoordinate(rightStartVector.getXCoordinate() + directionVector.getXCoordinate());
        rightEndVector.setYCoordinate(rightStartVector.getYCoordinate() + directionVector.getYCoordinate());

        if( getIntersectionPoint() != null ) {

            //two rectangles
            MyVector directionVectorMid = myVectorOperations.getDirectionVector( startVector, getIntersectionPoint() );

            leftMidVector = new MyVector();
            rightMidVector = new MyVector();

            leftMidVector.setXCoordinate(leftStartVector.getXCoordinate() + directionVectorMid.getXCoordinate());
            leftMidVector.setYCoordinate(leftStartVector.getYCoordinate() + directionVectorMid.getYCoordinate());

            rightMidVector.setXCoordinate(rightStartVector.getXCoordinate() + directionVectorMid.getXCoordinate());
            rightMidVector.setYCoordinate(rightStartVector.getYCoordinate() + directionVectorMid.getYCoordinate());

        } else {

            leftMidVector = null;
            rightMidVector = null;

        }


    }

    public void draw (GL10 gl) {


        // Counter-clockwise winding
        gl.glFrontFace(GL10.GL_CCW);

        gl.glEnable(GL10.GL_CULL_FACE);
        gl.glCullFace(GL10.GL_BACK);

        // Enabled the vertices buffer to be used during rendering.
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0,
                rectVerticesBuffer);

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA) ;

        gl.glColor4f(Color.red(color) / 255.0f, Color.green(color) / 255.0f, Color.blue(color) / 255.0f, Color.alpha(color) / 255.0f);

        gl.glDrawElements(GL10.GL_TRIANGLES, rectTriangles.length,
                GL10.GL_UNSIGNED_SHORT, rectTrianglesBuffer);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisable(GL10.GL_CULL_FACE);

        if( rectVerticesBuffer2 != null ) {

            gl.glEnable(GL10.GL_CULL_FACE);
            gl.glCullFace(GL10.GL_BACK);
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0,
                    rectVerticesBuffer2 );
            gl.glEnable(GL10.GL_BLEND);
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA) ;

            gl.glColor4f(Color.red(walkedColor) / 255.0f, Color.green(walkedColor) / 255.0f, Color.blue(walkedColor) / 255.0f, Color.alpha(walkedColor) / 255.0f);

            gl.glDrawElements(GL10.GL_TRIANGLES, rectTriangles.length,
                    GL10.GL_UNSIGNED_SHORT, rectTrianglesBuffer);
            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisable(GL10.GL_CULL_FACE);
        }

    }

    private static FloatBuffer getDirectFloatBuffer(float[] array) {
        int len = array.length * (Float.SIZE/8);
        ByteBuffer storage = ByteBuffer.allocateDirect(len);
        storage.order(ByteOrder.nativeOrder());
        FloatBuffer buffer = storage.asFloatBuffer();
        buffer.put(array);
        buffer.position(0);
        return buffer;
    }

    private static ShortBuffer getDirectShortBuffer(short[] array) {
        int len = array.length * (Short.SIZE/8);
        ByteBuffer storage = ByteBuffer.allocateDirect(len);
        storage.order(ByteOrder.nativeOrder());
        ShortBuffer buffer = storage.asShortBuffer();
        buffer.put(array);
        buffer.position(0);
        return buffer;
    }

    public MyVector getStartVector(){
        return startVector;
    }

    public MyVector getEndVector(){
        return  endVector;
    }
    public MyVector getIntersectionPoint() {
        return intersectionPoint;
    }

    public void setIntersectionPoint(MyVector intersectionPoint) {
        this.intersectionPoint = intersectionPoint;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = (0x00ffffff & color) | alpha; //remove alpha from argb color, then combine rgb with alpha
    }
}
