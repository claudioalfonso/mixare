package org.mixare.settings;

import android.annotation.TargetApi;
import android.content.Context;
import android.location.Location;
import android.os.Build;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.mapsforge.core.util.LatLongUtils;
import org.mixare.Config;
import org.mixare.R;

public class LocationPreference extends EditTextPreference implements View.OnLongClickListener{
    public LocationPreference(Context context) {
        super(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LocationPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public LocationPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LocationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(ViewGroup parent){
        ((ListView) parent).setOnItemLongClickListener(new ListView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                ListAdapter listAdapter = listView.getAdapter();
                Object listObj = listAdapter.getItem(position);
                if (listObj != null && listObj instanceof View.OnLongClickListener) {
                    View.OnLongClickListener longClickListener = (View.OnLongClickListener) listObj;
                    return longClickListener.onLongClick(view);
                }
                return false;
            }
        });

        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
              @Override
              public boolean onPreferenceChange(Preference preference, Object o) {
                  String newValue = (String) o;
                  try {
                      Config.parseLocationFromString(newValue);
                  } catch (IllegalArgumentException ex){
                      Log.d(Config.TAG, "onPreferenceChange - not valid - not changed", ex);
                      return false;
                  }
                  setSummary(getText());
                  return true;
              }
          }
        );

        return super.onCreateView(parent);
    }

    @Override
    public boolean onLongClick(View view) {
        setSummary("reset");
        return true;
    }

}
