package com.zmb.sunshine;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.zmb.sunshine.data.db.WeatherContract;
import com.zmb.sunshine.sync.SunshineSyncAdapter;
import com.zmb.sunshine.widget.SunshineWidget;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    boolean mIsBindingPreference = false;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load preferences from XML resource
        addPreferencesFromResource(R.xml.preferences);

        // attach listeners to each preference
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_units_key)));
    }


    /**
     * Attaches a listener so the summary is always updated with the
     * preference value.  Also fires the listener once to initialize
     * the summary (so it shows up before the value is changed).
     * @param preference
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        try {
            mIsBindingPreference = true;

            preference.setOnPreferenceChangeListener(this);

            // trigger the listener immediately
            onPreferenceChange(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), ""));
        } finally {
            mIsBindingPreference = false;
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {

        // if we're not just starting up and binding the preferences
        if (!mIsBindingPreference) {
            // if the location setting has changed, we need to fetch new data
            if (preference.getKey().equals(getString(R.string.pref_location_key))) {
                SunshineSyncAdapter.syncNow(getActivity());
            } else {

                // weather data may need to be updated (ie units changed)
                getActivity().getContentResolver().notifyChange(
                        WeatherContract.WeatherEntry.CONTENT_URI, null);

                if (preference.getKey().equals(getString(R.string.pref_units_key))) {
                    // we have to explicitly pass the "isMetric" parameter, since
                    // the preference hasn't actually changed yet
                    // (Sunshine.isMetric() won't return the new value)
                    boolean isMetric = value.toString().equals(getString(R.string.pref_units_metric));
                    SunshineWidget.updateAllWidgets(getActivity(), isMetric);
                }
            }
        }



        if (preference instanceof ListPreference) {
            // for ListPreferences, look up the correct display value
            // in the preference's 'entries' list
            ListPreference list = (ListPreference) preference;
            int index = list.findIndexOfValue(value.toString());
            if (index >= 0) {
                preference.setSummary(list.getEntries()[index]);
            }
        } else {
            // for all other preferences, set the summary to the value
            preference.setSummary(value.toString());
        }
        return true;
    }
}
