package com.zmb.sunshine.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses weather data returned by a web service.
 */
public interface IWeatherDataParser {

    /**
     * Stores deserialized weather data, after parsing.
     */
    public static class Result {

        private final List<DayForecast> days = new ArrayList<DayForecast>();
        private final String mCityName;
        private final double mLatitude;
        private final double mLongitude;

        public Result(String city, double lat, double lon) {
            this.mCityName = city;
            this.mLatitude = lat;
            this.mLongitude = lon;
        }

        public void addForecast(DayForecast day) {
            this.days.add(day);
        }

        public List<DayForecast> getDays() {
            return days;
        }

        public String getCityName() {
            return mCityName;
        }

        public double getLongitude() {
            return mLongitude;
        }

        public double getLatitude() {
            return mLatitude;
        }

    }

    public URL buildUrl(String locationSetting, int daysToFetch) throws MalformedURLException;

    // TODO: we don't need to return any results - just insert into the database
    public Result parse(String data, int numberOfDays) throws WeatherParseException;

}
