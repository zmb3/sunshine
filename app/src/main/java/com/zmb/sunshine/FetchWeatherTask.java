package com.zmb.sunshine;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.zmb.sunshine.data.IWeatherDataParser;
import com.zmb.sunshine.data.WeatherParseException;
import com.zmb.sunshine.data.openweathermap.OpenWeatherMapParser;
import com.zmb.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An AsyncTask for fetching weather data from a web service
 */
class FetchWeatherTask extends AsyncTask<String, Void, Void> {

    private static final String TAG = "FetchWeatherTask";
    private static final int DAYS_TO_FETCH = 14;

    private final Context mContext;

    // TODO: for now we're hard coded to use open weather map
    private final IWeatherDataParser mParser = new OpenWeatherMapParser();

    public FetchWeatherTask(Context context) {
        mContext = context;
    }


    protected Void doInBackground(String... params) {
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

            InputStream inputStream = urlConnection.getInputStream();
            String apiResponse = IoUtils.readAll(inputStream);
            mParser.parse(mContext, apiResponse, DAYS_TO_FETCH);
            return null;
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


}