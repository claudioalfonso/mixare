package org.mixare;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.mixare.gui.Radar;
import org.mixare.lib.gui.PaintScreen;

/**
 * View which includes the RangeBar and the radar.
 */
public class RangeBarView extends RelativeLayout {

    private SeekBar rangeBar;

    private Radar radar = null;
    private PaintScreen radarPaintScreen;

    Paint rangeBarLabelPaint = new Paint();



    public RangeBarView(Context context){
        super(context);
        init();
    }

    public RangeBarView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }


    public RangeBarView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context,attrs,defStyleAttr);
        init();
    }

    /**
     * Calculate range base 80.
     * Mixare support ranges between 0-80km and default value of 5km,
     * {@link SeekBar SeekBar} on the other hand, is 0-100 base.
     * This method handles the range conversion between Mixare range and SeekBar progress.
     * The range resolution on the seek bar is fine in proximity and coarse in the distance.
     *
     * @return int Range base 80
     */
    public float calcRange(){

        int rangeBarProgress = rangeBar.getProgress();
        float range = 0;

        if (rangeBarProgress <= 26) {
            range = rangeBarProgress / 25f;
        } else if (25 < rangeBarProgress && rangeBarProgress < 50) {
            range = (1 + (rangeBarProgress - 25)) * 0.38f;
        } else if (25 == rangeBarProgress) {
            range = 1;
        } else if (50 == rangeBarProgress) {
            range = 10;
        } else if (50 < rangeBarProgress && rangeBarProgress < 75) {
            range = (10 + (rangeBarProgress - 50)) * 0.83f;
        } else {
            range = (30 + (rangeBarProgress - 75) * 2f);
        }
        return range;
    }

    public void init() {
        addView(inflate(getContext(), R.layout.rangebar_layer, null));

        radar = new Radar();
        radarPaintScreen = new PaintScreen();

        initRangeBar();
    }

    private void initRangeBar(){
        rangeBar = (SeekBar) this.findViewById(R.id.rangeBar);
        rangeBar.setOnSeekBarChangeListener(onRangeBarChangeListener);
        setRangeBarProgress(MixContext.getInstance().getSettings().getInt(getContext().getString(R.string.pref_rangeBarProgress), Config.DEFAULT_RANGE_PROGRESS),true);
    }


    /* ******** Operators - Sensors ****** */

    private SeekBar.OnSeekBarChangeListener onRangeBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        public void onProgressChanged(SeekBar rangeBar, int progress, boolean fromUser) {
            setRangeBarProgress(progress,false);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar rangeBar) {
            setRangeBarProgress(rangeBar.getProgress(),true);
            //repaint after range changed.
            MixContext.getInstance().getActualMixViewActivity().repaint();
            MixContext.getInstance().getActualMixViewActivity().refreshDownload();
            //Log.d(Config.TAG, "range="+calcRange()+", rangeBarProgress="+rangeBar.getProgress());
        }
    };

    public boolean isRangeBarVisible() {
        return rangeBar != null
                && rangeBar.getVisibility() == View.VISIBLE;
    }

    public void hideRangeBar(){
        if(isRangeBarVisible()){
            rangeBar.setVisibility(View.INVISIBLE);
        }
    }
    public void showRangeBar(){
        if(!isRangeBarVisible()){
            rangeBar.setVisibility(View.VISIBLE);
        }
    }

    public void setRangeBarProgress(int rangeBarProgress,boolean finalValue) {
        rangeBar.setProgress(rangeBarProgress); // set the visual state of the SeekBar
        float range = calcRange();
        MixViewDataHolder.getInstance().setRange(range); // save the calculated range in km to be accessed by other processes
        if(finalValue){
            MixContext.getInstance().getActualMixViewActivity().getMarkerRenderer().setRadius(range); // set radius of the renderer in KM, TODO remove and access global range from MixViewDataHolder
            SharedPreferences.Editor editor = MixContext.getInstance().getSettings().edit();

            // store the range of the range bar selected by the user
            editor.putInt(getContext().getString(R.string.pref_rangeBarProgress), rangeBarProgress);
            editor.apply(); //or commit()?
        }
    }

    public int getRangeBarProgress() {
        return rangeBar.getProgress();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        // Draw Radar
        radarPaintScreen.setCanvas(canvas);
        radar.paint(radarPaintScreen);

        try {
            if (isRangeBarVisible()) {
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
                int rangeBarProgress = getRangeBarProgress();
                if (rangeBarProgress > 92 || rangeBarProgress < 6) { // at beginning/end, jump up because of start/end labels
                    yPos = canvas.getHeight() / 100 * 80;
                }

                canvas.drawText(MixViewDataHolder.getInstance().getRangeString(), (canvas.getWidth()) / 100
                        * rangeBarProgress + 20, yPos, rangeBarLabelPaint);
            }
        } catch (Exception ex) {
            MixContext.getInstance().getActualMixViewActivity().doError(ex, MixViewActivity.GENERAL_ERROR);
        }
        super.dispatchDraw(canvas);
    }

}
