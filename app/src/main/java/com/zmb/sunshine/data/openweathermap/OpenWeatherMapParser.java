package com.zmb.sunshine.data.openweathermap;

import com.zmb.sunshine.data.DayForecast;
import com.zmb.sunshine.data.DayOfWeek;
import com.zmb.sunshine.data.IWeatherDataParser;
import com.zmb.sunshine.data.WeatherParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Parses weather data received from Ope Weather ma.
 */
public class OpenWeatherMapParser implements IWeatherDataParser {

    @Override
    public List<DayForecast> parse(String data, int numberOfDays) throws WeatherParseException {
        try {
            List<DayForecast> forecasts = new ArrayList<DayForecast>();
            JSONObject json = new JSONObject(data);
            JSONArray days = json.getJSONArray("list");
            for (int i = 0; i < numberOfDays; ++i) {
                forecasts.add(parseDay(days.getJSONObject(i)));
            }
            return forecasts;
        } catch (JSONException e) {
            throw new WeatherParseException(data, e);
        }
    }

    private DayForecast parseDay(JSONObject day) throws JSONException {
        JSONObject temp = day.getJSONObject("temp");
        final double min = temp.getDouble("min");
        final double max = temp.getDouble("max");
        final String desc = day.getJSONArray("weather").getJSONObject(0).getString("main");

        // open weather map reports the date as a unix timestamp (seconds)
        // convert it to milliseconds to convert to a Date object
        long datetime = day.getLong("dt");
        Date date = new Date(datetime * 1000);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        return new DayForecast(max, min, getDayOfWeek(dayOfWeek), desc);
    }

    /**
     * Creates a {@link com.zmb.sunshine.data.DayOfWeek} given
     * an integer day.  Note the integer IDs used by {@code DayOfWeek}
     * are not the same as used by {@link java.util.Calendar}.
     * <p/>
     * {@code Calendar} uses Sunday(1) - Saturday(7).
     * @param fromCalendar the day code returned by the calendar
     * @return the day
     */
    private static DayOfWeek getDayOfWeek(int fromCalendar) {
        switch (fromCalendar) {
            case 1: return DayOfWeek.SUNDAY;
            case 2: return DayOfWeek.MONDAY;
            case 3: return DayOfWeek.TUESDAY;
            case 4: return DayOfWeek.WEDNESDAY;
            case 5: return DayOfWeek.THURSDAY;
            case 6: return DayOfWeek.FRIDAY;
            case 7: return DayOfWeek.SATURDAY;
            default: throw new IllegalArgumentException(
                    Integer.toString(fromCalendar) + " is not a valid day");
        }
    }
}
