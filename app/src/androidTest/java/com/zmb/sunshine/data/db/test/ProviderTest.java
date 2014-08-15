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
 * Unit tests for the weather database.
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

        String testLocation = "94074";
        // content://com.zmb.sunshine/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.zmb.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://com.zmb.sunshine/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithStartDate(testLocation, testDate));
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

        ContentValues locationValues = createLocationValues();
        long locationRowId = db.insert(LocationEntry.TABLE_NAME, null, locationValues);
        assertTrue(locationRowId != -1);

        // this looks just like the test in TestDb but instead of querying the database
        // directly we use a content resovler to query the content provider
        validateCursor(mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI, null, null, null, null), locationValues);

        // now try to query for a particular row
        validateCursor((mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null, null, null, null)), locationValues);

        ContentValues weatherValues = createWeatherValues(locationRowId);
        long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue(weatherRowId != -1);
        validateCursor(mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI, null, null, null, null), weatherValues);

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

    static ContentValues createLocationValues() {
        ContentValues values = new ContentValues();
        values.put(LocationEntry.COLUMN_CITY_NAME, "North Pole");
        values.put(LocationEntry.COLUMN_LOCATION_SETTING, 99705);
        values.put(LocationEntry.COLUMN_LATITUDE, 64.7488);
        values.put(LocationEntry.COLUMN_LONGITUDE, -147.353);
        return values;
    }

    static ContentValues createWeatherValues(long rowId) {
        ContentValues weatherValues = new ContentValues();
        weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, rowId);
        weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
        weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
        weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
        weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
        weatherValues.put(WeatherEntry.COLUMN_TEMPERATURE_HIGH, 75);
        weatherValues.put(WeatherEntry.COLUMN_TEMPERATURE_LOW, 65);
        weatherValues.put(WeatherEntry.COLUMN_SHORT_DESCRIPTION, "Asteroids");
        weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
        weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);
        return weatherValues;
    }
}
