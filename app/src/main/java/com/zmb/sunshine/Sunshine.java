package com.zmb.sunshine;

import android.content.Context;
import android.preference.PreferenceManager;

import com.zmb.sunshine.data.Convert;
import com.zmb.sunshine.data.db.WeatherContract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Sunshine {

    private Sunshine() { }

    public static final String DEGREE_SYMBOL = "\u00B0";

    private static final SimpleDateFormat sDatabaseFormat =
            new SimpleDateFormat("yyyyMMdd");

    private static final SimpleDateFormat sDayFormat =
            new SimpleDateFormat("EEEE");

    private static final SimpleDateFormat sMmonthDayFormat =
            new SimpleDateFormat("MMMM dd");

    private static final SimpleDateFormat sShortDateFormat =
            new SimpleDateFormat("EEE MMM dd");

    public static String formatTemperature(Context context, double tempMetric, boolean isMetric) {
        if (!isMetric) {
            tempMetric = Convert.toFahrenheit(tempMetric);
        }
        return context.getString(R.string.format_temperature, tempMetric);
    }

    public static String formatDate(String dateText) {
        return DateFormat.getDateInstance().format(WeatherContract.convertStringToDate(dateText));
    }

    public static String friendlyDate(Context context, String dateText) {
        Date today = new Date();
        String todayString = WeatherContract.convertDateToString(today);
        Date input = WeatherContract.convertStringToDate(dateText);

        // 1) check if date is today
        if (todayString.equals(dateText)) {
            // format: "Today, June 24"
            String result = context.getString(R.string.today);
            return context.getString(R.string.format_full_friendly_date,
                    result, formattedMonthDay(context, dateText));
        }

        // 2) check if date is this week
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        cal.add(Calendar.DATE, 7);
        String nextWeekString = WeatherContract.convertDateToString(cal.getTime());

        if (dateText.compareTo(nextWeekString) < 0) {
            // format: "Tuesday"
            return dayName(context, dateText);
        }

        // 3) future date
        return  sShortDateFormat.format(input);
    }

    public static String formattedMonthDay(Context context, String date) {
        try {
            Date input = sDatabaseFormat.parse(date);
            return sMmonthDayFormat.format(input);
        } catch (ParseException e) {
            return "";
        }
    }

    public static String dayName(Context context, String dateString) {
        Date input = WeatherContract.convertStringToDate(dateString);
        Date today = new Date();
        String todayString =WeatherContract.convertDateToString(today);

        // check if the date is today or tomorrow
        if (dateString.equals(todayString)) {
            return context.getString(R.string.today);
        }
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        String tomorrowString = WeatherContract.convertDateToString(cal.getTime());
        if (dateString.equals(tomorrowString)) {
            return context.getString(R.string.tomorrow);
        }

        // if not today or tomorrow, then the day name will do
        return sDayFormat.format(input);
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
