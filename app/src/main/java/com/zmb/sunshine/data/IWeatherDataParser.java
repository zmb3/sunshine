package com.zmb.sunshine.data;

import android.content.Context;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Parses weather data returned by a web service.
 */
public interface IWeatherDataParser {

    /**
     * Builds the URL to query for weather data.
     *
     * @param locationSetting the location to query for data
     * @param daysToFetch the number of days of data to query for
     * @return the URL to query
     * @throws MalformedURLException
     */
    public URL buildUrl(String locationSetting, int daysToFetch) throws MalformedURLException;

    /**
     * This method is responsible for parsing the API response
     * and inserting the resulting data into the database.
     *
     * @param c
     * @param data the API response
     * @param numberOfDays the number of days worth of data to parse
     * @throws WeatherParseException if an error occurs
     */
    public void parse(Context c, String data, int numberOfDays) throws WeatherParseException;

}
