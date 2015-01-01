package com.zmb.sunshine.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.zmb.sunshine.R;
import com.zmb.sunshine.Sunshine;
import com.zmb.sunshine.data.IWeatherDataParser;
import com.zmb.sunshine.data.db.WeatherContract;
import com.zmb.sunshine.data.openweathermap.OpenWeatherMapParser;
import com.zmb.sunshine.data.worldweatheronline.WorldWeatherOnlineParser;
import com.zmb.sunshine.widget.SunshineWidget;
import com.zmb.utils.IoUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class SunshineSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SunshineSyncAdapter.class.getSimpleName();
    private static final int DAYS_TO_FETCH = 14;

    public static final int SYNC_INTERVAL_SECONDS = 60 * 60 * 3;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL_SECONDS / 3;

    public SunshineSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult) {
        String location = Sunshine.getPreferredLocation(getContext());
        HttpURLConnection connection = null;
        IWeatherDataParser parser = getParser();
        try {
            removeOldWeatherData();

            URL url = parser.buildUrl(location, DAYS_TO_FETCH);
            Log.v(TAG, "Querying " + url.toString());

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            String response = IoUtils.readAll(inputStream);
            parser.parse(getContext(), response, DAYS_TO_FETCH);

            // update any widgets with the new data
            SunshineWidget.updateAllWidgets(getContext());

        } catch (IOException e) {
            Log.e(TAG, "Failed to fetch weather", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private IWeatherDataParser getParser() {
        Context c = getContext();
        String provider = PreferenceManager.getDefaultSharedPreferences(c)
                .getString(c.getString(R.string.pref_weather_provider_key),
                        c.getString(R.string.pref_weather_provider_default));
        if (provider.equals(c.getString(R.string.pref_weather_provider_openweathermap))) {
            return new OpenWeatherMapParser();
        } else {
            return new WorldWeatherOnlineParser();
        }
    }


    /**
     * A helper to get the fake account used with the SyncAdapter.
     * @param context
     * @return
     */
    static Account getSyncAccount(Context context) {
        AccountManager accountManager = AccountManager.get(context);
        Account account = new Account(context.getString(R.string.app_name),
                context.getString(R.string.sync_account_type));

        // if we don't have a password associated with the account, then the account doesn't exist yet
        if (accountManager.getPassword(account) == null) {
            // create the account (empty password and no user data)
            if (!accountManager.addAccountExplicitly(account, "", null)) {
                return null;
            }
            onAccountCreated(account, context);
        }
        return account;
    }

    static void configurePeriodicSync(Context context, int intervalSeconds, int flexTime) {
        Account acct = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        Bundle extras = new Bundle();
        // in kitkat (API 19) and up, we can use inexact repeating alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder()
                    .setExtras(extras) // shouldn't have to do this - Android 5.0 bug
                    .syncPeriodic(intervalSeconds, flexTime)
                    .setSyncAdapter(acct, authority).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(acct, authority, extras, intervalSeconds);
        }
    }

    public static void initialize(Context context) {
        // just make sure that an account has been created
        getSyncAccount(context);
    }

    /**
     * A helper method to start a sync immediately.
     * @param context used to access the account service
     */
    public static void syncNow(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Initial setup that we run when an acount is created.
     * @param newAccount
     * @param context
     */
    private static void onAccountCreated(Account newAccount, Context context) {
        SunshineSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL_SECONDS, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
    }

    private void removeOldWeatherData() {
        String today = WeatherContract.convertDateToString(new Date());
        String where = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " < ?";
        getContext().getContentResolver().delete(
                WeatherContract.WeatherEntry.CONTENT_URI, where, new String[] { today });
    }
}
