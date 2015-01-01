package com.zmb.sunshine.data.worldweatheronline;

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
import java.util.List;

public class WorldWeatherOnlineParser extends AWeatherDataParser {

    private static final String TAG = WorldWeatherOnlineParser.class.getSimpleName();
    private static final String API_KEY = "6b7892a1b12aeb6feb3b8785d4f94";
    private static final String BASE_URI = "https://api.worldweatheronline.com/free/v2/weather.ashx";

    private static final int SNOW_AND_THUNDER = 395;
    private static final int LIGHT_SNOW_AND_THUNDER = 392;
    private static final int RAIN_AND_THUNDER = 389;
    private static final int THUNDERY_OUTBREAKS = 200;
    private static final int LIGHT_RAIN_AND_THUNDER = 386;
    private static final int HEAVY_ICE_PELLETS = 377;
    private static final int ICE_PELLETS = 350;
    private static final int LIGHT_ICE_PELLETS = 374;

    private static final int BLIZZARD = 230;
    private static final int BLOWING_SNOW = 227;
    private static final int PATCHY_HEAVY_SNOW = 335;
    private static final int PATCHY_MODERATE_SNOW = 329;
    private static final int PATCHY_LIGHT_SNOW = 323;
    private static final int HEAVY_SNOW_SHOWERS = 338;
    private static final int SNOW_SHOWERS = 371;
    private static final int LIGHT_SNOW_SHOWERS = 368;
    private static final int LIGHT_SNOW = 326;
    private static final int MODERATE_SNOW = 332;
    private static final int PATCHY_SNOW_NEARBY = 179;

    private static final int LIGHT_SLEET = 317;
    private static final int HEAVY_SLEET = 320;
    private static final int SLEET_SHOWERS = 365;
    private static final int PATCHY_SLEET = 182;
    private static final int LIGHT_SLEET_SHOWERS = 362;

    private static final int TORRENTIAL_RAIN = 359;
    private static final int RAIN = 356;
    private static final int HEAVY_RAIN = 308;
    private static final int HEAVY_RAIN_AT_TIMES = 305;
    private static final int MODERATE_RAIN = 302;
    private static final int MODERATE_RAIN_AT_TIMES = 299;
    private static final int LIGHT_RAIN_SHOWER = 353;
    private static final int LIGHT_RAIN = 296;
    private static final int PATCHY_LIGHT_RAIN = 293;
    private static final int PATCHY_RAIN_NEARBY = 176;
    private static final int FREEZING_RAIN = 314;
    private static final int LIGHT_FREEZING_RAIN = 311;
    private static final int LIGHT_DRIZZLE = 266;
    private static final int PATCHY_LIGHT_DRIZZLE = 263;
    private static final int HEAVY_FREEZING_DRIZZLE = 284;
    private static final int FREEZING_DRIZZLE = 281;
    private static final int PATCHY_FREEZING_DRIZZLE = 185;
    private static final int MIST = 143;

    private static final int OVERCAST = 122;
    private static final int CLOUDY = 119;
    private static final int PARTLY_CLOUDY = 116;
    private static final int CLEAR = 113;
    private static final int FREEZING_FOG = 260;
    private static final int FOG = 248;

    private String mLocation;

    @Override
    public URL buildUrl(String locationSetting, int daysToFetch) throws MalformedURLException {

        mLocation = locationSetting;

        Uri uri = Uri.parse(BASE_URI).buildUpon()
                .appendQueryParameter("key", API_KEY)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("q", locationSetting)
                .appendQueryParameter("num_of_days", String.valueOf(daysToFetch))
                .appendQueryParameter("cc", "no") // don't care about current conditions
                .appendQueryParameter("includeLocation", "yes")
                .build();
        return new URL(uri.toString());
    }

    @Override
    public void parse(Context c, String data, int numberOfDays) throws WeatherParseException {
        try {
            JSONObject root = new JSONObject(data).getJSONObject("data");

            JSONObject area = (JSONObject) root.getJSONArray("nearest_area").get(0);
            JSONObject areaName = (JSONObject) area.getJSONArray("areaName").get(0);
            String cityName = areaName.getString("value");
            double lat = area.getDouble("latitude");
            double lon = area.getDouble("longitude");
            long locationRowId = addLocation(c, mLocation, cityName, lat, lon);

            JSONArray weather = root.getJSONArray("weather");
            List<ContentValues> valuesToAdd = new ArrayList<>();
            for (int i = 0; i < weather.length(); ++i) {
                valuesToAdd.add(parseDay(weather.getJSONObject(i), locationRowId));
            }
            int rowsInserted = c.getContentResolver().bulkInsert(
                    WeatherContract.WeatherEntry.CONTENT_URI,
                    valuesToAdd.toArray(new ContentValues[valuesToAdd.size()]));
            Log.d("WorldWeatherOnline", "Inserted " + rowsInserted + " rows of weather data.");
        } catch (JSONException e) {
            throw new WeatherParseException(data, e);
        }
    }

    private ContentValues parseDay(JSONObject day, long locationRowId) throws JSONException {

        final double min = day.getDouble("mintempC");
        final double max = day.getDouble("maxtempC");

        // date is in YYYY-mm-DD format, we just need to remove the -
        // to be compatible with our database
        String date = day.getString("date").replace("-", "");

        Log.v(TAG,"For " + date + ", " + max + " / " + min);

        JSONObject hourly = (JSONObject) day.getJSONArray("hourly").get(0);
        int humidity = hourly.getInt("humidity");
        int weatherCode = hourly.getInt("weatherCode");
        double pressure = hourly.getDouble("pressure");
        double windDir = hourly.getDouble("winddirDegree");

        // convert kph to mps
        double windSpeedKmph = hourly.getDouble("windspeedKmph");
        double windSpeed = windSpeedKmph * 1000 / 3600;
        JSONObject weatherDescription = (JSONObject) hourly.getJSONArray("weatherDesc").get(0);
        String desc = weatherDescription.getString("value");

        ContentValues values = new ContentValues();
        values.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowId);
        values.put(WeatherContract.WeatherEntry.COLUMN_DATETEXT, date);
        values.put(WeatherContract.WeatherEntry.COLUMN_TEMPERATURE_HIGH, max);
        values.put(WeatherContract.WeatherEntry.COLUMN_TEMPERATURE_LOW, min);
        values.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESCRIPTION, desc);
        values.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, convertWeatherId(weatherCode));
        values.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY, humidity);
        values.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE, pressure);
        values.put(WeatherContract.WeatherEntry.COLUMN_DEGREES, windDir);
        values.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
        return values;
    }

    /**
     * Convert API weather codes to common codes used in our database.
     *
     * http://www.worldweatheronline.com/feed/wwoConditionCodes.xml
     *
     * @param weatherId
     * @return
     */
    private static int convertWeatherId(int weatherId) {
        switch (weatherId) {
            case CLEAR:
                return WeatherContract.WeatherId.CLEAR.mValue;
            case CLOUDY:
            case OVERCAST:
                return WeatherContract.WeatherId.CLOUDS.mValue;
            case PARTLY_CLOUDY:
                return WeatherContract.WeatherId.LIGHT_CLOUDS.mValue;
            case FOG:
            case FREEZING_FOG:
                return WeatherContract.WeatherId.FOG.mValue;
            case THUNDERY_OUTBREAKS:
            case LIGHT_RAIN_AND_THUNDER:
            case LIGHT_SNOW_AND_THUNDER:
            case RAIN_AND_THUNDER:
            case SNOW_AND_THUNDER:
            case HEAVY_ICE_PELLETS:
            case ICE_PELLETS:
            case LIGHT_ICE_PELLETS:
                return WeatherContract.WeatherId.STORM.mValue;
            case LIGHT_DRIZZLE:
            case LIGHT_FREEZING_RAIN:
            case LIGHT_RAIN_SHOWER:
            case LIGHT_SLEET:
            case LIGHT_SLEET_SHOWERS:
            case PATCHY_LIGHT_RAIN:
            case PATCHY_LIGHT_DRIZZLE:
            case LIGHT_RAIN:
            case PATCHY_RAIN_NEARBY:
            case MIST:
                return WeatherContract.WeatherId.LIGHT_RAIN.mValue;
            case TORRENTIAL_RAIN:
            case RAIN:
            case FREEZING_RAIN:
            case HEAVY_RAIN:
            case HEAVY_RAIN_AT_TIMES:
            case HEAVY_SLEET:
            case SLEET_SHOWERS:
            case PATCHY_SLEET:
            case MODERATE_RAIN:
            case MODERATE_RAIN_AT_TIMES:
            case FREEZING_DRIZZLE:
            case HEAVY_FREEZING_DRIZZLE:
            case PATCHY_FREEZING_DRIZZLE:
                return WeatherContract.WeatherId.RAIN.mValue;
            case SNOW_SHOWERS:
            case BLOWING_SNOW:
            case LIGHT_SNOW:
            case MODERATE_SNOW:
            case PATCHY_HEAVY_SNOW:
            case PATCHY_LIGHT_SNOW:
            case PATCHY_MODERATE_SNOW:
            case HEAVY_SNOW_SHOWERS:
            case LIGHT_SNOW_SHOWERS:
            case PATCHY_SNOW_NEARBY:
            case BLIZZARD:
                return WeatherContract.WeatherId.SNOW.mValue;
            default: return -1;
        }
    }
}
