package org.mixare;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.List;
import java.util.Vector;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by MelanieW on 27.02.2016.
 */
public class Rectangle {

    private FloatBuffer rectVerticesBuffer;
    private ShortBuffer rectTrianglesBuffer;

    public float getStartY() {
        return startY;
    }

    public float getStartX() {
        return startX;
    }

    public float getEndY() {
        return endY;
    }

    public float getEndX() {
        return endX;
    }

    private float startX;
    private float startY;
    private float endX;
    private float endY;
    //float [] results;
    Vector [] results = new Vector[4];

    short[] rectTriangles = new short[]{
            0, 1, 2,
            2, 3, 0
    };

    public Rectangle(float startX, float startY, float endX, float endY){

        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;

        endX = endX - startX;
        endY = startY - endY;

        Log.i("Result1", startX+ " "+ startY+ " "+endX + " "+ endY);

     /*   float [] rectVertices ={
                0,  0+1, 0.0f,  // 0, Top Left
                0, 0-1.0f, 0.0f,  // 1, Bottom Left
                endX, endY-1, 0.0f,  // 2, Bottom Right
                endX,  endY+1, 0.0f, // 3, Top Right
        };
        */


       /* float [] rectVertices ={
                startX-1,  startY+2, 0.0f,  // 0, Top Left
                startX-1, startY+2+2, 0.0f,  // 1, Bottom Left
                endX-1, endY+2, 0.0f,  // 2, Bottom Right
                endX-1,  endY+2+2, 0.0f, // 3, Top Right
        };*/



        results = calculateRecVertices();

      /*  Log.i("Results", results[0] + " " + results[1]+ " " +
                results[2]+ " " + results[3]+ " " +
                results[4]+ " " + results[5]+ " " +
                results[6]+ " " + results[7]);



      /*  float rectVertices[] = {
                results[0],results[1] , 0,
                results[2], results[3], 0,
                results[6], results[7], 0,
                results[4], results[5], 0,

        };
        */
        float rectVertices[]= {
                (float)results[0].get(0), (float)results[0].get(1),0,
                (float)results[1].get(0), (float)results[1].get(1),0,
                (float)results[3].get(0), (float)results[3].get(1),0,
                (float)results[2].get(0), (float)results[2].get(1),0,
        };



        /* float rectVertices[] = {
                -1.0f,  1.0f, 0.0f,  // 0, Top Left
                -1.0f, -1.0f, 0.0f,  // 1, Bottom Left
                1.0f, -1.0f, 0.0f,  // 2, Bottom Right
                1.0f,  1.0f, 0.0f,  // 3, Top Right
        }; */

        rectVerticesBuffer
                = getDirectFloatBuffer(rectVertices);
      /*  short[] rectTriangles = new short[]{
                0, 1, 2,
                2, 3, 0
        };*/
        rectTrianglesBuffer
                = getDirectShortBuffer(rectTriangles);

    }

    public Vector<Float> [] calculateRecVertices(){

        float directionX;
        float directionY;
        //float [] results = new float[8];
        float resultTemp1;
        Vector [] results = new Vector[4];

        //Richtungsvektor
        directionX = startX-endX;
        directionY = startY-endY;

        Log.i("Result1", startX+ " "+ startY+ " "+endX + " "+ endY);


        //Abstand der Verktoren: Für Richtungsvektor (X,Y) > (-Y,X)
        resultTemp1 = (float)Math.sqrt(Math.pow(-directionY,2) + Math.pow(directionX,2));
        //Normierung des Vektors auf die Länge 1, Ergebnisse für (X,Y) Koorinaten je Kante des Rechtecks.

        Vector<Float> v1 = new Vector<Float>();
        Vector<Float> v2 = new Vector<Float>();
        Vector<Float> v3 = new Vector<Float>();
        Vector<Float> v4 = new Vector<Float>();

        v1.addElement(new Float(-directionY/resultTemp1));
        v1.addElement(new Float(directionX/resultTemp1));
        results[0]=v1;

        v2.addElement(new Float(directionY/resultTemp1));
        v2.addElement(new Float(-directionX/resultTemp1));
        results[1]=v2;

        v3.addElement(new Float(v1.firstElement()+directionX));
        v3.addElement(new Float(v1.lastElement()+directionY));
        results[2]=v3;

        v4.addElement(new Float(v2.firstElement()+directionX));
        v4.addElement(new Float(v2.lastElement()+directionY));
        results[3]=v4;

        /*results[0] = -directionY/resultTemp1;

        results[1]= directionX/resultTemp1;

        results[2]= directionY/resultTemp1;

        results[3]= -directionX/resultTemp1;

        results[4]=results[0]+directionX;

        results[5]= results[1]+directionY;

        results[6]= results[2]+directionX;

        results[7]=results[3]+directionY;
        */

        return results;
    }

    public void draw (GL10 gl) {

        // gl.glVertexPointer(3, GL10.GL_FLOAT, 0, rectVerticesBuffer);
        // gl.glClearColor(0, 0, 0, 0);
        // gl.glColor4f(1.0f, 0.0f, 0.0f, 1.0f);
        // gl.glDrawElements(GL10.GL_TRIANGLES, rectVertices.,
        //        GL10.GL_UNSIGNED_SHORT, rectTrianglesBuffer);

        // Counter-clockwise winding.
        gl.glFrontFace(GL10.GL_CCW);
        // Enable face culling.
        gl.glEnable(GL10.GL_CULL_FACE);
        // What faces to remove with the face culling.
        gl.glCullFace(GL10.GL_BACK);

        // Enabled the vertices buffer for writing and to be used during
        // rendering.
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // Specifies the location and data format of an array of vertex
        // coordinates to use when rendering.
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0,
                rectVerticesBuffer);

        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA) ;


        gl.glColor4f(0.2f, 0.6f, 1.0f, 0.5f);

        gl.glDrawElements(GL10.GL_TRIANGLES, rectTriangles.length,
                GL10.GL_UNSIGNED_SHORT, rectTrianglesBuffer);

        // Disable the vertices buffer.
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        // Disable face culling.
        gl.glDisable(GL10.GL_CULL_FACE);
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
}
