package org.mixare;/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.location.Location;
import android.util.Log;

import org.mapsforge.core.util.MercatorProjection;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * A vertex shaded cube.
 */
class Waypoint
{
 /*   public float getZ() {
        return z;
    } */

    private float absoluteX;
    private float absoluteY;

    public float relativeX;
    public float relativeY;


    private static final int MERCATOR_SCALE = 10000000;

    private boolean isStart = false;



    public float getRelativeX() {
        return relativeX;
    }

    public float getRelativeY() {
        return relativeY;
    }



    public void setRelativeX(float relativeX) {
        this.relativeX = relativeX;
    }

    public void setRelativeY(float relativeY) {
        this.relativeY = relativeY;
    }

    public float getAbsoluteX() {
        return absoluteX;
    }

    public void setAbsoluteX(float absoluteX) {
        this.absoluteX = absoluteX;
    }

    public float getAbsoluteY() {
        return absoluteY;
    }

    public void setAbsoluteY(float absoluteY) {
        this.absoluteY = absoluteY;
    }


    public  Waypoint(){}
  //  public float z;
    public Waypoint(double lat, double lon, int index, RouteRenderer routeRenderer)
    {

        absoluteX =(float) MercatorProjection.longitudeToPixelX(lon, MERCATOR_SCALE);
        absoluteY =(float) MercatorProjection.latitudeToPixelY(lat, MERCATOR_SCALE);

        if(index == 0){
            //sollte das so sein?-daher der große wert...
            routeRenderer.setStartCoordX(absoluteX);
            routeRenderer.setStartCoordY(absoluteY);
           // startCoordY =  absoluteY;
            isStart = true;
            relativeX = 0;
            relativeY = 0;
        }
        else {
            relativeX = absoluteX - routeRenderer.getStartCoordX();
            relativeY = routeRenderer.getStartCoordY() - absoluteY;
        }


        int one = 0x10000;
        int vertices[] = {
                -one, -one, -one,
                one, -one, -one,
                one,  one, -one,
                -one,  one, -one,
                -one, -one,  one,
                one, -one,  one,
                one,  one,  one,
                -one,  one,  one,
        };

        /*int colors[] = {
                0,    0,    0,  one,
                one,    0,    0,  one,
                one,  one,    0,  one,
                0,  one,    0,  one,
                0,    0,  one,  one,
                one,    0,  one,  one,
                one,  one,  one,  one,
                0,  one,  one,  one,
        };*/

        byte indices[] = {
                0, 4, 5,    0, 5, 1,
                1, 5, 6,    1, 6, 2,
                2, 6, 7,    2, 7, 3,
                3, 7, 4,    3, 4, 0,
                4, 7, 6,    4, 6, 5,
                3, 0, 1,    3, 1, 2
        };

        // Buffers to be passed to gl*Pointer() functions
        // must be direct, i.e., they must be placed on the
        // native heap where the garbage collector cannot
        // move them.
        //
        // Buffers with multi-byte datatypes (e.g., short, int, float)
        // must have their byte order set to native order

        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        vbb.order(ByteOrder.nativeOrder());
        mVertexBuffer = vbb.asIntBuffer();
        mVertexBuffer.put(vertices);
        mVertexBuffer.position(0);

        /*ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4);
        cbb.order(ByteOrder.nativeOrder());
        mColorBuffer = cbb.asIntBuffer();
        mColorBuffer.put(colors);
        mColorBuffer.position(0);
        */


        mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
        mIndexBuffer.put(indices);
        mIndexBuffer.position(0);
    }

    public float distanceToCurrentPosition(){
        Log.d("Results", "DistanceBetweenWaypoints" + (int) Math.sqrt(Math.pow(relativeY,2) + Math.pow(relativeX,2)));
        return (float)Math.sqrt(Math.pow(relativeY,2) + Math.pow(relativeX,2));


    }

    public void draw(GL10 gl)
    {

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
       // gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        gl.glFrontFace(gl.GL_CW);
        gl.glVertexPointer(3, gl.GL_FIXED, 0, mVertexBuffer);
       // gl.glColorPointer(4, gl.GL_FIXED, 0, mColorBuffer);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA) ;
        gl.glColor4f(0.0f, 1.0f, 0.0f, 0.8f);

        gl.glDrawElements(gl.GL_TRIANGLES, 36, gl.GL_UNSIGNED_BYTE, mIndexBuffer);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
      //  gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    }

    private IntBuffer mVertexBuffer;
    private IntBuffer mColorBuffer;
    private ByteBuffer mIndexBuffer;
}
