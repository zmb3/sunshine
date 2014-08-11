package com.zmb.sunshine.data;

/**
 * This exception indicates that the weather data returned
 * by a web service couldn't be parsed.
 */
public class WeatherParseException extends Exception {

    private final String mData;
    private final Exception mInnerException;

    public WeatherParseException(String data, Exception innerException) {
        mData = data;
        mInnerException = innerException;
    }
}
