package com.zmb.sunshine.data;

/**
 * Contains various conversions.
 */
public class Convert {
    private Convert() { }

    /**
     * Convert a temperature in celsius to degrees fahrenheit.
     * @param celsius
     * @return
     */
    public static double toFahrenheit(double celsius) {
        return celsius * 1.8 + 32d;
    }
    
}
