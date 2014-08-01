package com.vvaidya.weathershine.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class WeatherProvider extends ContentProvider {

	private static final int WEATHER = 100;
	private static final int WEATHER_WITH_LOCATION = 101;
	private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
	private static final int LOCATION = 300;
	private static final int LOCATION_ID = 301;
	
	

	public UriMatcher sURIMatcher = buildUriMatcher();

	private WeatherDataQueryHelper mDbHelper;

	private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

	static {
		sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
		sWeatherByLocationSettingQueryBuilder
				.setTables(WeatherContract.WeatherEntry.TABLE_NAME
						+ " INNER JOIN "
						+ WeatherContract.LocationEntry.TABLE_NAME + " ON "
						+ WeatherContract.WeatherEntry.TABLE_NAME + "."
						+ WeatherContract.WeatherEntry.COLUMN_LOC_KEY + " = "
						+ WeatherContract.LocationEntry.TABLE_NAME + "."
						+ WeatherContract.LocationEntry._ID);
	}

	private static final String sLocationSettingSelection = WeatherContract.LocationEntry.TABLE_NAME
			+ "." + WeatherContract.LocationEntry.COLUMN_POSTAL_CODE + " = ?";

	private static final String sLocationSettingWithStartDateSelection = WeatherContract.LocationEntry.TABLE_NAME
			+ "."
			+ WeatherContract.LocationEntry.COLUMN_POSTAL_CODE
			+ " = ? AND "
			+ WeatherContract.WeatherEntry.COLUMN_DATETEXT
			+ " >= ? ";

	private static final String sLocationSettingAndDateSelection = WeatherContract.LocationEntry.TABLE_NAME
			+ "."
			+ WeatherContract.LocationEntry.COLUMN_POSTAL_CODE
			+ " = ? AND "
			+ WeatherContract.WeatherEntry.COLUMN_DATETEXT
			+ " = ? ";

	private Cursor getWeatherByLocationSetting(Uri uri, String[] projection,
			String sortOrder) {
		String locationSetting = WeatherContract.WeatherEntry
				.getLocationSettingFromUri(uri);
		String startDate = WeatherContract.WeatherEntry
				.getStartDateFromUri(uri);
		String selection = null;
		String selectionArgs[] = null;
		if (startDate != null) {
			selection = sLocationSettingWithStartDateSelection;
			selectionArgs = new String[] { locationSetting, startDate };
		} else {
			selection = sLocationSettingSelection;
			selectionArgs = new String[] { locationSetting };
		}

		return sWeatherByLocationSettingQueryBuilder.query(
				mDbHelper.getReadableDatabase(), projection, selection,
				selectionArgs, null, null, sortOrder);
	}

	private Cursor getWeatherByLocationSettingAndDate(Uri uri,
			String[] projection, String sortOrder) {
		String locationSetting = WeatherContract.WeatherEntry
				.getLocationSettingFromUri(uri);
		String startDate = WeatherContract.WeatherEntry.getDateFromUri(uri);
		String[] selectionArgs = null;
		if (startDate != null) {
			selectionArgs = new String[] { locationSetting, startDate };
		} else {
			selectionArgs = new String[] { locationSetting };
		}

		return sWeatherByLocationSettingQueryBuilder.query(
				mDbHelper.getReadableDatabase(), projection,
				sLocationSettingAndDateSelection, selectionArgs, null, null,
				sortOrder);
	}

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

		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		final int match = sURIMatcher.match(uri);
		int rowsUpdated = 0;
		switch (match) {
		case WEATHER: {
			rowsUpdated = db.delete(WeatherContract.WeatherEntry.TABLE_NAME,
					selection, selectionArgs);
			break;
		}
		case LOCATION: {

			rowsUpdated = db.delete(WeatherContract.LocationEntry.TABLE_NAME,
					selection, selectionArgs);
			break;
		}
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
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

		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		final int match = sURIMatcher.match(uri);
		Uri returnUri;

		switch (match) {
		case WEATHER: {
			long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null,
					values);
			if (_id > 0)
				returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
			else
				throw new android.database.SQLException(
						"Failed to insert row into " + uri);
			break;
		}
		case LOCATION: {

			long _id = db.insert(WeatherContract.LocationEntry.TABLE_NAME,
					null, values);
			if (_id > 0)
				returnUri = WeatherContract.LocationEntry.buildLocationUri(_id);
			else
				throw new android.database.SQLException(
						"Failed to insert row into " + uri);
			break;
		}
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return returnUri;
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
			retCursor = getWeatherByLocationSettingAndDate(uri, projection,
					sortOrder);
			break;
		}
		// "weather/*"
		case WEATHER_WITH_LOCATION: {
			retCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
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
			retCursor = mDbHelper.getReadableDatabase().query(
					WeatherContract.LocationEntry.TABLE_NAME,
					projection,
					WeatherContract.LocationEntry._ID + " = '"
							+ ContentUris.parseId(uri) + "'", null, null, null,
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
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		final int match = sURIMatcher.match(uri);
		int rowsUpdated = 0;
		switch (match) {
		case WEATHER: {
			rowsUpdated = db.update(WeatherContract.WeatherEntry.TABLE_NAME,
					values, selection, selectionArgs);
			break;
		}
		case LOCATION: {

			rowsUpdated = db.update(WeatherContract.LocationEntry.TABLE_NAME,
					values, selection, selectionArgs);
			break;
		}
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();
		final int match = sURIMatcher.match(uri);
		switch (match) {
		case WEATHER:
			db.beginTransaction();
			int returnCount = 0;
			try {
				for (ContentValues value : values) {
					long _id = db.insert(
							WeatherContract.WeatherEntry.TABLE_NAME, null,
							value);
					if (_id != -1) {
						returnCount++;
					}
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
			getContext().getContentResolver().notifyChange(uri, null);
			return returnCount;
		default:
			return super.bulkInsert(uri, values);
		}
	}

	
}
