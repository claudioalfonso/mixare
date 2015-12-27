/*
 * Copyright (C) 2010- Peer internet solutions
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare.gui;

import org.mixare.DataView;
import org.mixare.MixContext;
import org.mixare.R;
import org.mixare.lib.MixUtils;
import org.mixare.lib.gui.ScreenLine;
import org.mixare.lib.marker.Marker;
import org.mixare.lib.gui.PaintScreen;
import org.mixare.lib.gui.ScreenObj;
import org.mixare.data.DataHandler;
import org.mixare.lib.render.Camera;

import android.graphics.Color;

/** Takes care of the small radar in the top left corner and of its points
 * @author daniele
 *
 */
public class Radar implements ScreenObj {
    /** current context */
    private MixContext mixContext;
	/** The screen */
	public DataView view;

    private RadarPoints radarPoints = null;


    private PaintScreen dw=null;
	/** The radar's range */
	float range;
	/** Radius in pixel on screen */
	public static float RADIUS = 40;
	/** Position on screen */
	static float originX = 10 , originY = 20;
	/** Color */
	static int radarColor = Color.argb(100, 0, 0, 200);

    private ScreenLine lrl = new ScreenLine();
    private ScreenLine rrl = new ScreenLine();

    private String[] directions;

    private float rx = 10, ry = 20;

    public Radar(MixContext ctx, DataView view){
        this.mixContext = ctx;
        this.view=view;
        directions = new String[8];
        directions[0] = mixContext.getString(R.string.N);
        directions[1] = mixContext.getString(R.string.NE);
        directions[2] = mixContext.getString(R.string.E);
        directions[3] = mixContext.getString(R.string.SE);
        directions[4] = mixContext.getString(R.string.S);
        directions[5] = mixContext.getString(R.string.SW);
        directions[6] = mixContext.getString(R.string.W);
        directions[7] = mixContext.getString(R.string.NW);
        radarPoints = new RadarPoints(this.view);

    }

    public void paint(PaintScreen paintScreen) {
        this.dw=paintScreen;
		/** radius is in KM. */
		range = view.getRadius() * 1000;
        int bearing = (int) view.getState().getCurBearing();

        /** Draw the radar */
		dw.setFill(true);
		dw.setColor(radarColor);
		dw.paintCircle(originX + RADIUS, originY + RADIUS, RADIUS);

		/** put the markers in it */
        dw.paintObj(radarPoints, rx, ry, -bearing, 1);


        String dirTxt = "";
        int range = (int) (bearing / (360f / 16f));
        if (range == 15 || range == 0)
            dirTxt = directions[0];
        else if (range == 1 || range == 2)
            dirTxt = directions[1];
        else if (range == 3 || range == 4)
            dirTxt = directions[2];
        else if (range == 5 || range == 6)
            dirTxt = directions[3];
        else if (range == 7 || range == 8)
            dirTxt = directions[4];
        else if (range == 9 || range == 10)
            dirTxt = directions[5];
        else if (range == 11 || range == 12)
            dirTxt = directions[6];
        else if (range == 13 || range == 14)
            dirTxt = directions[7];


 //       dw.paintObj(this, rx, ry, -bearing, 1);
        dw.setFill(false);
        dw.setColor(Color.argb(150, 0, 0, 220));
        dw.paintLine(lrl.x, lrl.y, rx + RADIUS, ry
                + RADIUS);
        dw.paintLine(rrl.x, rrl.y, rx + RADIUS, ry
                + RADIUS);
        dw.setColor(Color.rgb(255, 255, 255));
        dw.setFontSize(12);

        radarText(dw, MixUtils.formatDist(view.getRadius() * 1000), rx
                + RADIUS, ry + RADIUS * 2 - 10, false);
        radarText(dw, "" + bearing + ((char) 176) + " " + dirTxt, rx
                + RADIUS, ry - 5, true);

        lrl.set(0, -RADIUS);
        lrl.rotate(Camera.DEFAULT_VIEW_ANGLE / 2);
        lrl.add(rx + RADIUS, ry + RADIUS);
        rrl.set(0, -RADIUS);
        rrl.rotate(-Camera.DEFAULT_VIEW_ANGLE / 2);
        rrl.add(rx + RADIUS, ry + RADIUS);
	}

    private void radarText(PaintScreen dw, String txt, float x, float y,
                           boolean bg) {
        float padw = 4, padh = 2;
        float w = dw.getTextWidth(txt) + padw * 2;
        float h = dw.getTextAsc() + dw.getTextDesc() + padh * 2;
        if (bg) {
            dw.setColor(Color.rgb(0, 0, 0));
            dw.setFill(true);
            dw.paintRect(x - w / 2, y - h / 2, w, h);
            dw.setColor(Color.rgb(255, 255, 255));
            dw.setFill(false);
            dw.paintRect(x - w / 2, y - h / 2, w, h);
        }
        dw.paintText(padw + x - w / 2, padh + dw.getTextAsc() + y - h / 2, txt,
                false);
    }



	/** Width on screen */
	public float getWidth() {
		return RADIUS * 2;
	}

	/** Height on screen */
	public float getHeight() {
		return RADIUS * 2;
	}
}

