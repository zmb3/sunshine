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
public class DbTest extends AndroidTestCase {

    public void testCreate() throws Throwable {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());
        db.close();
    }

    public void testInsertRead() {
        WeatherDbHelper helper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues locationValues = createLocationValues();
        long locationRowId = db.insert(LocationEntry.TABLE_NAME, null, locationValues);
        assertTrue(locationRowId != -1);
        validateCursor(db.query(LocationEntry.TABLE_NAME, null,
                null, null, null, null, null), locationValues);

        ContentValues weatherValues = createWeatherValues(locationRowId);
        long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
        assertTrue(weatherRowId != -1);
        validateCursor(db.query(WeatherEntry.TABLE_NAME, null,
                null, null, null, null, null), weatherValues);

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
