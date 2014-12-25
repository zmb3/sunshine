package com.zmb.sunshine.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.zmb.sunshine.R;
import com.zmb.sunshine.Sunshine;
import com.zmb.sunshine.data.db.WeatherContract;

import java.util.Calendar;
import java.util.Date;

/**
 * Implementation of App Widget functionality.
 */
public class SunshineWidget extends AppWidgetProvider {

    private static final String TAG = "Widget";

    private static final int LOADER_ID = 55312;

    private static final String[] COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_DATETEXT,
            WeatherContract.WeatherEntry.COLUMN_TEMPERATURE_HIGH,
            WeatherContract.WeatherEntry.COLUMN_TEMPERATURE_LOW
    };

    private static final int COL_DATE = 0;
    private static final int COL_HIGH = 1;
    private static final int COL_LOW = 2;

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.v(TAG, "onUpdate");
        // There may be multiple widgets active, so update all of them
        for (int i = 0; i < appWidgetIds.length; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    /**
     * Called when the widget is first placed and any time it is resized.
     * @param context
     * @param appWidgetManager
     * @param appWidgetId
     * @param newOptions
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        Log.v(TAG, "App Widget Options Changed");
        // TODO: customize how many days we display based on the size
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        Log.v(TAG, "App widget enabled");
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        Log.v(TAG, "App widget disabled");
    }

    void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                         int appWidgetId) {

        Date todaysDate = new Date();
        String today = WeatherContract.convertDateToString(todaysDate);

        // we only want to query 3 days
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(todaysDate);
        calendar.add(Calendar.DATE, 3);
        Date endDate = calendar.getTime();
        String end = WeatherContract.convertDateToString(endDate);

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATETEXT + " ASC";
        Uri uri = WeatherContract.WeatherEntry.buildWeatherLocatinWithStartAndEndDate(
                Sunshine.getPreferredLocation(context), today, end);

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, COLUMNS, null, null, sortOrder);
        RemoteViews views;

        try {
            // update views - just brute force each of 3 days
            views = new RemoteViews(context.getPackageName(), R.layout.sunshine_widget);
            boolean isMetric = Sunshine.isMetric(context);
            if (cursor.moveToFirst()) {
                String temperature = Sunshine.formatTemperature(context, cursor.getDouble(COL_HIGH), isMetric) +
                        " / " + Sunshine.formatTemperature(context, cursor.getDouble(COL_LOW), isMetric);
                views.setTextViewText(R.id.widget_day_text0, Sunshine.shortFriendlyDate(context, cursor.getString(COL_DATE)));
                views.setTextViewText(R.id.widget_temperature_text0, temperature);
                views.setImageViewResource(R.id.widget_icon0, R.drawable.ic_clear); // TODO:
            }

            if (cursor.moveToNext()) {
                String temperature = Sunshine.formatTemperature(context, cursor.getDouble(COL_HIGH), isMetric) +
                        " / " + Sunshine.formatTemperature(context, cursor.getDouble(COL_LOW), isMetric);
                views.setTextViewText(R.id.widget_day_text1, Sunshine.shortFriendlyDate(context, cursor.getString(COL_DATE)));
                views.setTextViewText(R.id.widget_temperature_text1, temperature);
                views.setImageViewResource(R.id.widget_icon1, R.drawable.ic_snow); // TODO:
            }

            if (cursor.moveToNext()) {
                String temperature = Sunshine.formatTemperature(context, cursor.getDouble(COL_HIGH), isMetric) +
                        " / " + Sunshine.formatTemperature(context, cursor.getDouble(COL_LOW), isMetric);
                views.setTextViewText(R.id.widget_day_text2, Sunshine.shortFriendlyDate(context, cursor.getString(COL_DATE)));
                views.setTextViewText(R.id.widget_temperature_text2, temperature);
                views.setImageViewResource(R.id.widget_icon2, R.drawable.ic_fog); // TODO:
            }
        } finally {
            cursor.close();
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);


    }




}



