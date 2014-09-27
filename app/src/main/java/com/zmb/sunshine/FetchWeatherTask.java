package com.zmb.sunshine;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.zmb.sunshine.data.DayForecast;
import com.zmb.sunshine.data.IWeatherDataParser;
import com.zmb.sunshine.data.WeatherParseException;
import com.zmb.sunshine.data.db.WeatherContract;
import com.zmb.sunshine.data.openweathermap.OpenWeatherMapParser;
import com.zmb.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

class FetchWeatherTask extends AsyncTask<String, Void, List<DayForecast>> {

    private static final String TAG = "FetchWeatherTask";
    private static final int DAYS_TO_FETCH = 14;

    private final Context mContext;
    private final ArrayAdapter<String> mAdapter;

    // TODO: for now we're hard coded to use open weather map
    private final IWeatherDataParser mParser = new OpenWeatherMapParser();

    public FetchWeatherTask(Context context, ArrayAdapter<String> adapter) {
        mContext = context;
        mAdapter = adapter;
    }

    @Override
    protected void onPostExecute(List<DayForecast> dayForecasts) {
        if (dayForecasts != null) {
            final String imperial = mContext.getString(R.string.pref_units_imperial);
            final String units = PreferenceManager
                    .getDefaultSharedPreferences(mContext)
                    .getString(mContext.getString(R.string.pref_units_key), imperial);
            mAdapter.clear();
            for (DayForecast d : dayForecasts) {
                if (units.equals(imperial)) {
                    mAdapter.add(d.toStringImperial());
                } else {
                    mAdapter.add(d.toStringMetric());
                }
            }
        }
    }

    protected List<DayForecast> doInBackground(String... params) {
        if (params.length == 0) {
            Log.w(TAG, "Not provided with location setting");
            return null;
        }


        HttpURLConnection urlConnection = null;
        try {
            String locationSetting = params[0];
            URL url = mParser.buildUrl(locationSetting, DAYS_TO_FETCH);
            Log.v(TAG, "Querying " + url.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            String apiResponse = IoUtils.readAll(inputStream);


            IWeatherDataParser.Result result = mParser.parse(apiResponse, DAYS_TO_FETCH);
            long locationRowId = addLocation(locationSetting, result.getCityName(), result.getLatitude(), result.getLongitude());

            ArrayList<ContentValues> itemsToInsert = new ArrayList<ContentValues>();
            for (DayForecast day : result.getDays()) {
                ContentValues values = new ContentValues();
                values.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
                values.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT, WeatherContract.convertDateToString(day.getDate()));
                values.put(WeatherContract.WeatherEntry.COLUMN_TEMPERATURE_HIGH, day.getHighTemperature());
                values.put(WeatherContract.WeatherEntry.COLUMN_TEMPERATURE_LOW, day.getLowTemperature());
                // TODO: add humidity, pressure, wind speed, wind direction, etc.
                itemsToInsert.add(values);
            }
            int rowsInserted = mContext.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI,
                    itemsToInsert.toArray(new ContentValues[itemsToInsert.size()]));
            Log.d(TAG, "Inserted " + rowsInserted + " rows of weather data>");

            return result.getDays();

        } catch (IOException e) {
            Log.e(TAG, "Error fetching weather", e);
            Toast.makeText(mContext, "There was an error fetching weather data.", Toast.LENGTH_LONG).show();
        } catch (WeatherParseException wpe) {
            Log.e(TAG, "Failed to parse weather");
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    /**
     * Insert a location into the database if it doesn't already exist.
     * @param locationSetting
     * @param cityName
     * @param lat
     * @param lon
     * @return the row ID the location
     */
    protected long addLocation(String locationSetting, String cityName, double lat, double lon) {
        // check if location exists already
        Cursor cursor = mContext.getContentResolver().query(
                WeatherContract.LocationEntry.CONTENT_URI,
                new String[] {WeatherContract.LocationEntry._ID },
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[] { locationSetting },
                null);
        try {
            if (cursor.moveToFirst()) {
                // location found - just return it
                return cursor.getLong(cursor.getColumnIndex(WeatherContract.LocationEntry._ID));
            } else {
                // we need to add it
                ContentValues values = new ContentValues();
                values.put(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
                values.put(WeatherContract.LocationEntry.COLUMN_CITY_NAME, cityName);
                values.put(WeatherContract.LocationEntry.COLUMN_LATITUDE, lat);
                values.put(WeatherContract.LocationEntry.COLUMN_LONGITUDE,lon);
                Uri uri = mContext.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, values);
                return ContentUris.parseId(uri);
            }
        } finally {
            cursor.close();
        }
    }

}