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

	Paint rangeBarLabelPaint = new Paint();

	public AugmentedView(Context context) {
		super(context);

		try {
			mixViewActivity = (MixViewActivity) context;

			mixViewActivity.killOnError();
		} catch (Exception ex) {
			mixViewActivity.doError(ex, MixViewActivity.GENERAL_ERROR);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {

		try {
			mixViewActivity.killOnError();

			MixViewActivity.getPaintScreen().setWidth(canvas.getWidth());
			MixViewActivity.getPaintScreen().setHeight(canvas.getHeight());

			MixViewActivity.getPaintScreen().setCanvas(canvas);

			if (!mixViewActivity.getMarkerRenderer().isInited()) {
                mixViewActivity.getMarkerRenderer().init(MixViewActivity.getPaintScreen().getWidth(),
						MixViewActivity.getPaintScreen().getHeight());
			}
			if (mixViewActivity.hudView.isRangeBarVisible()) {
				rangeBarLabelPaint.setColor(Color.WHITE);
				rangeBarLabelPaint.setTextSize(14);
				String startKM, endKM;
				endKM = "80km";
				startKM = "0km";

				canvas.drawText(startKM, canvas.getWidth() / 100 * 4,
						canvas.getHeight() / 100 * 85, rangeBarLabelPaint);
				canvas.drawText(endKM, canvas.getWidth() / 100 * 99 + 25,
						canvas.getHeight() / 100 * 85, rangeBarLabelPaint);

				int yPos = canvas.getHeight() / 100 * 85;
				int rangeBarProgress = mixViewActivity.hudView.getRangeBarProgress();
				if (rangeBarProgress > 92 || rangeBarProgress < 6) { // at beginning/end, jump up because of ster/end labels
					yPos = canvas.getHeight() / 100 * 80;
				}

				canvas.drawText(MixViewDataHolder.getInstance().getRangeString(), (canvas.getWidth()) / 100
						* rangeBarProgress + 20, yPos, rangeBarLabelPaint);
			}

            mixViewActivity.getMarkerRenderer().draw(MixViewActivity.getPaintScreen());
		} catch (Exception ex) {
			mixViewActivity.doError(ex, MixViewActivity.GENERAL_ERROR);
		}
	}
}
