/*
 * Spartan Time Lapse Recorder - Minimalistic android time lapse recording app
 * Copyright (C) 2014  Andreas Rohner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.andreasrohner.spartantimelapserec;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import at.andreasrohner.spartantimelapserec.R;
import at.andreasrohner.spartantimelapserec.sensor.MuteShutter;

public class MainActivity extends Activity {

	private void setButtonToStop(Button button) {
		button.setText(R.string.button_stop);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_stop, 0, 0, 0);
		findViewById(R.id.button_preview).setEnabled(false);
	}

	private void setButtonToStart(Button button) {
		button.setText(R.string.button_start);
		button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_video, 0, 0, 0);
		findViewById(R.id.button_preview).setEnabled(true);
	}

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Context context = getApplicationContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SettingsCommon.setDefaultValues(context, prefs);

		// Display the fragment as the main content.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getFragmentManager().beginTransaction()
					.replace(android.R.id.content, new SettingsFragment())
					.commit();
		} else {
			setContentView(R.layout.main_activity_legacy);
			Button btn = (Button) findViewById(R.id.button_start);
			if (BackgroundService.isCreated()) {
				setButtonToStop(btn);
			} else {
				setButtonToStart(btn);
			}
		}
	}

	@SuppressLint("NewApi")
	public void actionStart(MenuItem item) {
		Intent intent = new Intent(MainActivity.this, BackgroundService.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startService(intent);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			invalidateOptionsMenu();
		finish();
	}

	@SuppressLint("NewApi")
	public void actionStop(MenuItem item) {
		stopService(new Intent(MainActivity.this, BackgroundService.class));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			invalidateOptionsMenu();
	}

	public void actionGallery(MenuItem item) {
		Intent intent = new Intent(Intent.ACTION_VIEW,
				Uri.parse("content://media/internal/images/media"));

		// test if app is available that can handle the intent
		if (intent.resolveActivity(getPackageManager()) != null) {
			startActivity(intent);
		}
	}

	public void actionPreview(MenuItem item) {
		Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
		startActivity(intent);
	}

	public void buttonGallery(View view) {
		actionGallery(null);
	}

	public void buttonPreview(View view) {
		actionPreview(null);
	}

	public void actionUnmuteAllStreams(MenuItem item) {
		MuteShutter mute = new MuteShutter(this);
		mute.maxAllStreams();
	}

	public void buttonStart(View view) {
		if (((Button) view).getText().equals(getString(R.string.button_stop))) {
			stopService(new Intent(MainActivity.this, BackgroundService.class));
			setButtonToStart((Button) view);
		} else {
			Context context = getApplicationContext();
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			// check if settings were shown at least once
			if (prefs.getBoolean("pref_settings_shown", false) == false) {
				prefs.edit().putBoolean("pref_settings_shown", true).commit();
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
			} else {
				setButtonToStop((Button) view);
				Intent intent = new Intent(this, BackgroundService.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startService(intent);
				finish();
			}
		}
	}

	public void buttonUnmuteAllStreams(View view) {
		actionUnmuteAllStreams(null);
	}

	public void buttonSettings(View view) {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_start).setEnabled(
				!BackgroundService.isCreated());
		menu.findItem(R.id.action_preview).setEnabled(
				!BackgroundService.isCreated());
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
