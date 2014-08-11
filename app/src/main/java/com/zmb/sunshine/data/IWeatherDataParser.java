package com.zmb.sunshine.data;

import java.util.List;

/**
 * Parses weather data returned by a web service.
 */
public interface IWeatherDataParser {

    List<DayForecast> parse(String data, int numberOfDays) throws WeatherParseException;

}
