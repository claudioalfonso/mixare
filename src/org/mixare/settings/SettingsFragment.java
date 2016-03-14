package org.mixare.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.mixare.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // load settings view from XML file
        addPreferencesFromResource(R.xml.settings);
    }
}
