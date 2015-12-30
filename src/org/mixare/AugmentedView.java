package org.mixare;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created  on 28.12.2015.
 */
class AugmentedView extends View {
	MixViewActivity mixViewActivity;
	int xSearch = 200;
	int ySearch = 10;
	int searchObjWidth = 0;
	int searchObjHeight = 0;

	Paint rangeBarLabelPaint = new Paint();

	public AugmentedView(Context context) {
		super(context);

		try {
			mixViewActivity = (MixViewActivity) context;

			mixViewActivity.killOnError();
		} catch (Exception ex) {
			mixViewActivity.doError(ex, mixViewActivity.GENERAL_ERROR);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {

		try {
			// if (mixViewActivity.fError) {
			//
			// Paint errPaint = new Paint();
			// errPaint.setColor(Color.RED);
			// errPaint.setTextSize(16);
			//
			// /*Draws the Error code*/
			// canvas.drawText("ERROR: ", 10, 20, errPaint);
			// canvas.drawText("" + mixViewActivity.fErrorTxt, 10, 40, errPaint);
			//
			// return;
			// }

			mixViewActivity.killOnError();

			MixViewActivity.getPaintScreen().setWidth(canvas.getWidth());
			MixViewActivity.getPaintScreen().setHeight(canvas.getHeight());

			MixViewActivity.getPaintScreen().setCanvas(canvas);

			if (!MixViewActivity.getMarkerRenderer().isInited()) {
				MixViewActivity.getMarkerRenderer().init(MixViewActivity.getPaintScreen().getWidth(),
						MixViewActivity.getPaintScreen().getHeight());
			}
			if (mixViewActivity.isRangeBarVisible()) {
				rangeBarLabelPaint.setColor(Color.WHITE);
				rangeBarLabelPaint.setTextSize(14);
				String startKM, endKM;
				endKM = "80km";
				startKM = "0km";
				/*
				 * if(MixListView.getDataSource().equals("Twitter")){ startKM =
				 * "1km"; }
				 */
				canvas.drawText(startKM, canvas.getWidth() / 100 * 4,
						canvas.getHeight() / 100 * 85, rangeBarLabelPaint);
				canvas.drawText(endKM, canvas.getWidth() / 100 * 99 + 25,
						canvas.getHeight() / 100 * 85, rangeBarLabelPaint);

				int height = canvas.getHeight() / 100 * 85;
				int rangeBarProgress = mixViewActivity.getRangeBarProgress();
				if (rangeBarProgress > 92 || rangeBarProgress < 6) {
					height = canvas.getHeight() / 100 * 80;
				}
				canvas.drawText(mixViewActivity.getRangeLevel(), (canvas.getWidth()) / 100
						* rangeBarProgress + 20, height, rangeBarLabelPaint);
			}

			MixViewActivity.getMarkerRenderer().draw(MixViewActivity.getPaintScreen());
		} catch (Exception ex) {
			mixViewActivity.doError(ex, MixViewActivity.GENERAL_ERROR);
		}
	}
}
