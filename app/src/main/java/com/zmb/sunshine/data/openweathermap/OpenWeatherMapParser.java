package com.zmb.sunshine.data.openweathermap;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.zmb.sunshine.data.IWeatherDataParser;
import com.zmb.sunshine.data.WeatherParseException;
import com.zmb.sunshine.data.db.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Parses weather data received from Open Weather map.
 */
public class OpenWeatherMapParser implements IWeatherDataParser {

    private static final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily";

    private String mLocation;

    @Override
    public URL buildUrl(String locationSetting, int daysToFetch) throws MalformedURLException {

        mLocation = locationSetting;

        // we have to add ",USA" to the location setting or open weather map
        // gets confused and looks outside the USA
        locationSetting += ",USA";

        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("q", locationSetting)
                .appendQueryParameter("mode", "json")
                .appendQueryParameter("units", "metric")
                .appendQueryParameter("cnt", String.valueOf(daysToFetch))
                .build();
        URL url = new URL(uri.toString());
        return url;
    }

    @Override
    public void parse(Context context, String data, int numberOfDays) throws WeatherParseException {
        try {
            JSONObject json = new JSONObject(data);
            JSONObject city = json.getJSONObject("city");
            String cityName = city.getString("name");
            JSONObject location = city.getJSONObject("coord");
            double lat = location.getDouble("lat");
            double lon = location.getDouble("lon");
            long rowId = addLocation(context, mLocation, cityName, lat, lon);

            List<ContentValues> itemsToInsert = new ArrayList<ContentValues>();
            JSONArray days = json.getJSONArray("list");
            for (int i = 0; i < numberOfDays; ++i) {
                ContentValues values = parseDay(days.getJSONObject(i), rowId);
                itemsToInsert.add(values);
            }
            int rowsInserted = context.getContentResolver().bulkInsert(
                    WeatherContract.WeatherEntry.CONTENT_URI,
                    itemsToInsert.toArray(new ContentValues[itemsToInsert.size()]));
            Log.d("OpenWeatherMap", "Inserted " + rowsInserted + " rows of weather data.");
        } catch (JSONException e) {
            throw new WeatherParseException(data, e);
        }
    }

    /**
     * Insert a location into the database if it doesn't already exist.
     *
     * @param locationSetting
     * @param cityName
     * @param lat
     * @param lon
     * @return the row ID of the specified location
     */
    private long addLocation(Context c, String locationSetting, String cityName, double lat, double lon) {
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

    private ContentValues parseDay(JSONObject day, long locationRowId) throws JSONException {

        JSONObject temp = day.getJSONObject("temp");
        final double min = temp.getDouble("min");
        final double max = temp.getDouble("max");

        final int humidity = day.getInt("humidity");
        final double pressure = day.getDouble("pressure");
        final double windSpeed = day.getDouble("speed");
        final double windDir = day.getDouble("deg");

        JSONObject weather = day.getJSONArray("weather").getJSONObject(0);
        final String desc = weather.getString("main");
        final int weatherId = weather.getInt("id");

        // open weather map reports the date as a unix timestamp (seconds)
        // convert it to milliseconds to convert to a Date object
        long datetime = day.getLong("dt");
        Date date = new Date(datetime * 1000);

        ContentValues values = new ContentValues();
        values.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        values.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                WeatherContract.convertDateToString(date));
        values.put(WeatherContract.WeatherEntry.COLUMN_TEMPERATURE_HIGH, max);
        values.put(WeatherContract.WeatherEntry.COLUMN_TEMPERATURE_LOW, min);
        values.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESCRIPTION, desc);
        values.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, weatherId);
        values.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
        values.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
        values.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDir);
        values.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
        return values;
    }
}
