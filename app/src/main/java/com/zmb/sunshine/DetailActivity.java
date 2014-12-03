package com.zmb.sunshine;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zmb.sunshine.data.db.WeatherContract;

public class DetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        public static final String LOCATION_KEY = "LOCATION_KEY";
        public static final String DATE_KEY = "DATE_KEY";

        private static final String[] FORECAST_COLUMNS = {
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_DATETEXT,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESCRIPTION,
                WeatherContract.WeatherEntry.COLUMN_TEMPERATURE_HIGH,
                WeatherContract.WeatherEntry.COLUMN_TEMPERATURE_LOW,
        };

        private String mLocationSetting;
        private TextView mDateTextView;
        private TextView mForecastTextView;
        private TextView mHighTextView;
        private TextView mLowTextView;


        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // grab location if it was previously saved
            if (savedInstanceState != null) {
                mLocationSetting = savedInstanceState.getString(LOCATION_KEY);
            }

            getLoaderManager().initLoader(0, null, this);
        }

        @Override
        public void onResume() {
            super.onResume();

            // if the location has changed, then we have to reset the loader to listen for changes on a new URI
            if (mLocationSetting != null && !mLocationSetting.equals(
                    Sunshine.getPreferredLocation(getActivity()))) {
                getLoaderManager().restartLoader(0, null, this);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_detail, container, false);
            mForecastTextView = (TextView) view.findViewById(R.id.detail_forecast_textview);
            mHighTextView = (TextView) view.findViewById(R.id.detail_high_textview);
            mLowTextView = (TextView) view.findViewById(R.id.detail_low_textview);
            mDateTextView = (TextView) view.findViewById(R.id.detail_date_textview);
            return view;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);

            // save location so we don't lose it when screen is rotated
            outState.putString(LOCATION_KEY, mLocationSetting);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            mLocationSetting = Sunshine.getPreferredLocation(getActivity());

            // get the info that was passed when this activity was started
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                String date = intent.getStringExtra(DATE_KEY);
                Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocationSetting, date);
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";
                return new CursorLoader(getActivity(), weatherUri, FORECAST_COLUMNS, null, null, sortOrder);
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data.moveToFirst()) {
                String desc = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESCRIPTION));
                String date = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT));
                double high = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_TEMPERATURE_HIGH));
                double low = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_TEMPERATURE_LOW));

                boolean isMetric = Sunshine.isMetric(getActivity());
                mForecastTextView.setText(desc);
                mDateTextView.setText(Sunshine.formatDate(date));
                mHighTextView.setText(Sunshine.formatTemperature(high, isMetric) + Sunshine.DEGREE_SYMBOL);
                mHighTextView.setText(Sunshine.formatTemperature(high, isMetric) + Sunshine.DEGREE_SYMBOL);
                mLowTextView.setText(Sunshine.formatTemperature(low, isMetric) + Sunshine.DEGREE_SYMBOL);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
            getLoaderManager().restartLoader(0, null, this);
        }
    }
}
