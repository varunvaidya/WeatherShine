// Created by Varun Vaidya
package com.vvaidya.weathershine;


import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ActionProvider;
import android.support.v4.view.MenuCompat;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.os.Build;

public class DetailActivity extends Activity {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new DetailFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.detail, menu);
		return true;
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivity(new Intent(this,SettingsActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class DetailFragment extends Fragment {
		
		private static final String LOG_TAG = DetailFragment.class.getSimpleName();
		private static final String FORECAST_SHARE_HASHTAG = " #WeatherShine by Varun Vaidya";
		
		private String mForecastStr;

		public DetailFragment() {
			setHasOptionsMenu(true);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			Intent intent = getActivity().getIntent();
			View rootView = inflater.inflate(R.layout.fragment_detail,
					container, false);
			if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT))
			{
				mForecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
				Log.v("DetailActivity", "Clicked Item ---- "+mForecastStr);
				TextView text = (TextView) rootView.findViewById(R.id.detail_text);
				text.setText(mForecastStr);
			}
			else
			{
				Log.v("DetailActivity", "Intent is null or Epmty");
			}
			return rootView;
		}
		
		
		public void onCreateOptionsMenu(Menu menu , MenuInflater inflater) {
			// Inflate the menu; this adds items to the action bar if it is present.
			inflater.inflate(R.menu.detailfragment, menu);
			
			MenuItem share = menu.findItem(R.id.action_share);
			
			ShareActionProvider actionProvider = (ShareActionProvider) share.getActionProvider();
			
			if(actionProvider != null)
			{
				actionProvider.setShareIntent(createShareIntent());
			}
			else
			{
				Log.d(LOG_TAG, "Sharing failed");
			}
			
			//return true;
		}
		
		private Intent createShareIntent()
		{
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			//shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr+FORECAST_SHARE_HASHTAG);
			
			return shareIntent;
		}
		
		
	}
}
