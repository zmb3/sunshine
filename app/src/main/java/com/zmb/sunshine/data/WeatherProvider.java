package com.zmb.sunshine.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.zmb.sunshine.data.db.WeatherContract;
import com.zmb.sunshine.data.db.WeatherDbHelper;

/**
 * A content provider for our weather data.
 */
public class WeatherProvider extends ContentProvider {

    // each URI in the content provider is tied to an integer constant
    private static final int WEATHER = 100;
    private static final int WEATHER_WITH_LOCATION = 101;
    private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private WeatherDbHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new WeatherDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor result = null;
        switch (sUriMatcher.match(uri)) {
            case WEATHER:
                result = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.WeatherEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case WEATHER_WITH_LOCATION:
            case WEATHER_WITH_LOCATION_AND_DATE:
            case LOCATION:
                result = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case LOCATION_ID:
                result = mOpenHelper.getReadableDatabase().query(
                        WeatherContract.LocationEntry.TABLE_NAME,
                        projection, WeatherContract.LocationEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        null, null, null, sortOrder);
                break;
        }
        if (result != null) {
            // causes the cursor to register a content observer to watch for
            // changes at the specified URI and its descendants
            result.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return result;
    }

    @Override
    public String getType(Uri uri) {
        // return MIME type associated with the data at the given URI
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case WEATHER:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION:
                return WeatherContract.WeatherEntry.CONTENT_TYPE;
            case WEATHER_WITH_LOCATION_AND_DATE:
                return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
            case LOCATION:
                // can return multiple rows
                return WeatherContract.LocationEntry.CONTENT_TYPE;
            case LOCATION_ID:
                // only a single row will ever match a location ID
                return WeatherContract.LocationEntry.CONTENT_ITEM_TYPE;
            default: throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    private static UriMatcher buildUriMatcher() {
        // root node shouldn't match anything
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER, WEATHER);

        // date is always numeric, but we store it in the DB as a string
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER + "/*", WEATHER_WITH_LOCATION);
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_WEATHER + "/*/*", WEATHER_WITH_LOCATION_AND_DATE);

        // location ID is always numeric
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_LOCATION, LOCATION);
        matcher.addURI(WeatherContract.CONTENT_AUTHORITY, WeatherContract.PATH_LOCATION + "/#", LOCATION_ID);
        return matcher;
    }
}
