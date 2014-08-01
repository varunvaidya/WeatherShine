package com.vvaidya.weathershine;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ArrayAdapter;
 
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vvaidya.weathershine.data.WeatherContract;
import com.vvaidya.weathershine.data.WeatherContract.LocationEntry;
import com.vvaidya.weathershine.data.WeatherContract.WeatherEntry;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
 
public class FetchWeatherTask extends AsyncTask<String, Void, Void> {
 
    private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
 
   
    private final Context mContext;
 
    public FetchWeatherTask(Context context) {
        mContext = context;
       
    }
 
    /* The date/time conversion code is going to be moved outside the asynctask later,
     * so for convenience we're breaking it out into its own method now.
     */
    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        Date date = new Date(time * 1000);
        SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
        return format.format(date).toString();
    }
 
    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // Data is fetched in Celsius by default.
        // If user prefers to see in Fahrenheit, convert the values here.
        // We do this rather than fetching in Fahrenheit so that the user can
        // change this option without us having to re-fetch the data once
        // we start storing the values in a database.
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        String unitType = sharedPrefs.getString(
                mContext.getString(R.string.list_pref_key),
                mContext.getString(R.string.list_pref_default_value));
 
        if (unitType.equals(mContext.getString(R.string.list_pref_imperial_value))) {
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;
        } else if (!unitType.equals(mContext.getString(R.string.list_pref_default_value))) {
            Log.d(LOG_TAG, "Unit type not found: " + unitType);
        }
 
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);
 
        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }
 
    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays , String locationQuery)
            throws JSONException {
 
        // These are the names of the JSON objects that need to be extracted.
    	// Location 
    	final String OWM_CITY = "city";
    	final String OWM_CITY_NAME = "name";
        final String OWM_COORD = "coord";
        final String OWM_LATITUDE = "lat";
        final String OWM_LONGITUDE = "lon";
        final String OWM_PRESSURE = "pressure";
    	final String OWM_HUMIDITY = "humidity";
        final String OWM_WINDSPEED = "speed";
        final String OWM_WIND_DIRECTION = "deg";
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DATETIME = "dt";
        final String OWM_DESCRIPTION = "main";
        final String OWM_WEATHER_ID = "id";
        long locationId = 0;
       
 
        JSONArray weatherArray = null;
		try {
			JSONObject forecastJson = new JSONObject(forecastJsonStr);
			weatherArray = forecastJson.getJSONArray(OWM_LIST);
			
			JSONObject cityJson = forecastJson.getJSONObject(OWM_CITY);
			String cityName = cityJson.getString(OWM_CITY_NAME);
			
			JSONObject cityCoord = cityJson.getJSONObject(OWM_COORD);
			double cityLat = cityCoord.getDouble(OWM_LATITUDE);
			double cityLon = cityCoord.getDouble(OWM_LONGITUDE);
			
			locationId = addLocation(locationQuery, cityName, cityLat, cityLon);
			
		} catch (Exception e) {
			Log.e("FetchWeatherTask","getWeatherDataFromJson :: exception" +e.getMessage());
			e.printStackTrace();
		}
 
		 // Get and insert the new weather information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(weatherArray.length());
 
        String[] resultStrs = new String[numDays];
 
        for(int i = 0; i < weatherArray.length(); i++) {
            // These are the values that will be collected.
 
            long dateTime;
            double pressure;
            int humidity;
            double windSpeed;
            double windDirection;
 
            double high;
            double low;
 
            String description;
            int weatherId;
 
            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);
 
            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            dateTime = dayForecast.getLong(OWM_DATETIME);
 
            pressure = dayForecast.getDouble(OWM_PRESSURE);
            humidity = dayForecast.getInt(OWM_HUMIDITY);
            windSpeed = dayForecast.getDouble(OWM_WINDSPEED);
            windDirection = dayForecast.getDouble(OWM_WIND_DIRECTION);
 
            // Description is in a child array called "weather", which is 1 element long.
            // That element also contains a weather code.
            JSONObject weatherObject =
                    dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);
            weatherId = weatherObject.getInt(OWM_WEATHER_ID);
 
            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            high = temperatureObject.getDouble(OWM_MAX);
            low = temperatureObject.getDouble(OWM_MIN);
 
            ContentValues weatherValues = new ContentValues();
 
            weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationId);
            weatherValues.put(WeatherEntry.COLUMN_DATETEXT,
                    WeatherContract.getDbDateString(new Date(dateTime * 1000L)));
            weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, humidity);
            weatherValues.put(WeatherEntry.COLUMN_PRESSURE, pressure);
            weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, windSpeed);
            weatherValues.put(WeatherEntry.COLUMN_DEGREES, windDirection);
            weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, high);
            weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, low);
            weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, description);
            weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, weatherId);
 
            cVVector.add(weatherValues);
            
           
            /*String highAndLow = formatHighLows(high, low);
            String day = getReadableDateString(dateTime);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;*/
        }
        ContentValues[] cValues = (ContentValues[]) cVVector.toArray();
        int updatedRows = mContext.getContentResolver().bulkInsert(WeatherContract.WeatherEntry.CONTENT_URI, cValues);
       Log.v(LOG_TAG, "No of rows updated in Weather Table are "+updatedRows);
        return resultStrs;
 
    }
    @Override
    protected Void doInBackground(String... params) {
    	
    	 String locationQuery = params[0];
 
        // If there's no zip code, there's nothing to look up.  Verify size of params.
       /* if (params.length == 0) {
            return null;
        }*/
 
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
 
        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;
 
        String format = "json";
        String units = "metric";
        int numDays = 14;
 
        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            final String FORECAST_BASE_URL =
                    "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "q";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
 
            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, params[0])
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .build();
 
            URL url = new URL(builtUri.toString());
 
            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();
 
            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
               
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
 
            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }
 
            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
            	 Log.d(LOG_TAG, "buffer.length() == 0");
              
            }
            forecastJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
		return null;
 
     
    }
 
   
    
    private long addLocation(String postalCode, String cityName, double lat,
			double lon) {
    	
    	Log.v(LOG_TAG, "inserting "+cityName+" "+postalCode+" in db" );
    	
    	Cursor cursor = mContext.getContentResolver().query(WeatherContract.LocationEntry.CONTENT_URI,
    			new String[]{LocationEntry._ID}, 
    			LocationEntry.COLUMN_POSTAL_CODE + "= ?", 
    			new String[]{postalCode}, 
    			null);
    	
    	if(cursor.moveToFirst())
    	{
    		Log.v(LOG_TAG, "Location found in database");
    		int locationColIndex = cursor.getColumnIndex(LocationEntry._ID);
    		return cursor.getLong(locationColIndex);
    	}
    	else
    	{
    		Log.v(LOG_TAG, "Didnt find Location in database. Inserting now!");
    		ContentValues values = new ContentValues();
    		values.put(LocationEntry.COLUMN_POSTAL_CODE, postalCode);
    		values.put(LocationEntry.COLUMN_LOC_NAME, cityName);
    		values.put(LocationEntry.COLUMN_LOCATION_LAT, lat);
    		values.put(LocationEntry.COLUMN_LOCATION_LONG, lon);
    		
    		Uri uri = mContext.getContentResolver().insert(WeatherContract.LocationEntry.CONTENT_URI, values);
    		long locationId = ContentUris.parseId(uri);
    		return locationId;
    	}

		
	}

}