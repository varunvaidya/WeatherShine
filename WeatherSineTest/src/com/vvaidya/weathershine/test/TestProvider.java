package com.vvaidya.weathershine.test;

import java.util.Map;
import java.util.Set;

import com.vvaidya.weathershine.data.WeatherContract.LocationEntry;
import com.vvaidya.weathershine.data.WeatherContract.WeatherEntry;
import com.vvaidya.weathershine.data.WeatherDataQueryHelper;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

public class TestProvider extends AndroidTestCase {

	public static final String LOG_TAG = TestProvider.class.getSimpleName();

	static final String KALAMAZOO_LOCATION_SETTING = "kalamazoo";
	static final String KALAMAZOO_WEATHER_START_DATE = "20140625";

	long locationRowId;

	static ContentValues createKalamazooWeatherValues(long locationRowId) {
		ContentValues weatherValues = new ContentValues();
		weatherValues.put(WeatherEntry.COLUMN_LOC_KEY, locationRowId);
		weatherValues.put(WeatherEntry.COLUMN_DATETEXT,
				KALAMAZOO_WEATHER_START_DATE);
		weatherValues.put(WeatherEntry.COLUMN_DEGREES, 1.2);
		weatherValues.put(WeatherEntry.COLUMN_HUMIDITY, 1.5);
		weatherValues.put(WeatherEntry.COLUMN_PRESSURE, 1.1);
		weatherValues.put(WeatherEntry.COLUMN_MAX_TEMP, 85);
		weatherValues.put(WeatherEntry.COLUMN_MIN_TEMP, 35);
		weatherValues.put(WeatherEntry.COLUMN_SHORT_DESC, "Cats and Dogs");
		weatherValues.put(WeatherEntry.COLUMN_WIND_SPEED, 3.4);
		weatherValues.put(WeatherEntry.COLUMN_WEATHER_ID, 42);

		return weatherValues;
	}

	static ContentValues createKalamazooLocationValues() {
		// Create a new map of values, where column names are the keys
		ContentValues testValues = new ContentValues();
		testValues.put(LocationEntry.COLUMN_POSTAL_CODE,
				KALAMAZOO_LOCATION_SETTING);
		testValues.put(LocationEntry.COLUMN_LOC_NAME, "Kalamazoo");
		testValues.put(LocationEntry.COLUMN_LOCATION_LAT, 42.2917);
		testValues.put(LocationEntry.COLUMN_LOCATION_LONG, -85.5872);

		return testValues;
	}

	// Inserts both the location and weather data for the Kalamazoo data set.
	public void insertKalamazooData() {
		ContentValues kalamazooLocationValues = createKalamazooLocationValues();
		Uri locationInsertUri = mContext.getContentResolver().insert(
				LocationEntry.CONTENT_URI, kalamazooLocationValues);
		assertTrue(locationInsertUri != null);

		locationRowId = ContentUris.parseId(locationInsertUri);

		ContentValues kalamazooWeatherValues = createKalamazooWeatherValues(locationRowId);
		Uri weatherInsertUri = mContext.getContentResolver().insert(
				WeatherEntry.CONTENT_URI, kalamazooWeatherValues);
		assertTrue(weatherInsertUri != null);
	}

	public void testUpdateAndReadWeather() {
		insertKalamazooData();
		String newDescription = "Cats and Frogs (don't warn the tadpoles!)";

		// Make an update to one value.
		ContentValues kalamazooUpdate = new ContentValues();
		kalamazooUpdate.put(WeatherEntry.COLUMN_SHORT_DESC, newDescription);

		mContext.getContentResolver().update(WeatherEntry.CONTENT_URI,
				kalamazooUpdate, null, null);

		// A cursor is your primary interface to the query results.
		Cursor weatherCursor = mContext.getContentResolver().query(
				WeatherEntry.CONTENT_URI, null, null, null, null);

		// Make the same update to the full ContentValues for comparison.
		ContentValues kalamazooAltered = createKalamazooWeatherValues(locationRowId);
		kalamazooAltered.put(WeatherEntry.COLUMN_SHORT_DESC, newDescription);

		validateCursor(weatherCursor, kalamazooAltered);
	}

	public void testRemoveHumidityAndReadWeather() {
		insertKalamazooData();

		mContext.getContentResolver().delete(WeatherEntry.CONTENT_URI,
				WeatherEntry.COLUMN_HUMIDITY + " = " + locationRowId, null);

		// A cursor is your primary interface to the query results.
		Cursor weatherCursor = mContext.getContentResolver().query(
				WeatherEntry.CONTENT_URI, null, null, null, null);

		// Make the same update to the full ContentValues for comparison.
		ContentValues kalamazooAltered = createKalamazooWeatherValues(locationRowId);
		kalamazooAltered.remove(WeatherEntry.COLUMN_HUMIDITY);

		validateCursor(weatherCursor, kalamazooAltered);
	}

	public void deleteAllRecords() {
		mContext.getContentResolver().delete(WeatherEntry.CONTENT_URI, null,
				null);
		mContext.getContentResolver().delete(LocationEntry.CONTENT_URI, null,
				null);

		Cursor cursor = mContext.getContentResolver().query(
				WeatherEntry.CONTENT_URI, null, null, null, null);
		assertEquals(0, cursor.getCount());
		cursor.close();

		cursor = mContext.getContentResolver().query(LocationEntry.CONTENT_URI,
				null, null, null, null);
		assertEquals(0, cursor.getCount());
		cursor.close();
	}

	// Since we want each test to start with a clean slate, run deleteAllRecords
	// in setUp (called by the test runner before each test).
	public void setUp() {
		deleteAllRecords();
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
		values.put(LocationEntry.COLUMN_POSTAL_CODE, "99705");
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

	public void testInsertReadProvider() {

		// Create a new map of values, where column names are the keys
		ContentValues testValues = createNorthPoleLocationValues();

		long locationRowId;
		Uri uri = mContext.getContentResolver().insert(
				LocationEntry.CONTENT_URI, testValues);
		locationRowId = ContentUris.parseId(uri);

		Log.d(LOG_TAG, "New row id: " + locationRowId);

		// A cursor is your primary interface to the query results.
		Cursor cursor = mContext.getContentResolver().query(
				LocationEntry.buildLocationUri(locationRowId), // Table to Query
				null, null, // Columns for the "where" clause
				null, // Values for the "where" clause
				null // sort order
				);

		validateCursor(cursor, testValues);

		ContentValues weatherValues = createWeatherValues(locationRowId);

		uri = mContext.getContentResolver().insert(WeatherEntry.CONTENT_URI,
				weatherValues);
		long weatherRowId = ContentUris.parseId(uri);

		Log.v(LOG_TAG, "COntent provider insert Weather id =" + weatherRowId);

		Cursor weatherCursor = mContext.getContentResolver().query(
				WeatherEntry.CONTENT_URI, null, null, null, null);

		if (weatherCursor.moveToFirst())
			validateCursor(weatherCursor, weatherValues);

		weatherCursor.close();

		// A cursor is your primary interface to the query results.
		weatherCursor = mContext.getContentResolver().query(
				WeatherEntry.buildWeatherLocation(testDB.TEST_LOCATION_PCODE),
				null, null, null, null);

		if (weatherCursor.moveToFirst())
			validateCursor(weatherCursor, weatherValues);

		weatherCursor.close();

		weatherCursor = mContext.getContentResolver().query(
				WeatherEntry.buildWeatherLocationWithStartDate(
						testDB.TEST_LOCATION_PCODE, testDB.TEST_START_DATE),
				null, null, null, null);

		if (weatherCursor.moveToFirst())
			validateCursor(weatherCursor, weatherValues);

		weatherCursor.close();

		// get Weather using Postal Code and Date
		weatherCursor = mContext.getContentResolver().query(
				WeatherEntry.buildWeatherLocationWithDate(
						testDB.TEST_LOCATION_PCODE, testDB.TEST_START_DATE),
				null, null, null, null);

		if (weatherCursor.moveToFirst())
			validateCursor(weatherCursor, weatherValues);

		weatherCursor.close();

	}

	public void testUpdateLocation() {
		// Create a new map of values, where column names are the keys
		ContentValues values = createNorthPoleLocationValues();

		Uri locationUri = mContext.getContentResolver().insert(
				LocationEntry.CONTENT_URI, values);
		long locationRowId = ContentUris.parseId(locationUri);

		// Verify we got a row back.
		assertTrue(locationRowId != -1);
		Log.d(LOG_TAG, "New row id: " + locationRowId);

		ContentValues updatedValues = new ContentValues(values);
		updatedValues.put(LocationEntry._ID, locationRowId);
		updatedValues.put(LocationEntry.COLUMN_LOC_NAME, "Santa's Village");

		int count = mContext.getContentResolver().update(
				LocationEntry.CONTENT_URI, updatedValues,
				LocationEntry._ID + "= ?",
				new String[] { Long.toString(locationRowId) });

		assertEquals(count, 1);

		// A cursor is your primary interface to the query results.
		Cursor cursor = mContext.getContentResolver().query(
				LocationEntry.buildLocationUri(locationRowId), null, null, // Columns
																			// for
																			// the
																			// "where"
																			// clause
				null, // Values for the "where" clause
				null // sort order
				);

		validateCursor(cursor, updatedValues);
	}

	// Make sure we can still delete after adding/updating stuff
	public void testDeleteRecordsAtEnd() {
		deleteAllRecords();
	}

	public void testGetType() {
		// content://com.example.android.sunshine.app/weather/
		String type = mContext.getContentResolver().getType(
				WeatherEntry.CONTENT_URI);
		// vnd.android.cursor.dir/com.example.android.sunshine.app/weather
		assertEquals(WeatherEntry.CONTENT_TYPE, type);

		String testLocation = "94074";
		// content://com.example.android.sunshine.app/weather/94074
		type = mContext.getContentResolver().getType(
				WeatherEntry.buildWeatherLocation(testLocation));
		// vnd.android.cursor.dir/com.example.android.sunshine.app/weather
		assertEquals(WeatherEntry.CONTENT_TYPE, type);

		String testDate = "20140612";
		// content://com.example.android.sunshine.app/weather/94074/20140612
		type = mContext.getContentResolver().getType(
				WeatherEntry.buildWeatherLocationWithDate(testLocation,
						testDate));
		// vnd.android.cursor.item/com.example.android.sunshine.app/weather
		assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

		// content://com.example.android.sunshine.app/location/
		type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
		// vnd.android.cursor.dir/com.example.android.sunshine.app/location
		assertEquals(LocationEntry.CONTENT_TYPE, type);

		// content://com.example.android.sunshine.app/location/1
		type = mContext.getContentResolver().getType(
				LocationEntry.buildLocationUri(1L));
		// vnd.android.cursor.item/com.example.android.sunshine.app/location
		assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
	}

}
