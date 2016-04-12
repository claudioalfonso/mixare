package org.mixare.gui.opengl;

import android.graphics.Color;

import org.mixare.MixContext;
import org.mixare.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by MelanieW on 11.04.2016.
 */
public class Triangle {

    private FloatBuffer rectVerticesBuffer;
    private ShortBuffer trianglesBuffer;


        private short[] indices;

    private int color = Color.argb(128, 255, 0, 255); //128, 51, 153, 255= light blue
    private static final int alpha=Color.argb(128, 0, 0, 0);

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = (0x00ffffff & color) | alpha; //remove alpha from argb color, then combine rgb with alpha
        }

        public  Triangle(){}
        public Triangle(RouteSegment previousRouteSegment, RouteSegment currentRouteSegment)
        {


            String walkedRouteColorString = MixContext.getInstance().getSettings().getString(MixContext.getInstance().getString(R.string.pref_item_walkedroutecolor_key), MixContext.getInstance().getString(R.string.color_hint2));;
            int walkedColor = Color.parseColor(walkedRouteColorString);

            float[] verticesBoth = {
                    previousRouteSegment.getLeftStartVector().getXCoordinate(), previousRouteSegment.getLeftStartVector().getYCoordinate(),0 , //0
                    currentRouteSegment.getLeftStartVector().getXCoordinate(), currentRouteSegment.getLeftStartVector().getYCoordinate(),0 , //0
                    0,0,0 , //0
                    previousRouteSegment.getRightStartVector().getXCoordinate(), previousRouteSegment.getRightStartVector().getYCoordinate(),0 , //0
                    currentRouteSegment.getRightStartVector().getXCoordinate(), currentRouteSegment.getRightStartVector().getYCoordinate(),0  //0
            };

            indices = new short[] {
                    0,1,2
                    ,2,3,4
            };

            trianglesBuffer = getDirectShortBuffer(indices);

            rectVerticesBuffer = getDirectFloatBuffer(verticesBoth);

           // setColor(walkedColor);
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

        public void draw(GL10 gl)
        {

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0,
                    rectVerticesBuffer);

            gl.glColor4f(Color.red(color) / 255.0f, Color.green(color) / 255.0f, Color.blue(color) / 255.0f, Color.alpha(color) / 255.0f);

            gl.glDrawElements(GL10.GL_TRIANGLES, indices.length,
                    GL10.GL_UNSIGNED_SHORT, trianglesBuffer);

            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        }


}
