package com.zmb.sunshine.data;

/**
 * Contains various conversions.
 */
public class Convert {
    private Convert() { }

    /**
     * Convert a temperature in celcius to degrees fahrenheit.
     * @param celcius
     * @return
     */
    public static double toFahrenheit(double celcius) {
        return celcius * 1.8 + 32d;
    }
}
