package com.vvaidya.weathershine.test;

import java.util.Map;
import java.util.Set;

import com.vvaidya.weathershine.data.WeatherContract.LocationEntry;
import com.vvaidya.weathershine.data.WeatherContract.WeatherEntry;
import com.vvaidya.weathershine.data.WeatherDataQueryHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

public class testDB extends AndroidTestCase {

	public static final String LOG_TAG = testDB.class.getSimpleName();
	
	public static final String TEST_LOCATION = "North Pole";
	public static final String TEST_LOCATION_PCODE = "94043";
	public static final String TEST_START_DATE = "20141205";

	public void testCreateDb() throws Throwable {
		mContext.deleteDatabase(WeatherDataQueryHelper.DB_NAME);
		SQLiteDatabase database = new WeatherDataQueryHelper(this.mContext)
				.getWritableDatabase();
		assertEquals(true, database.isOpen());
		database.close();
	}

	static ContentValues createWeatherValues(long locationRowId) {
		
		
		ContentValues weatherValues = new ContentValues();
		weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
		weatherValues.put(WeatherEntry.COLUMN_DATETEXT, "20141205");
		weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.1);
		weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.2);
		weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.3);
		weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 75);
		weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 65);
		weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
		weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 5.5);
		weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 321);

		return weatherValues;
	}

	static ContentValues createNorthPoleLocationValues() {
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(LocationEntry.COLUMN_POSTAL_CODE,  "99705");
		values.put(LocationEntry.COLUMN_LOC_NAME, "North Pole");
		values.put(LocationEntry.COLUMN_LOCATION_LAT, 64.485);
		values.put(LocationEntry.COLUMN_LOCATION_LONG, -143.543);

		return values;
	}

	static void validateCursor(Cursor valueCursor, ContentValues expectedValues) {

		assertTrue(valueCursor.moveToFirst());

		Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
		for (Map.Entry<String, Object> entry : valueSet) {
			String columnName = entry.getKey();
			int idx = valueCursor.getColumnIndex(columnName);
			assertFalse(idx == -1);
			String expectedValue = entry.getValue().toString();
			assertEquals(expectedValue, valueCursor.getString(idx));
		}
		valueCursor.close();
	}

	public void testInsertReadDb() {
		
		WeatherDataQueryHelper dbHelper = new WeatherDataQueryHelper(mContext);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		// Create a new map of values, where column names are the keys
		 ContentValues testValues = createNorthPoleLocationValues();
		 
		long locationRowId;
		locationRowId = db.insert(LocationEntry.TABLE_NAME, null, testValues);
		// Verify we got a row back.
		assertTrue(locationRowId != -1);
		Log.d(LOG_TAG, "New row id: " + locationRowId);
		
		
		// A cursor is your primary interface to the query results.
		Cursor cursor = db.query(LocationEntry.TABLE_NAME, // Table to Query
				null, 
				null, // Columns for the "where" clause
				null, // Values for the "where" clause
				null, // columns to group by
				null, // columns to filter by row groups
				null // sort order
				);
		
		 validateCursor(cursor, testValues);
		 
		 ContentValues weatherValues = createWeatherValues(locationRowId);
		 
		 long weatherRowId = db.insert(WeatherEntry.TABLE_NAME, null, weatherValues);
		 assertTrue(weatherRowId != -1);
		 
		 Cursor weatherCursor = mContext.getContentResolver().query(WeatherEntry.CONTENT_URI, 
				 null,
				 null, 
				 null, 
				 null);

		if(weatherCursor.moveToFirst())
		validateCursor(weatherCursor, weatherValues);
		
		weatherCursor.close();
		  
		 weatherCursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocation(testDB.TEST_LOCATION_PCODE), 
				 null,
				 null, 
				 null, 
				 null);

		if(weatherCursor.moveToFirst())
		validateCursor(weatherCursor, weatherValues);
		
		weatherCursor.close();
		
		weatherCursor = mContext.getContentResolver().query(WeatherEntry.buildWeatherLocationWithStartDate(testDB.TEST_LOCATION_PCODE, testDB.TEST_START_DATE), null, null, null, null);

		if(weatherCursor.moveToFirst())
		validateCursor(weatherCursor, weatherValues);
		
		weatherCursor.close();

		

		dbHelper.close();

	}

}
