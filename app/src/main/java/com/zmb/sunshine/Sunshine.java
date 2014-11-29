package com.zmb.sunshine;


import android.content.Context;
import android.preference.PreferenceManager;

import com.zmb.sunshine.data.Convert;
import com.zmb.sunshine.data.db.WeatherContract;

import java.text.DateFormat;

public class Sunshine {

    private Sunshine() { }

    public static String formatTemperature(double tempMetric, boolean isMetric) {
        if (!isMetric) {
            tempMetric = Convert.toFahrenheit(tempMetric);
        }
        return String.format("%.0f", tempMetric);
    }

    public static String formatDate(String dateText) {
        return DateFormat.getDateInstance().format(WeatherContract.convertStringToDate(dateText));
    }

    public static String getPreferredLocation(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getString(
                c.getString(R.string.pref_location_key),
                c.getString(R.string.pref_location_default));
    }

    public static boolean isMetric(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getString(
                c.getString(R.string.pref_units_key),
                c.getString(R.string.pref_units_metric))
                .equals(c.getString(R.string.pref_units_metric));
    }

}
