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

    /**
     * Format a temperature for display.
     * @param context
     * @param temperature the temperature, ALWAYS METRIC
     * @param isMetric indicates whether the app is set to display metric units
     * @return
     */
    public static String formatTemperature(Context context, double temperature, boolean isMetric) {
        if (!isMetric) {
            temperature = Convert.toFahrenheit(temperature);
        }
        return context.getString(R.string.format_temperature, temperature);
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

    public static String shortFriendlyDate(Context context, String dateText) {
        String day = dayName(context, dateText);

        // for today and tomorrow, we still return today and tomorrow
        if (day.equals(context.getString(R.string.today)) ||
                day.equals(context.getString(R.string.tomorrow))) {
            return day;
        }
        // for other days, just return the first 3 letters of the day
        // ie: "Sun", "Mon", etc.
        return day.substring(0, 3);
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

    public static String formatWind(Context c, float speed, float degrees) {
        int windFormat;
        if (isMetric(c)) {
            windFormat = R.string.format_wind_kmh;
        } else {
            windFormat = R.string.format_wind_mph;
            speed *= 0.621371192237334f;
        }
        return String.format(c.getString(windFormat), speed, directionFromDegrees(degrees));
    }

    private static String directionFromDegrees(float degrees) {
        if (degrees >= 337.5 || degrees < 22.5) {
            return "N";
        } else if (degrees >= 22.5 && degrees < 67.5) {
            return "NE";
        } else if (degrees >= 67.5 && degrees < 112.5) {
            return "E";
        } else if (degrees >= 112.5 && degrees < 157.5) {
            return "SE";
        } else if (degrees >= 157.5 && degrees < 202.5) {
            return"S";
        } else if (degrees >= 202.5 && degrees < 247.5) {
            return"SW";
        } else if (degrees >= 247.5 && degrees < 292.5) {
            return "W";
        } else if (degrees >= 292.5 || degrees < 22.5) {
            return "NW";
        } else {
            return "Unknown";
        }
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

    /**
     * Get the art resource ID for the specified weather condition.
     * @param weatherId the weather ID
     * @return the resource id, or -1 if no match is found
     */
    public static int getArtForWeatherId(WeatherContract.WeatherId weatherId) {
        switch (weatherId) {
            case CLOUDS:
                return R.drawable.art_clouds;
            case LIGHT_CLOUDS:
                return R.drawable.art_light_clouds;
            case CLEAR:
                return R.drawable.art_clear;
            case FOG:
                return R.drawable.art_fog;
            case LIGHT_RAIN:
                return R.drawable.art_light_rain;
            case RAIN:
                return R.drawable.art_rain;
            case SNOW:
                return R.drawable.art_snow;
            case STORM:
                return R.drawable.art_storm;
        }
        return -1;
    }

    /**
     * Get the icon resource ID for the specified weather ID.
     * @param weatherId the weather ID
     * @return the resource ID, or -1 if no match is found
     */
    public static int getIconForWeatherId(WeatherContract.WeatherId weatherId) {
        switch (weatherId) {
            case CLOUDS:
                return R.drawable.ic_cloudy;
            case LIGHT_CLOUDS:
                return R.drawable.ic_light_clouds;
            case CLEAR:
                return R.drawable.ic_clear;
            case FOG:
                return R.drawable.ic_fog;
            case LIGHT_RAIN:
                return R.drawable.ic_light_rain;
            case RAIN:
                return R.drawable.ic_rain;
            case SNOW:
                return R.drawable.ic_snow;
            case STORM:
                return R.drawable.ic_storm;
        }
        return -1;
    }

}
