package com.school.vacationplanner;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.school.vacationplanner.util.NotificationUtility;

public class SettingsActivity extends AppCompatActivity {

    // constants
    private static final String TAG = "SettingsActivity";


    // inner classes
    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        // constants
        private static final String TAG = "SettingsFragment";


        // override methods
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            Log.d(TAG, "onCreatePreferences: Called with rootKey: " + rootKey);
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.d(TAG, "onCreatePreferences: Preference changed: " + key);
            if (key.equals("notifications_enabled")) {
                boolean isEnabled = sharedPreferences.getBoolean(key, true);
                Log.d(TAG, "onCreatePreferences: notifications_enabled changed to: " + isEnabled);
                NotificationUtility.setNotificationsEnabled(isEnabled);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.d(TAG, "onResume: Called");
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            Log.d(TAG, "onPause: Called");
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }


    // override methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: Activity created");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            Log.d(TAG, "onCreate: Initializing SettingsFragment");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            Log.d(TAG, "onCreate: Enabling ActionBar home button");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
}