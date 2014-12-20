package com.zmb.sunshine.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.zmb.sunshine.data.IWeatherDataParser;
import com.zmb.sunshine.data.openweathermap.OpenWeatherMapParser;
import com.zmb.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * An {@link IntentService} subclass for refreshing weather data in
 * a service on a separate handler thread.
 */
public class SunshineService extends IntentService {

    public static final String EXTRA_LOCATION = "location_setting";

    private static final String TAG = "SunshineService";
    private static final int DAYS_TO_FETCH = 14;

    // TODO: support other types of parsers
    private final IWeatherDataParser mParser = new OpenWeatherMapParser();

    public SunshineService() {
        super("SunshineService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && intent.hasExtra(EXTRA_LOCATION)) {
            String location = intent.getStringExtra(EXTRA_LOCATION);
            HttpURLConnection connection = null;
            try {
                URL url = mParser.buildUrl(location, DAYS_TO_FETCH);
                Log.v(TAG, "Querying " + url.toString());

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                String response = IoUtils.readAll(inputStream);
                mParser.parse(this, response, DAYS_TO_FETCH);

            } catch (IOException e) {
                Log.e(TAG, "Failed to fetch weather", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    public static class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Intent serviceIntent = new Intent(context, SunshineService.class);
            serviceIntent.putExtra(EXTRA_LOCATION, intent.getStringExtra(EXTRA_LOCATION));
            context.startService(serviceIntent);
        }
    }
}
