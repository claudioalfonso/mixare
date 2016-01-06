package org.mixare.gui;

import android.content.Context;
import android.location.Location;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
}
