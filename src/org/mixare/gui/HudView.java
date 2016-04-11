package org.mixare.gui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.mixare.Config;
import org.mixare.MixContext;
import org.mixare.MixViewActivity;
import org.mixare.MixViewDataHolder;
import org.mixare.R;
import org.mixare.lib.gui.PaintScreen;


public class HudView extends RelativeLayout {
    private TextView positionStatusText; // the TextView on the HUD to show information about gpsPosition
    private TextView dataSourcesStatusText; // the TextView on the HUD to show information about dataSources
    private TextView sensorsStatusText; // the TextView on the HUD to show information about sensors
    private TextView destinationStatusText; // the TextView on the HUD to show information about the currently selected destination to navigate to

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
        addView(inflate(getContext(), R.layout.hud_layer, null));

        positionStatusText =(TextView) this.findViewById(R.id.positionStatusText);
        dataSourcesStatusText =(TextView) this.findViewById(R.id.dataSourcesStatusText);
        sensorsStatusText =(TextView) this.findViewById(R.id.sensorsStatusText);
        positionStatusProgress = (ProgressBar) this.findViewById(R.id.positionStatusProgress);
        dataSourcesStatusProgress =(ProgressBar) this.findViewById(R.id.dataSourcesStatusProgress);
        sensorsStatusProgress =(ProgressBar) this.findViewById(R.id.sensorsStatusProgress);
        positionStatusIcon =(ImageView) this.findViewById(R.id.positionStatusIcon);
        dataSourcesStatusIcon =(ImageView) this.findViewById(R.id.dataSourcesStatusIcon);
        sensorsStatusIcon =(ImageView) this.findViewById(R.id.sensorsStatusIcon);
        destinationStatusText = (TextView) this.findViewById(R.id.destinationStatusText);

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

    private String formatLocation(Location location){
        CharSequence relativeTime =  DateUtils.getRelativeTimeSpanString(location.getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
        return getResources().getString(R.string.positionStatusText,location.getProvider(),location.getAccuracy(),relativeTime,location.getLatitude(),location.getLongitude(),location.getAltitude());
    }

    public void updatePositionStatus(Location curFix){
        if(positionStatusText !=null) {
            positionStatusText.setText(formatLocation(curFix));
        }
    }

    /* ******** Operators - Sensors ****** */


    public void setDestinationStatus(Location destination) {
        if(destination!=null && destinationStatusText !=null) {
            destinationStatusText.setText(formatLocation(destination));
        }
    }
}