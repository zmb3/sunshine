package com.zmb.sunshine.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.zmb.sunshine.data.db.WeatherContract;

public abstract class AWeatherDataParser implements IWeatherDataParser {
    /**
     * Insert a location into the database if it doesn't already exist.
     *
     * @param locationSetting
     * @param cityName
     * @param lat
     * @param lon
     * @return the row ID of the specified location
     */
    protected long addLocation(Context c, String locationSetting, String cityName, double lat, double lon) {
        ContentResolver cr = c.getContentResolver();
        Cursor cursor = cr.query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[] { WeatherContract.LocationEntry._ID },
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[] { locationSetting },
                null);
        try {
            if (cursor.moveToFirst()) {
                // the location was already in the database
                int locationId = cursor.getColumnIndex(WeatherContract.LocationEntry._ID);
                return cursor.getLong(locationId);
            } else {
                // location wasn't in database, must be added
                ContentValues values = new ContentValues();
                values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
                values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
                values.put(WeatherContract.LocationEntry.COLUMN_LATITUDE, lat);
                values.put(WeatherContract.LocationEntry.COLUMN_LONGITUDE,lon);
                Uri uri = cr.insert(WeatherContract.LocationEntry.CONTENT_URI, values);
                return ContentUris.parseId(uri);
            }
        } finally {
            cursor.close();
        }
    }
}
