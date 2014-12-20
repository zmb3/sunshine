package com.zmb.sunshine;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.ListView;

import com.zmb.sunshine.data.db.AndroidDatabaseManager;
import com.zmb.sunshine.data.db.WeatherContract;
import com.zmb.sunshine.service.SunshineService;

import java.util.Date;

/**
 * A fragment for displaying the overall forecast for
 * several days of weather data.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String TAG = "ForecastFragment";
    private static final String POSITION_KEY = "position";

    private static final int LOADER_ID = 0;

    /**
     * The database columns that we'll display in this fragment.
     */
    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESCRIPTION,
            WeatherContract.WeatherEntry.COLUMN_TEMPERATURE_HIGH,
            WeatherContract.WeatherEntry.COLUMN_TEMPERATURE_LOW,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID
    };

    // These indices are tied to the columns above.
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_HIGH = 3;
    public static final int COL_WEATHER_LOW = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;

    private ListView mForecastList;
    private ForecastAdapter mAdapter;
    private String mLocation;
    private int mSelectedPosition = ListView.INVALID_POSITION;
    private boolean mUseEnhancedTodayView = false;

    /**
     * A callback interface for all activities that
     * contain this fragment.  This allows activities
     * to be notified of selections.
     */
    public interface Callback {
        void onItemSelected(String date);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // indicate that we have a menu (otherwise our menu callbacks won't be called)
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // loaders get initialized here instead of in onCreate
        // because their lifecycles are bound to that of the
        // activity, not the fragment.
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mAdapter = new ForecastAdapter(getActivity(), null, 0);
        mAdapter.setUseDifferentTodayView(mUseEnhancedTodayView);

        mForecastList = (ListView) rootView.findViewById(R.id.listview_forecast);
        mForecastList.setAdapter(mAdapter);

        mForecastList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mSelectedPosition = position;
                Cursor cursor = mAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    // notify the activity that a day was selected
                    // leave it up to the activity to display the details
                    String date = cursor.getString(COL_WEATHER_DATE);
                    ((Callback) getActivity()).onItemSelected(date);
                }
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(POSITION_KEY)) {
            mSelectedPosition = savedInstanceState.getInt(POSITION_KEY);
            // we don't scroll to the position yet, because the list view
            // probably hasn't been populated yet (waiting for data from loader)
        }
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        // fetch new weather data if the location has changed
        if (mLocation != null && !getPreferredLocation().equals(mLocation)) {
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }
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
        } else if (item.getItemId() == R.id.action_database) {
            Intent dbMgr = new Intent(getActivity(), AndroidDatabaseManager.class);
            startActivity(dbMgr);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save our position so we can ensure that the
        // selected item is in view when the activity
        // is recreated
        if (mSelectedPosition != ListView.INVALID_POSITION) {
            outState.putInt(POSITION_KEY, mSelectedPosition);
        }
    }

    /**
     * Configure how the fragment displays today's forecast.
     * Today's forecast can either use a larger, enhanced view,
     * or a standard view.
     * @param useEnhancedView true to use the enhanced view for today,
     *                        false otherwise
     */
    public void setUseEnhancedTodayView(boolean useEnhancedView) {
        mUseEnhancedTodayView = useEnhancedView;
        if (mAdapter != null) {
            mAdapter.setUseDifferentTodayView(useEnhancedView);
        } else {
            Log.e(TAG, "Adapter hasn't been created yet.");
        }
    }

    private void updateWeather() {
        // pull the zip code from the preferences
        // and use it to start a service that will fetch weather data
        mLocation = getPreferredLocation();
        Intent intent = new Intent(getActivity(), SunshineService.class);
        intent.putExtra(SunshineService.EXTRA_LOCATION, mLocation);
        getActivity().startService(intent);
    }

    private String getPreferredLocation() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return prefs.getString(
                getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
    }

    /**
     * Called when a new Loader needs to be created.
     * The loader is going to use our content provider to query the database.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        // we only want to look for current and future days' forecasts
        String today = WeatherContract.convertDateToString(new Date());
        String orderAscending = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";
        mLocation = getPreferredLocation();
        Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(mLocation, today);
        return new CursorLoader(getActivity(), uri, FORECAST_COLUMNS, null, null, orderAscending);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
        if (mSelectedPosition != ListView.INVALID_POSITION) {
            mForecastList.setSelection(mSelectedPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mAdapter.swapCursor(null);
    }
}
