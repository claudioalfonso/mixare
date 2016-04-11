package org.mixare.gui.opengl;/*
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

import android.graphics.Color;
import android.util.Log;

import org.mapsforge.core.util.MercatorProjection;
import org.mixare.Config;
import org.mixare.MixContext;
import org.mixare.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * A Waypoint on a route.
 */
class Waypoint
{

    private IntBuffer mVertexBuffer;
    private ByteBuffer mIndexBuffer;
    private byte[] indices;

    private float absoluteX;
    private float absoluteY;

    public float relativeX;
    public float relativeY;

    private int color = Color.argb(204,0,255,0);
    private static final int alpha=Color.argb(204, 0, 0, 0);

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

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = (0x00ffffff & color) | alpha; //remove alpha from argb color, then combine rgb with alpha
    }

    public  Waypoint(){
        calculateWaypoint();
    }
    public Waypoint(double lat, double lon, int index, RouteRenderer routeRenderer) {

        absoluteX = (float) MercatorProjection.longitudeToPixelX(lon, MERCATOR_SCALE);
        absoluteY = (float) MercatorProjection.latitudeToPixelY(lat, MERCATOR_SCALE);

        if (index == 0) {
            isStart = true;
            relativeX = 0;
            relativeY = 0;
        } else {
            relativeX = absoluteX - routeRenderer.getCurrX();
            relativeY = routeRenderer.getCurrY() - absoluteY;
        }
        calculateWaypoint();
    }

    public void calculateWaypoint() {

    String walkedRouteColorString = MixContext.getInstance().getSettings().getString(MixContext.getInstance().getString(R.string.pref_item_walkedroutecolor_key), MixContext.getInstance().getString(R.string.color_hint2));
    ;
    int walkedColor = Color.parseColor(walkedRouteColorString);

    int[] vertices = {
            -45000, 0, 100000, //0
            -35000, -35000, 100000, //1
            0, -45000, 100000, //2
            35000, -35000, 100000, //3
            45000, 0, 100000, //4
            35000, 35000, 100000, //5
            0, 45000, 100000, //6
            -35000, 35000, 100000, //7
            0, 0, 200000, //8
            0, 0, 0 //9

    };

    indices = new byte[]{
            0, 1, 8,
            1, 2, 8,
            2, 3, 8,
            3, 4, 8,
            4, 5, 8,
            5, 6, 8,
            6, 7, 8,
            7, 0, 8,
            0, 1, 9,
            1, 2, 9,
            2, 3, 9,
            3, 4, 9,
            4, 5, 9,
            5, 6, 9,
            6, 7, 9,
            7, 0, 9,
    };

    setColor(walkedColor);

    ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
    vbb.order(ByteOrder.nativeOrder());
    mVertexBuffer = vbb.asIntBuffer();
    mVertexBuffer.put(vertices);
    mVertexBuffer.position(0);
    mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
    mIndexBuffer.put(indices);
    mIndexBuffer.position(0);
}



    public void draw(GL10 gl)
    {

        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);

        gl.glFrontFace(gl.GL_CW);
        gl.glVertexPointer(3, gl.GL_FIXED, 0, mVertexBuffer);
        gl.glEnable(GL10.GL_BLEND);
        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA) ;

        gl.glColor4f(Color.red(color) / 255.0f, Color.green(color) / 255.0f, Color.blue(color) / 255.0f, Color.alpha(color) / 255.0f);


        gl.glDrawElements(gl.GL_TRIANGLES, indices.length, gl.GL_UNSIGNED_BYTE, mIndexBuffer);

        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
    }

}
