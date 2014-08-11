package com.zmb.sunshine.data.db;

import android.provider.BaseColumns;

/**
 * Contains database and column names used for storing ewather data.
 */
public class WeatherContract {

    /**
     * Defines the contents of the weather table.
     */
    public static final class WeatherEntry implements BaseColumns {

        public static final String TABLE_NAME = "weather";

        /**
         * A foreign key into the location table.
         */
        public static final String COLUMN_LOC_KEY = "location_id";

        /**
         * The date, stored as text of the form yyyy-MM-dd.
         */
        public static final String COLUMN_DATETEXT = "date";

        public static final String COLUMN_WEATHER_ID = "weather_id";

        /**
         * A short description of the weather conditions.
         */
        public static final String COLUMN_SHORT_DESCRIPTION = "short_desc";

        public static final String COLUMN_TEMPERATURE_HIGH = "max";

        public static final String COLUMN_TEMPERATURE_LOW = "min";

        public static final String COLUMN_HUMIDITY = "humidity";

        public static final String COLUMN_PRESSURE = "pressure";

        public static final String COLUMN_WIND_SPEED = "wind";

        public static final String COLUMN_DEGREES = "degrees";
    }

    /**
     * Defines the contents of the location table.
     */
    public static final class LocationEntry implements BaseColumns {
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
