package com.vvaidya.weathershine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

	ArrayAdapter<String> arrayAdapter = null;
    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        
                  
		 arrayAdapter = new ArrayAdapter<String>(
		  getActivity(),
		  R.layout.list_forecast_item , 
		  R.id.list_item_forecast_textView ,
		  new ArrayList<String>());
		  ListView listView = (ListView) rootView.findViewById(R.id.listView_forecast);
		  listView.setAdapter(arrayAdapter);
		  listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				
				String forecast = arrayAdapter.getItem(position);
				//Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
				Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
										.putExtra(Intent.EXTRA_TEXT, forecast);
				startActivity(detailIntent);
				Log.v("ForecastFragment", "Intent executed Successfully");
				
			}
		});
		
        
        return rootView;
    }
    
   @Override
    public void onCreate(Bundle savedInstanceState) {
    	
    	 super.onCreate(savedInstanceState);
    	 setHasOptionsMenu(true);
    }
    
    
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
       
    	menuInflater.inflate(R.menu.forecastfragment, menu);
    }
   
    @Override
    public void onStart()
    {
    	super.onStart();
    	updateWeather();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
        	
        	updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    /**
	 * Update  the weather.
	 */
	private void updateWeather() {
		
		FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
    	String location = settings.getString(getString(R.string.edit_pref_key),getString(R.string.edit_pref_default_value));
    	fetchWeatherTask.execute(location);
	   
	}
    
    public class FetchWeatherTask extends AsyncTask<String,Void,String[]>{

    	public final String LOG_CON = FetchWeatherTask.class.getSimpleName();
    	
		@Override
		protected String[] doInBackground(String... params)
		{

			// These two need to be declared outside the try/catch
	        // so that they can be closed in the finally block.
	        HttpURLConnection urlConnection = null;
	        BufferedReader reader = null;
	         
	        // Will contain the raw JSON response as a string.
	        String forecastJsonStr = null;
	        
	        String formatString   = "JSON";
	        String unitsParam = "metric";
	        int cnt = 7;
	        String[] weatherStr = null;
	         
	        try {
	            // Construct the URL for the OpenWeatherMap query
	            // Possible parameters are avaiable at OWM's forecast API page, at
	            // http://openweathermap.org/API#forecast
	        	final String BASE_URL_FORMAT = "http://api.openweathermap.org/data/2.5/forecast/daily?";
	        	final String QUERY_PARAM ="q";
	        	final String FORMAT_PARAM = "mode";
	        	final String UNITS_PARAM = "units";
	        	final String DAYS_PARAM = "cnt";
	        	
	        	Uri uriBuild = Uri.parse(BASE_URL_FORMAT).buildUpon()
	        			.appendQueryParameter(QUERY_PARAM, params[0])
	        			.appendQueryParameter(FORMAT_PARAM, formatString)
	        			.appendQueryParameter(UNITS_PARAM, unitsParam)
	        			.appendQueryParameter(DAYS_PARAM, Integer.toString(cnt))
	        			.build();
	        	
	        	
	            URL url = new URL(uriBuild.toString());
	         
	            Log.v(LOG_CON, "URL BUILT = "+uriBuild.toString());
	            
	            // Create the request to OpenWeatherMap, and open the connection
	            urlConnection = (HttpURLConnection) url.openConnection();
	            urlConnection.setRequestMethod("GET");
	            urlConnection.connect();
	         
	            // Read the input stream into a String
	            InputStream inputStream = urlConnection.getInputStream();
	            StringBuffer buffer = new StringBuffer();
	            if (inputStream == null) {
	                // Nothing to do.
	                forecastJsonStr = null;
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
	                forecastJsonStr = null;
	            }
	            forecastJsonStr = buffer.toString();	
	            
	  
	            //Log.v(LOG_CON, "JSON String = "+forecastJsonStr);
	            
	        } catch (IOException e) {
	            Log.e(LOG_CON, "Error ", e);
	            // If the code didn't successfully get the weather data, there's no point in attemping
	            // to parse it.
	            forecastJsonStr = null;
	        } finally{
	            if (urlConnection != null) {
	                urlConnection.disconnect();
	            }
	            if (reader != null) {
	                try {
	                    reader.close();
	                } catch (final IOException e) {
	                    Log.e(LOG_CON, "Error closing stream", e);
	                }
	            }
	        }
	        
	        try {
				weatherStr = getWeatherDataFromJson(forecastJsonStr,cnt);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				  Log.e(LOG_CON, "JSON EXCEPTION Error "+ e.getMessage());
			}
	        
			return weatherStr;
	    	
		}
		
		@Override
		protected void onPostExecute(String[] result)
		{
			  if(result != null)
			  {
				  arrayAdapter.clear();				 
				  arrayAdapter.addAll(result);
				  
			  }
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
		   
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
	    	String unitType = settings.getString(getString(R.string.list_pref_key),getString(R.string.list_pref_default_value));
			
	    	if(unitType.equals(getString(R.string.list_pref_imperial_value)))
	    	{
	    		high = (high * 1.8) + 32;
	    		low = (low * 1.8) + 32;
	    	}
	    	else if(!unitType.equals(getString(R.string.list_pref_default_value)))
	    	{
	    		Log.e(LOG_CON, "Temperature Units Not Found "+unitType);
	    	}
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
		private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
		        throws JSONException {
		 
		    // These are the names of the JSON objects that need to be extracted.
		    final String OWM_LIST = "list";
		    final String OWM_WEATHER = "weather";
		    final String OWM_TEMPERATURE = "temp";
		    final String OWM_MAX = "max";
		    final String OWM_MIN = "min";
		    final String OWM_DATETIME = "dt";
		    final String OWM_DESCRIPTION = "main";
		 
		    JSONObject forecastJson = new JSONObject(forecastJsonStr);
		    JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
		 
		    String[] resultStrs = new String[numDays];
		    for(int i = 0; i < weatherArray.length(); i++) {
		        // For now, using the format "Day, description, hi/low"
		        String day;
		        String description;
		        String highAndLow;
		 
		        // Get the JSON object representing the day
		        JSONObject dayForecast = weatherArray.getJSONObject(i);
		 
		        // The date/time is returned as a long.  We need to convert that
		        // into something human-readable, since most people won't read "1400356800" as
		        // "this saturday".
		        long dateTime = dayForecast.getLong(OWM_DATETIME);
		        day = getReadableDateString(dateTime);
		 
		        // description is in a child array called "weather", which is 1 element long.
		        JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
		        description = weatherObject.getString(OWM_DESCRIPTION);
		 
		        // Temperatures are in a child object called "temp".  Try not to name variables
		        // "temp" when working with temperature.  It confuses everybody.
		        JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
		        double high = temperatureObject.getDouble(OWM_MAX);
		        double low = temperatureObject.getDouble(OWM_MIN);
		 
		        highAndLow = formatHighLows(high, low);
		        resultStrs[i] = day + " - " + description + " - " + highAndLow;
		    }
		    
		    for(String s: resultStrs)
		    {
		    	Log.v(LOG_CON, "Forecast entry: "+s);
		    }
		 
		    return resultStrs;
		}
    	
    }
}