package fr.turfu.urbapp2.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import fr.turfu.urbapp2.R;


public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from the XML resource
        addPreferencesFromResource(R.xml.settings);

    }
}