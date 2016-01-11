package org.mixare.gui;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.mixare.Config;
import org.mixare.MixContext;
import org.mixare.MixViewDataHolder;
import org.mixare.R;


public class HudView extends RelativeLayout {
    private TextView positionStatusText; // the textView on the HUD to show information about gpsPosition
    private TextView dataSourcesStatusText; // the textView on the HUD to show information about dataSources
    private TextView sensorsStatusText; // the textView on the HUD to show information about sensors

    private ProgressBar positionStatusProgress;
    private ProgressBar dataSourcesStatusProgress;
    private ProgressBar sensorsStatusProgress;
    private ImageView positionStatusIcon;
    private ImageView dataSourcesStatusIcon;
    private ImageView sensorsStatusIcon;
    private SeekBar rangeBar;

    public HudView(Context context){
        super(context);
        init();
    }

    public HudView(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }


    public HudView(Context context, AttributeSet attrs, int defStyleAttr){
        super(context,attrs,defStyleAttr);
        init();
    }

    /**
     * Calculate Range Level base 80.
     * Mixare support ranges between 0-80km and default value of 20km,
     * {@link SeekBar SeekBar} on the other hand, is 0-100 base.
     * This method handles the Range level conversion between Mixare range and SeekBar progress.
     *
     * @return int Range base 80
     */
    public float calcRange(){

        int rangeBarProgress = rangeBar.getProgress();
        float range = 5;

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
        addView(inflate(getContext(), R.layout.hud_view, null));
        positionStatusText =(TextView) this.findViewById(R.id.positionStatusText);
        dataSourcesStatusText =(TextView) this.findViewById(R.id.dataSourcesStatusText);
        sensorsStatusText =(TextView) this.findViewById(R.id.sensorsStatusText);
        positionStatusProgress = (ProgressBar) this.findViewById(R.id.positionStatusProgress);
        dataSourcesStatusProgress =(ProgressBar) this.findViewById(R.id.dataSourcesStatusProgress);
        sensorsStatusProgress =(ProgressBar) this.findViewById(R.id.sensorsStatusProgress);
        positionStatusIcon =(ImageView) this.findViewById(R.id.positionStatusIcon);
        dataSourcesStatusIcon =(ImageView) this.findViewById(R.id.dataSourcesStatusIcon);
        sensorsStatusIcon =(ImageView) this.findViewById(R.id.sensorsStatusIcon);
        initRangeBar();
    }

    private void initRangeBar(){
        rangeBar = (SeekBar) this.findViewById(R.id.rangeBar);
        rangeBar.setOnSeekBarChangeListener(onRangeBarChangeListener);
        SharedPreferences settings = getContext().getSharedPreferences(Config.PREFS_NAME, 0);
        setRangeBarProgress(settings.getInt(getContext().getString(R.string.pref_rangeLevel), Config.DEFAULT_RANGE),true);
    }

    public void setDataSourcesStatus(boolean working, boolean problem, String statusText){
        if(statusText!=null && dataSourcesStatusText !=null) {
            dataSourcesStatusText.setText(getResources().getString(R.string.dataSourcesStatusText));
        }
        if(dataSourcesStatusProgress !=null) {
            if(working) {
                dataSourcesStatusProgress.setVisibility(View.VISIBLE);
            } else {
                dataSourcesStatusProgress.setVisibility(View.INVISIBLE);
            }
        }
        if(dataSourcesStatusIcon !=null) {
            if(problem) {
                dataSourcesStatusIcon.setVisibility(View.VISIBLE);
            } else {
                dataSourcesStatusIcon.setVisibility(View.INVISIBLE);
            }
        }
    }

    public void updatePositionStatus(Location curFix){
        CharSequence relativeTime =  DateUtils.getRelativeTimeSpanString(curFix.getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        if(positionStatusText !=null) {
            positionStatusText.setText(getResources().getString(R.string.positionStatusText,curFix.getProvider(),curFix.getAccuracy(),relativeTime,curFix.getLatitude(),curFix.getLongitude(),curFix.getAltitude()));
        }
    }

    /* ******** Operators - Sensors ****** */

    private SeekBar.OnSeekBarChangeListener onRangeBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        public void onProgressChanged(SeekBar rangeBar, int progress,
                                      boolean fromUser) {
            //TODO change to only update label
            setRangeBarProgress(progress,false);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar rangeBar) {
            setRangeBarProgress(rangeBar.getProgress(),true);
            hideRangeBar();

            //repaint after range level changed.
            MixContext.getInstance().getActualMixViewActivity().repaint();
            MixContext.getInstance().getActualMixViewActivity().refreshDownload();
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
            SharedPreferences settings = getContext().getSharedPreferences(Config.PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();

            // store the range of the range bar selected by the user
            editor.putInt(getContext().getString(R.string.pref_rangeLevel), rangeBarProgress);
            editor.apply(); //or commit()?
        }
    }

    public int getRangeBarProgress() {
        return rangeBar.getProgress();
    }
}