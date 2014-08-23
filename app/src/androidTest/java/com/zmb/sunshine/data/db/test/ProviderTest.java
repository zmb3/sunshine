package com.zmb.sunshine.data.db.test;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.zmb.sunshine.data.db.WeatherContract.LocationEntry;
import com.zmb.sunshine.data.db.WeatherContract.WeatherEntry;
import com.zmb.sunshine.data.db.WeatherDbHelper;

import java.util.Map;

/**
 * Unit tests for the weather content provider.
 */
public class ProviderTest extends AndroidTestCase {

    public void testDeleteDb() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    public void testGetType() {
        // content://com.zmb.sunshine/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.zmb.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        // content://com.zmb.sunshine/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(DbTest.TEST_LOCATION));
        // vnd.android.cursor.dir/com.zmb.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        // content://com.zmb.sunshine/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(DbTest.TEST_LOCATION, DbTest.TEST_DATE));
        // vnd.android.cursor.item/com.zmb.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.zmb.sunshine/location/
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.zmb.sunshine/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.zmb.sunshine/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.zmb.sunshine/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testInsertReadProvider() {
        WeatherDbHelper helper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues locationValues = DbTest.createLocationValues();
        long locationRowId = db.insert(LocationEntry.TABLE_NAME, null, locationValues);
        assertTrue(locationRowId != -1);

        // this looks just like the test in TestDb but instead of querying the database
        // directly we use a content resolver to query the content provider
        validateCursor(mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI, null, null, null, null), locationValues);

        // now try to query for a particular row
        validateCursor((mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null, null, null, null)), locationValues);

        ContentValues weatherValues = DbTest.createWeatherValues(locationRowId);
        long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue(weatherRowId != -1);
        validateCursor(mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI, null, null, null, null), weatherValues);

        // test the JOIN
        addAllContentValues(weatherValues, locationValues);
        validateCursor(mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocation(DbTest.TEST_LOCATION),
                null, null, null, null), weatherValues);

        validateCursor(mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithStartDate(DbTest.TEST_LOCATION, DbTest.TEST_DATE),
                null, null, null, null), weatherValues);

        validateCursor(mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithDate(DbTest.TEST_LOCATION, DbTest.TEST_DATE),
                null, null, null, null), weatherValues);

        helper.close();
    }

    static void validateCursor(Cursor cursor, ContentValues values) {
        assertTrue(cursor.moveToFirst());
        for (Map.Entry<String, Object> entry : values.valueSet()) {
            String column = entry.getKey();
            int idx = cursor.getColumnIndex(column);
            assertTrue(idx != -1);
            String expected = entry.getValue().toString();
            assertEquals("Expected " + expected + ", got " + cursor.getString(idx),
                    expected, cursor.getString(idx));
        }
        cursor.close();
    }

    static void addAllContentValues(ContentValues destination, ContentValues source) {
        for (String key : source.keySet()) {
            destination.put(key, source.getAsString(key));
        }
    }
}
