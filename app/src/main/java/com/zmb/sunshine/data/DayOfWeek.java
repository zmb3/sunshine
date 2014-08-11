package com.zmb.sunshine.data;

/**
 * A day of the week, such as 'Tuesday'.
 */
public enum DayOfWeek {

    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
    SUNDAY(7);

    final int mValue;

    private DayOfWeek(int value) {
        mValue = value;
    }

    /**
     * Returns the ISO-8601 value for the day.
     * 1 (Monday) through 7 (Sunday).
     * @return
     */
    public int getValue() { return mValue; }

    public String toString() {
        switch (this) {
            case MONDAY: return "Monday";
            case TUESDAY: return "Tuesday";
            case WEDNESDAY: return "Wednesday";
            case THURSDAY: return "Thursday";
            case FRIDAY: return "Friday";
            case SATURDAY: return "Saturday";
            case SUNDAY: return "Sunday";
            default: return null;
        }
    }

}
