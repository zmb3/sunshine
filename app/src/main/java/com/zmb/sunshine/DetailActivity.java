package com.zmb.sunshine;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * An activity for displaying a detailed forecast for a particular day.
 * This activity is only used in the phone (single pane) UI.
 * In the tablet UI, the forecast list and details vew are shown
 * in the same activity.
 */
public class DetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            String date = getIntent().getStringExtra(DetailFragment.DATE_KEY);
            Bundle dateInfo = new Bundle();
            dateInfo.putString(DetailFragment.DATE_KEY, date);
            DetailFragment details = new DetailFragment();
            details.setArguments(dateInfo);
            getFragmentManager().beginTransaction()
                    .add(R.id.weather_detail_container, details)
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

}
