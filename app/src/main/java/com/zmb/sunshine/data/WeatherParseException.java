package com.zmb.sunshine.data;

import java.io.IOException;

/**
 * This exception indicates that the weather data returned
 * by a web service couldn't be parsed.
 */
public class WeatherParseException extends IOException {

    private final String mData;
    private final Exception mInnerException;

    public WeatherParseException(String data, Exception innerException) {
        mData = data;
        mInnerException = innerException;
    }
}
