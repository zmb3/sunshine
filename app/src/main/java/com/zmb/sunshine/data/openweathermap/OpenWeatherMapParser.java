package com.zmb.sunshine.data.openweathermap;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.zmb.sunshine.data.AWeatherDataParser;
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
public class OpenWeatherMapParser extends AWeatherDataParser {

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
        values.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, getWeatherId(weatherId));
        values.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
        values.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
        values.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDir);
        values.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
        return values;
    }

    /**
     * Convert a weather ID code from the OpenWeatherMap API
     * into the code we use in the database.
     * @param apiId
     * @return
     */
    private static int getWeatherId(int apiId) {
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (apiId >= 200 && apiId <= 232) {
            return WeatherContract.WeatherId.STORM.mValue;
        } else if (apiId >= 300 && apiId <= 321) {
            return WeatherContract.WeatherId.LIGHT_RAIN.mValue;
        } else if (apiId >= 500 && apiId <= 504) {
            return WeatherContract.WeatherId.RAIN.mValue;
        } else if (apiId == 511) {
            return WeatherContract.WeatherId.RAIN.mValue;
        } else if (apiId >= 520 && apiId <= 531) {
            return WeatherContract.WeatherId.RAIN.mValue;
        } else if (apiId >= 600 && apiId <= 622) {
            return WeatherContract.WeatherId.SNOW.mValue;
        } else if (apiId >= 701 && apiId <= 761) {
            return WeatherContract.WeatherId.FOG.mValue;
        } else if (apiId == 761 || apiId == 781) {
            return WeatherContract.WeatherId.STORM.mValue;
        } else if (apiId == 800) {
            return WeatherContract.WeatherId.CLEAR.mValue;
        } else if (apiId == 801) {
            return WeatherContract.WeatherId.LIGHT_CLOUDS.mValue;
        } else if (apiId >= 802 && apiId <= 804) {
            return WeatherContract.WeatherId.CLOUDS.mValue;
        }
        return -1;
    }
}
