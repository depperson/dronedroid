package com.d33z.android.dronedroid;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * This shows the preferences page defined by preferences.xml
 */
public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    final String tag = "PreferencesActivity";

    // TODO: validate input or don't crash when a float comes in for an int preference
    // http://www.dotnetperls.com/parseint

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        //Log.d(tag, "onSharedPreferenceChanged() called");
    }
}
