package com.zmb.sunshine;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.zmb.sunshine.data.DayForecast;
import com.zmb.sunshine.data.WeatherParseException;
import com.zmb.sunshine.data.openweathermap.OpenWeatherMapParser;
import com.zmb.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment for displaying the overall forecast for
 * several days of weather data.
 */
public class ForecastFragment extends Fragment {

    private static final String TAG = "ForecastFragment";

    private ListView mForecastList;
    private ArrayAdapter<String> mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // indicate that we have a menu (otherwise our menu callbacks won't be called)
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new ArrayList<String>());

        mForecastList = (ListView) rootView.findViewById(R.id.listview_forecast);
        mForecastList.setAdapter(mAdapter);

        mForecastList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // navigate to the detail activity
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                String forecast = mAdapter.getItem(position);
                intent.putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        // pull the zip code from the preferences
        // and use it to fetch weather data
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = prefs.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        new FetchWeatherTask().execute(location);
    }

    class FetchWeatherTask extends AsyncTask<String, Void, List<DayForecast>> {

        private static final String TAG = "FetchWeatherTask";
        private static final String BASE_URL_OPEN_WEATHER_MAP = "http://api.openweathermap.org/data/2.5/forecast/daily";

        @Override
        protected void onPostExecute(List<DayForecast> dayForecasts) {
            if (dayForecasts != null) {
                final String imperial = getString(R.string.pref_units_imperial);
                final String units = PreferenceManager
                        .getDefaultSharedPreferences(getActivity())
                        .getString(getString(R.string.pref_units_key), imperial);
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
                Log.w(TAG, "Not provided with zip code");
                return null;
            }

            // declared outside the try/catch so that it can be closed in the finally block.
            HttpURLConnection urlConnection = null;

            try {
                Uri uri = Uri.parse(BASE_URL_OPEN_WEATHER_MAP).buildUpon()
                        .appendQueryParameter("q", params[0] +",USA")
                        .appendQueryParameter("mode", "json")
                        .appendQueryParameter("units", "metric")
                        .appendQueryParameter("cnt", "7")
                        .build();
                URL url = new URL(uri.toString());
                Log.v(TAG, "Querying " + url.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                String apiResponse = IoUtils.readAll(inputStream);

                // TODO: for now we're hard coded to use open weather map
                return new OpenWeatherMapParser().parse(apiResponse, 7);

            } catch (IOException e) {
                Log.e(TAG, "Error fetching weather", e);
                Toast.makeText(getActivity(), "There was an error fetching weather data.", Toast.LENGTH_LONG).show();
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
}
