package com.vvaidya.weathershine.data;

import com.vvaidya.weathershine.data.WeatherContract.LocationEntry;
import com.vvaidya.weathershine.data.WeatherContract.WeatherEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class WeatherDataQueryHelper extends SQLiteOpenHelper{
	
	private static final int DB_VERSION = 1;
	public static final String DB_NAME = "weather.db";

	public WeatherDataQueryHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase sQLiteDatabase) {
		// TODO Auto-generated method stub
		 // Create a table to hold locations. A location consists of the string supplied in the
		// location setting, the city name, and the latitude and longitude
		 
		// TBD
		 
		final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherEntry.TABLE_NAME + " (" +
		// Why AutoIncrement here, and not above?
		// Unique keys will be auto-generated in either case. But for weather
		// forecasting, it's reasonable to assume the user will want information
		// for a certain date and all dates *following*, so the forecast data
		// should be sorted accordingly.
		WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
		 
		// the ID of the location entry associated with this weather data
		WeatherEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
		WeatherEntry.COLUMN_DATETEXT + " TEXT NOT NULL, " +
		WeatherEntry.COLUMN_SHORT_DESC + " TEXT NOT NULL, " +
		WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL," +
		 
		WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL, " +
		WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL, " +
		 
		WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL, " +
		WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL, " +
		WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
		WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL, " +
		 
		// Set up the location column as a foreign key to location table.
		" FOREIGN KEY (" + WeatherEntry.COLUMN_LOC_KEY + ") REFERENCES " +
		LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), " +
		 
		// To assure the application have just one weather entry per day
		// per location, it's created a UNIQUE constraint with REPLACE strategy
		" UNIQUE (" + WeatherEntry.COLUMN_DATETEXT + ", " +
		WeatherEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";
		
		
		final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
				
				LocationEntry._ID + " INTEGER PRIMARY KEY ," +
				
				//LocationEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
				
				LocationEntry.COLUMN_LOC_NAME + " TEXT NOT NULL, " +
				
				LocationEntry.COLUMN_POSTAL_CODE + " TEXT UNIQUE NOT NULL, " +
				LocationEntry.COLUMN_LOCATION_LAT + " REAL NOT NULL, " +
				LocationEntry.COLUMN_LOCATION_LONG + " REAL NOT NULL, " +
				"UNIQUE (" + LocationEntry.COLUMN_POSTAL_CODE +") ON CONFLICT IGNORE" +");";
		
		sQLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
		sQLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sQLiteDatabase, int arg1, int arg2) {
		// TODO Auto-generated method stub
		sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS" + LocationEntry.TABLE_NAME);
		sQLiteDatabase.execSQL(" DROP TABLE IF EXISTS" + WeatherEntry.TABLE_NAME);
		onCreate(sQLiteDatabase);
		
	}

}
