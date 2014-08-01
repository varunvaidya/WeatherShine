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

import com.vvaidya.weathershine.data.WeatherContract;
import com.vvaidya.weathershine.data.WeatherContract.LocationEntry;
import com.vvaidya.weathershine.data.WeatherContract.WeatherEntry;

import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements
		LoaderCallbacks<Cursor> {

	private SimpleCursorAdapter mForecastAdapter;
	private static final int FORECAST_LOADER = 0;
	private String mLocation;

	// For the forecast view we're showing only a small subset of the stored
	// data.
	// Specify the columns we need.
	private static final String[] FORECAST_COLUMNS = {
			// In this case the id needs to be fully qualified with a table
			// name, since
			// the content provider joins the location & weather tables in the
			// background
			// (both have an _id column)
			// On the one hand, that's annoying. On the other, you can search
			// the weather table
			// using the location set by the user, which is only in the Location
			// table.
			// So the convenience is worth it.
			WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
			WeatherEntry.COLUMN_DATETEXT, WeatherEntry.COLUMN_SHORT_DESC,
			WeatherEntry.COLUMN_MAX_TEMP, WeatherEntry.COLUMN_MIN_TEMP,
			LocationEntry.COLUMN_POSTAL_CODE };

	// These indices are tied to FORECAST_COLUMNS. If FORECAST_COLUMNS changes,
	// these
	// must change.
	public static final int COL_WEATHER_ID = 0;
	public static final int COL_WEATHER_DATE = 1;
	public static final int COL_WEATHER_DESC = 2;
	public static final int COL_WEATHER_MAX_TEMP = 3;
	public static final int COL_WEATHER_MIN_TEMP = 4;
	public static final int COL_LOCATION_SETTING = 5;

	public ForecastFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_main, container,
				false);

		 mForecastAdapter = new SimpleCursorAdapter(
	                getActivity(),
	                R.layout.list_forecast_item,
	                null,
	                // the column names to use to fill the textviews
	                new String[]{WeatherContract.WeatherEntry.COLUMN_DATETEXT,
	                        WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
	                        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
	                        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
	                },
	                // the textviews to fill with the data pulled from the columns above
	                new int[]{R.id.list_item_date_textview,
	                        R.id.list_item_forecast_textview,
	                        R.id.list_item_high_textview,
	                        R.id.list_item_low_textview
	                },
	                0
	        );
		ListView listView = (ListView) rootView
				.findViewById(R.id.listView_forecast);
		listView.setAdapter(mForecastAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {

				/*String forecast = mForecastAdapter.getItem(position);
				// Toast.makeText(getActivity(), forecast,
				// Toast.LENGTH_SHORT).show();
				Intent detailIntent = new Intent(getActivity(),
						DetailActivity.class).putExtra(Intent.EXTRA_TEXT,
						forecast);
				startActivity(detailIntent);
				Log.v("ForecastFragment", "Intent executed Successfully");*/
				Intent intent = new Intent(getActivity(), DetailActivity.class)
	            .putExtra(Intent.EXTRA_TEXT, "placeholder");
				startActivity(intent);

			}
		});
		
		 mForecastAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
	            @Override
	            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
	                boolean isMetric = Utility.isMetric(getActivity());
	                switch (columnIndex) {
	                    case COL_WEATHER_MAX_TEMP:
	                    case COL_WEATHER_MIN_TEMP: {
	                        // we have to do some formatting and possibly a conversion
	                        ((TextView) view).setText(Utility.formatTemperature(
	                                cursor.getDouble(columnIndex), isMetric));
	                        return true;
	                    }
	                    case COL_WEATHER_DATE: {
	                        String dateString = cursor.getString(columnIndex);
	                        TextView dateView = (TextView) view;
	                        dateView.setText(Utility.formatDate(dateString));
	                        return true;
	                    }
	                }
	                return false;
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
	public void onStart() {
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
	 * Update the weather.
	 */
	private void updateWeather() {

		FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getActivity());
		String location = Utility.getPreferredLocation(getActivity());
		fetchWeatherTask.execute(location);

	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    getLoaderManager().initLoader(FORECAST_LOADER, null, this);
	    super.onActivityCreated(savedInstanceState);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		  // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.
 
        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.
        String startDate = WeatherContract.getDbDateString(new Date());
 
        // Sort order:  Ascending, by date.
        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";
 
        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
                mLocation, startDate);
 
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		
		 mForecastAdapter.swapCursor(data);
		    if ( !mLocation.equals(Utility.getPreferredLocation(getActivity())) ) {
		        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
		    }
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		
		mForecastAdapter.swapCursor(null);

	}

}