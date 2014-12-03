package com.zmb.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A custom CursorAdapter that allows today's forecast
 * to use a different layout than the other days.
 */
public class ForecastAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE = 1;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public int getViewTypeCount() {
        // 2 different views - one for today, and one for other days
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int layout = getItemViewType(cursor.getPosition()) == VIEW_TYPE_TODAY ?
                R.layout.list_item_today :
                R.layout.list_item_forecast;
        return LayoutInflater.from(context)
                .inflate(layout, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);

        // use dummy image for now
        ImageView iv = (ImageView) view.findViewById(R.id.list_item_icon);
        iv.setImageResource(R.drawable.ic_launcher);

        String date = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
        TextView dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
        dateView.setText(Sunshine.friendlyDate(context, date));

        String desc = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        TextView descView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
        descView.setText(desc);

        boolean isMetric = Sunshine.isMetric(context);

        double maxTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_HIGH);
        TextView maxView = (TextView) view.findViewById(R.id.list_item_high_textview);
        maxView.setText(Sunshine.formatTemperature(maxTemp, isMetric));

        double minTemp = cursor.getDouble(ForecastFragment.COL_WEATHER_LOW);
        TextView minView = (TextView) view.findViewById(R.id.list_item_low_textview);
        minView.setText(Sunshine.formatTemperature(minTemp, isMetric));
    }
}
