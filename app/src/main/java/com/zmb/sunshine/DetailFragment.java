package com.zmb.sunshine;


import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zmb.sunshine.data.db.WeatherContract;

import javax.net.ssl.SSLPeerUnverifiedException;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String LOCATION_KEY = "LOCATION_KEY";
    public static final String DATE_KEY = "DATE_KEY";

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESCRIPTION,
            WeatherContract.WeatherEntry.COLUMN_TEMPERATURE_HIGH,
            WeatherContract.WeatherEntry.COLUMN_TEMPERATURE_LOW,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    private String mLocationSetting;

    private ImageView mIconView;
    private TextView mDateTextView;
    private TextView mDayTextView;
    private TextView mForecastTextView;
    private TextView mHighTextView;
    private TextView mLowTextView;
    private TextView mHumidityTextView;
    private TextView mWindTextView;
    private TextView mPressureTextView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // grab location if it was previously saved
        if (savedInstanceState != null) {
            mLocationSetting = savedInstanceState.getString(LOCATION_KEY);
        }

        Bundle args = getArguments();
        if (args != null && args.containsKey(DATE_KEY)) {
            getLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // if the location has changed, then we have to reset the loader to listen for changes on a new URI
        Bundle args = getArguments();
        if (args != null && args.containsKey(DATE_KEY) &&
                mLocationSetting != null &&
                !mLocationSetting.equals(Sunshine.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        mIconView = (ImageView) view.findViewById(R.id.detail_icon);
        mForecastTextView = (TextView) view.findViewById(R.id.detail_forecast_textview);
        mHighTextView = (TextView) view.findViewById(R.id.detail_high_textview);
        mLowTextView = (TextView) view.findViewById(R.id.detail_low_textview);
        mDateTextView = (TextView) view.findViewById(R.id.detail_date_textview);
        mDayTextView = (TextView) view.findViewById(R.id.detail_day_textview);
        mHumidityTextView = (TextView) view.findViewById(R.id.detail_humitidy_textview);
        mWindTextView = (TextView) view.findViewById(R.id.detail_wind_textview);
        mPressureTextView = (TextView) view.findViewById(R.id.detail_pressure_textview);
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

        // get the date that was provided in this fragments arguments
        String date = getArguments().getString(DATE_KEY);
        Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(mLocationSetting, date);
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";
        return new CursorLoader(getActivity(), weatherUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            int weatherId = data.getInt(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
            String desc = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESCRIPTION));
            String date = data.getString(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATETEXT));
            double high = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_TEMPERATURE_HIGH));
            double low = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_TEMPERATURE_LOW));
            double humidity = data.getDouble(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_HUMIDITY));
            float windSpeed = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED));
            float windDir = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DEGREES));
            float pressure = data.getFloat(data.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_PRESSURE));

            boolean isMetric = Sunshine.isMetric(getActivity());

            mIconView.setImageResource(Sunshine.getArtForWeatherId(weatherId));
            mForecastTextView.setText(desc);
            mDateTextView.setText(Sunshine.formatDate(date));
            mDayTextView.setText(Sunshine.dayName(getActivity(), date));
            mHighTextView.setText(Sunshine.formatTemperature(getActivity(), high, isMetric));
            mLowTextView.setText(Sunshine.formatTemperature(getActivity(), low, isMetric));
            mWindTextView.setText(Sunshine.formatWind(getActivity(), windSpeed, windDir));
            mPressureTextView.setText(getString(R.string.format_pressure, pressure));
            mHumidityTextView.setText(getString(R.string.format_humidity, humidity));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        getLoaderManager().restartLoader(0, null, this);
    }
}
