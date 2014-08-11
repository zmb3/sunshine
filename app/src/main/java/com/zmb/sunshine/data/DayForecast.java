package com.zmb.sunshine.data;

/**
 * An object to represent a single day in the weekly forecast.
 * All units are metric.
 */
public class DayForecast {

    private final double mHighTemperature;
    private final double mLowTemperature;
    private final DayOfWeek mDay;
    private final String mDescription;

    public DayForecast(double high, double low, DayOfWeek day, String desc) {
        mHighTemperature = high;
        mLowTemperature = low;
        mDay = day;
        mDescription = desc;
    }

    /**
     * Get the high temperature for the day. (degrees Celsius)
     * @return
     */
    public double getHighTemperature() {
        return mHighTemperature;
    }

    /**
     * Get the low temperature for the day. (degrees Celsius)
     * @return
     */
    public double getLowTemperature() {
        return mLowTemperature;
    }

    public DayOfWeek getDay() {
        return mDay;
    }

    public String getDescription() {
        return mDescription;
    }

    @Override
    public String toString() {
        return toStringImperial();
    }

    public String toStringMetric() {
        return mDay.toString() + " - " + mDescription + " - " +
                (int)mHighTemperature + " / " + (int)mLowTemperature;
    }

    public String toStringImperial() {
        return mDay.toString() + " - " + mDescription + " - " +
                (int)Convert.toFahrenheit(mHighTemperature) + " / " +
                (int)Convert.toFahrenheit(mLowTemperature);
    }
}
