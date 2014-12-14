package com.zmb.sunshine;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity implements ForecastFragment.Callback {

    private boolean mIsTwoPaneUi = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getFragmentManager();

        // check if we're in two pane UI or single pane
        if (findViewById(R.id.weather_detail_container) != null) {
            mIsTwoPaneUi = true;

            // show the detail view in this activity
            // (we only need to restore the detail fragment if
            // it wasn't already saved off)
            if (savedInstanceState== null) {
                fm.beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment())
                        .commit();
            }
        } else {
            mIsTwoPaneUi = false;
        }

        ForecastFragment ff = (ForecastFragment)(fm.findFragmentById(R.id.fragment_forecast));
        ff.setUseEnhancedTodayView(!mIsTwoPaneUi);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    /**
     * Called when a particular day is selected from the forecast list.
     * @param date
     */
    @Override
    public void onItemSelected(String date) {
        if (mIsTwoPaneUi) {
            // on tablets, we replace the detail fragment
            DetailFragment details = new DetailFragment();
            Bundle dateBundle = new Bundle();
            dateBundle.putString(DetailFragment.DATE_KEY, date);
            details.setArguments(dateBundle);
            getFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, details)
                    .commit();
        } else {
            // on phones, we launch the detail activity
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailFragment.DATE_KEY, date);
            startActivity(intent);
        }
    }
}
