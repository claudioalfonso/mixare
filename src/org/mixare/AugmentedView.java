package org.mixare;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

/**
 * Created  on 28.12.2015.
 */
class AugmentedView extends View {
	MixView app;
	int xSearch = 200;
	int ySearch = 10;
	int searchObjWidth = 0;
	int searchObjHeight = 0;

	Paint zoomPaint = new Paint();

	public AugmentedView(Context context) {
		super(context);

		try {
			app = (MixView) context;

			app.killOnError();
		} catch (Exception ex) {
			app.doError(ex, app.GENERAL_ERROR);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {

		try {
			// if (app.fError) {
			//
			// Paint errPaint = new Paint();
			// errPaint.setColor(Color.RED);
			// errPaint.setTextSize(16);
			//
			// /*Draws the Error code*/
			// canvas.drawText("ERROR: ", 10, 20, errPaint);
			// canvas.drawText("" + app.fErrorTxt, 10, 40, errPaint);
			//
			// return;
			// }

			app.killOnError();

			MixView.getdWindow().setWidth(canvas.getWidth());
			MixView.getdWindow().setHeight(canvas.getHeight());

			MixView.getdWindow().setCanvas(canvas);

			if (!MixView.getDataView().isInited()) {
				MixView.getDataView().init(MixView.getdWindow().getWidth(),
						MixView.getdWindow().getHeight());
			}
			if (app.isZoombarVisible()) {
				zoomPaint.setColor(Color.WHITE);
				zoomPaint.setTextSize(14);
				String startKM, endKM;
				endKM = "80km";
				startKM = "0km";
				/*
				 * if(MixListView.getDataSource().equals("Twitter")){ startKM =
				 * "1km"; }
				 */
				canvas.drawText(startKM, canvas.getWidth() / 100 * 4,
						canvas.getHeight() / 100 * 85, zoomPaint);
				canvas.drawText(endKM, canvas.getWidth() / 100 * 99 + 25,
						canvas.getHeight() / 100 * 85, zoomPaint);

				int height = canvas.getHeight() / 100 * 85;
				int zoomProgress = app.getZoomProgress();
				if (zoomProgress > 92 || zoomProgress < 6) {
					height = canvas.getHeight() / 100 * 80;
				}
				canvas.drawText(app.getZoomLevel(), (canvas.getWidth()) / 100
						* zoomProgress + 20, height, zoomPaint);
			}

			MixView.getDataView().draw(MixView.getdWindow());
		} catch (Exception ex) {
			app.doError(ex, MixView.GENERAL_ERROR);
		}
	}
}
