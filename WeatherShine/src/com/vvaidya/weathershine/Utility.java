package com.vvaidya.weathershine;
 
import java.text.DateFormat;
import java.util.Date;

import com.vvaidya.weathershine.data.WeatherContract;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

 
public class Utility {
    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default));
    }
    
    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.list_pref_key),
                context.getString(R.string.list_pref_default_value))
                .equals(context.getString(R.string.list_pref_default_value));
    }
 
    static String formatTemperature(double temperature, boolean isMetric) {
        double temp;
        if ( !isMetric ) {
            temp = 9*temperature/5+32;
        } else {
            temp = temperature;
        }
        return String.format("%.0f", temp);
    }
 
    static String formatDate(String dateString) {
        Date date = WeatherContract.getDateFromDb(dateString);
        return DateFormat.getDateInstance().format(date);
    }
}