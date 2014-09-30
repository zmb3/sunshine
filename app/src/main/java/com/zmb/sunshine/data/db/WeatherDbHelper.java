package com.zmb.sunshine.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zmb.sunshine.data.db.WeatherContract.LocationEntry;
import com.zmb.sunshine.data.db.WeatherContract.WeatherEntry;


public class WeatherDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "weather.db";

    // should be incremented if the schema is changed!
    private static final int DATABASE_VERSION = 1;

    public WeatherDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // create our SQLite database tables
        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        // this only gets called if we change the version of our database
        // drop the tables and recreate them according to the new schema
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    /**
     * The SQL code for creating our weather table.
     */
    private static final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherEntry.TABLE_NAME +
            " (" + WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
            WeatherEntry.COLUMN_DATETEXT + " TEXT NOT NULL, " +
            WeatherEntry.COLUMN_SHORT_DESCRIPTION + " TEXT NOT NULL, " +
            WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL, " +             // TODO might change when we support other sources
            WeatherEntry.COLUMN_TEMPERATURE_HIGH + " REAL NOT NULL, " +
            WeatherEntry.COLUMN_TEMPERATURE_LOW + " REAL NOT NULL, " +
            WeatherEntry.COLUMN_HUMIDITY + " REAL, " +
            WeatherEntry.COLUMN_PRESSURE + " REAL, " +
            WeatherEntry.COLUMN_WIND_SPEED + " REAL, " +
            WeatherEntry.COLUMN_DEGREES + " REAL, " +

            // location column is foreign key into location table
            " FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
            LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), " +

            // ensure that we only store one weather entry per day per location
            " UNIQUE (" + WeatherEntry.COLUMN_DATETEXT + ", " +
            WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

    private static final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME +
            " (" + LocationEntry._ID + " INTEGER PRIMARY KEY," +
            LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, " +
            LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
            LocationEntry.COLUMN_LATITUDE + " REAL NOT NULL, " +
            LocationEntry.COLUMN_LONGITUDE + " REAL NOT NULL, " +
            "UNIQUE (" + LocationEntry.COLUMN_LOCATION_SETTING + ") ON CONFLICT IGNORE" +
            ");";

}


