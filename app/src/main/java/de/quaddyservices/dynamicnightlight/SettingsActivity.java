package de.quaddyservices.dynamicnightlight;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * http://developer.android.com/guide/topics/ui/settings.html
 *
 *
 */
public class SettingsActivity extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Log.i(getClass().getName(), "onCreate");
			// Display the fragment as the main content.
				getFragmentManager().beginTransaction()
						.replace(android.R.id.content, new SettingsFragment()).commit();

	}
}