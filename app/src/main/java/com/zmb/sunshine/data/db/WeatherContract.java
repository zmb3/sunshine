package com.zmb.sunshine.data.db;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Contains database and column names used for storing ewather data.
 */
public class WeatherContract {

    /**
     * We use these integer codes in our database to represent different
     * weather conditions.  These codes map to images that are displayed.
     */
    public static enum WeatherId {
        STORM(0),
        LIGHT_RAIN(1),
        RAIN(2),
        SNOW(3),
        FOG(4),
        CLEAR(5),
        LIGHT_CLOUDS(6),
        CLOUDS(7);

        public final int mValue;

        private WeatherId(int value) {
            mValue = value;
        }

        public static WeatherId fromInt(int value) {
            switch (value) {
                case 0: return STORM;
                case 1: return LIGHT_RAIN;
                case 2: return RAIN;
                case 3: return SNOW;
                case 4: return FOG;
                case 5: return CLEAR;
                case 6: return LIGHT_CLOUDS;
                case 7: return CLOUDS;
                default: throw new IllegalArgumentException();
            }
        }
    }

    /**
     * A name for the content provider.
     */
    public static final String CONTENT_AUTHORITY = "com.zmb.sunshine";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // paths that we append to the base URI
    public static final String PATH_WEATHER = "weather";
    public static final String PATH_LOCATION = "location";

    private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMdd");

    /**
     * Convert a {@link java.util.Date} into a string that's compatible
     * with the way we store dates (yyyyMMdd).
     * @param date the date to convert
     * @return the date expressed as a string
     */
    public static String convertDateToString(Date date) {
        return sDateFormat.format(date);
    }

    public static Date convertStringToDate(String date) {
        try {
            return sDateFormat.parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Defines the contents of the weather table.
     */
    public static final class WeatherEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();

        // MIME types that indicate whether we return multiple items (dir) or a single item
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;

        public static Uri buildWeatherUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildWeatherLocation(String locationSetting) {
            return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
        }

        public static Uri buildWeatherLocationWithDate(String location, String date) {
            return CONTENT_URI.buildUpon().appendPath(location).appendPath(date).build();
        }

        public static Uri buildWeatherLocationWithStartDate(String location, String date) {
            return CONTENT_URI.buildUpon().appendPath(location)
                    .appendQueryParameter(COLUMN_DATETEXT, date).build();
        }

        public static Uri buildWeatherLocatinWithStartAndEndDate(String location, String start, String end) {
            return CONTENT_URI.buildUpon().appendPath(location)
                    .appendQueryParameter(COLUMN_DATETEXT, start)
                    .appendQueryParameter(QUERY_PARAM_END_DATE, end).build();
        }

        public static String getLocationSettingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getPathSegments().get(2);
        }

        public static String getStartDateFromUri(Uri uri) {
            return uri.getQueryParameter(COLUMN_DATETEXT);
        }

        public static String getEndDateFromUri(Uri uri) {
            return uri.getQueryParameter(QUERY_PARAM_END_DATE);
        }

        public static final String TABLE_NAME = "weather";

        /**
         * A foreign key into the location table.
         */
        public static final String COLUMN_LOC_KEY = "location_id";

        /**
         * The date, stored as text of the form yyyyMMdd.
         */
        public static final String COLUMN_DATETEXT = "date";

        public static final String QUERY_PARAM_END_DATE = "end_date";

        public static final String COLUMN_WEATHER_ID = "weather_id";

        /**
         * A short description of the weather conditions.
         */
        public static final String COLUMN_SHORT_DESCRIPTION = "short_desc";

        /**
         * Temperature is stored in degrees Celsius.
         */
        public static final String COLUMN_TEMPERATURE_HIGH = "max";
        /**
         * Temperature is stored in degrees Celsius.
         */
        public static final String COLUMN_TEMPERATURE_LOW = "min";

        public static final String COLUMN_HUMIDITY = "humidity";

        /**
         * Pressure is measured in units of hPa.
         */
        public static final String COLUMN_PRESSURE = "pressure";

        /**
         * Wind speed is measured in meters per second.
         */
        public static final String COLUMN_WIND_SPEED = "wind";

        public static final String COLUMN_DEGREES = "degrees";
    }

    /**
     * Defines the contents of the location table.
     */
    public static final class LocationEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

        // MIME types that indicate whether we return multiple items (dir) or a single item
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

        public static Uri buildLocationUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String TABLE_NAME = "location";

        /**
         * The location setting that is sent as part of the weather query.
         */
        public static final String COLUMN_LOCATION_SETTING = "location_setting";

        /**
         * Human-readable location string provided by the API.
         */
        public static final String COLUMN_CITY_NAME = "city_name";

        public static final String COLUMN_LATITUDE = "latitude";

        public static final String COLUMN_LONGITUDE = "longitude";
    }
}
