package com.vvaidya.weathershine.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class WeatherProvider extends ContentProvider {

	private static final int WEATHER = 100;
	private static final int WEATHER_WITH_LOCATION = 101;
	private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
	private static final int LOCATION = 300;
	private static final int LOCATION_ID = 301;

	public UriMatcher sURIMatcher = buildUriMatcher();

	private WeatherDataQueryHelper mDbHelper;

	private static UriMatcher buildUriMatcher() {
		final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = WeatherContract.CONTENT_AUTHORITY;

		sURIMatcher.addURI(authority, WeatherContract.PATH_WEATHER, WEATHER);
		sURIMatcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*",
				WEATHER_WITH_LOCATION);
		sURIMatcher.addURI(authority, WeatherContract.PATH_WEATHER + "/*/*",
				WEATHER_WITH_LOCATION_AND_DATE);
		sURIMatcher.addURI(authority, WeatherContract.PATH_LOCATION, LOCATION);
		sURIMatcher.addURI(authority, WeatherContract.PATH_LOCATION + "/#",
				LOCATION_ID);

		return sURIMatcher;

	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// Use the Uri Matcher to determine what kind of URI this is.
		final int match = sURIMatcher.match(uri);

		switch (match) {
		case WEATHER_WITH_LOCATION_AND_DATE:
			return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
		case WEATHER_WITH_LOCATION:
			return WeatherContract.WeatherEntry.CONTENT_TYPE;
		case WEATHER:
			return WeatherContract.WeatherEntry.CONTENT_TYPE;
		case LOCATION:
			return WeatherContract.LocationEntry.CONTENT_TYPE;
		case LOCATION_ID:
			return WeatherContract.LocationEntry.CONTENT_ITEM_TYPE;
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		mDbHelper = new WeatherDataQueryHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// Here's the switch statement that, given a URI, will determine what
		// kind of request it is,
		// and query the database accordingly.
		Cursor retCursor;
		switch (sURIMatcher.match(uri)) {
		// "weather/*/*"
		case WEATHER_WITH_LOCATION_AND_DATE: {
			retCursor = null;
			break;
		}
		// "weather/*"
		case WEATHER_WITH_LOCATION: {
			retCursor = null;
			break;
		}
		// "weather"
		case WEATHER: {
			retCursor = mDbHelper.getReadableDatabase().query(
					WeatherContract.WeatherEntry.TABLE_NAME, projection,
					selection, selectionArgs, null, null, sortOrder);
			break;
		}
		// "location/*"
		case LOCATION_ID: {
			retCursor = mDbHelper.getReadableDatabase().query(WeatherContract.LocationEntry.TABLE_NAME, projection, 
					WeatherContract.LocationEntry._ID +" = '"+ ContentUris.parseId(uri) +"'" , 
					null , 
					null, 
					null,
					sortOrder);
			break;
		}
		// "location"
		case LOCATION: {
			retCursor = mDbHelper.getReadableDatabase().query(
					WeatherContract.LocationEntry.TABLE_NAME, projection,
					selection, selectionArgs, null, null, sortOrder);
			break;
		}
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		
		retCursor.setNotificationUri(getContext().getContentResolver(), uri);
		return retCursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
