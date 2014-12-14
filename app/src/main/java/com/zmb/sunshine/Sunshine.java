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
     * @param weatherId the OpenWeatherMap weather ID
     * @return the resource id, or -1 if no match is found
     */
    public static int getArtForWeatherId(int weatherId) {
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        // in the future when we do away with weatherId this will have to get smarter
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_rain;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }

    /**
     * Get the icon resource ID for the specified weather ID.
     * @param weatherId the OpenWeatherMap weather ID
     * @return the resource ID, or -1 if no match is found
     */
    public static int getIconForWeatherId(int weatherId) {
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
        } else if (weatherId == 511) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.ic_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
        } else if (weatherId == 800) {
            return R.drawable.ic_clear;
        } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
        }
        return -1;
    }

}
