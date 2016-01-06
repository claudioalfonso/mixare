package org.mixare.gui;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.mixare.Config;
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
    }

    public void setDataSourcesActivity(boolean working, boolean problem, String statusText){
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


    /**
     * Create range bar and returns FrameLayout. FrameLayout is created to be
     * hidden and not added to markerRenderer, Caller needs to add the frameLayout to
     * markerRenderer, and enable visibility when needed.
     *
     * @param settings where setting is stored
     * @return FrameLayout Hidden Range Bar
     */
    private FrameLayout createRangeBar(SharedPreferences settings) {
        SeekBar rangeBar=new SeekBar(getContext());
        rangeBar.setMax(100);
        rangeBar.setProgress(settings.getInt(getContext().getString(R.string.pref_rangeLevel), 65));
     //   rangeBar.setOnSeekBarChangeListener(onRangeBarChangeListener);
        rangeBar.setVisibility(View.INVISIBLE);
      //  getMixViewData().setRangeBar(rangeBar);

        FrameLayout frameLayout = new FrameLayout(getContext());

        frameLayout.setMinimumWidth(3000);
        ViewGroup.LayoutParams pa = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        frameLayout.setLayoutParams(pa);
        frameLayout.addView(rangeBar);
        frameLayout.setPadding(10, 0, 10, 10);

        return frameLayout;
    }

    /* ******** Operators - Sensors ****** */
    /*
    private SeekBar.OnSeekBarChangeListener onRangeBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        public void onProgressChanged(SeekBar rangeBar, int progress,
                                      boolean fromUser) {
            float rangeLevel = calcRangeLevel();

            getMixViewData().setRangeLevel(String.valueOf(rangeLevel));
            getMixViewData().setRangeBarProgress(progress);

            markerRenderer.getContext().getNotificationManager().
                    addNotification("Radius: " + String.valueOf(rangeLevel));
        }

        public void onStartTrackingTouch(SeekBar rangeBar) {
            markerRenderer.getContext().getNotificationManager().addNotification("Radius: ");
        }

        public void onStopTrackingTouch(SeekBar rangeBar) {
            SharedPreferences settings = getContext().getSharedPreferences(Config.PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
			// store the range of the range bar selected by the user
            editor.putInt(getContext().getString(R.string.pref_rangeLevel), rangeBar.getProgress());
            editor.commit();
            getMixViewData().getRangeBar().setVisibility(View.INVISIBLE);
            // rangeChanging= false;

            getMixViewData().getRangeBar().setProgress(rangeBar.getProgress());

            markerRenderer.getContext().getNotificationManager().clear();
            //repaint after range level changed.
            repaint();
            setRangeLevel();
            refreshDownload();

        }


    };
*/
}
