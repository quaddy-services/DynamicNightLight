package de.quaddyservices.dynamicnightlight;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragment {
//	implements
//		OnSharedPreferenceChangeListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);

		// http://stackoverflow.com/questions/531427/how-do-i-display-the-current-value-of-an-android-preference-in-the-preference-su
//		getPreferenceScreen().getSharedPreferences()
//				.registerOnSharedPreferenceChangeListener(this);

	}

//	/**
//	 * http://stackoverflow.com/questions/531427/how-do-i-display-the-current-value-of-an-android-preference-in-the-preference-su
//	 */
//	@Override
//	public void onResume() {
//		super.onResume();
//		for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); ++i) {
//			Preference preference = getPreferenceScreen().getPreference(i);
//			if (preference instanceof PreferenceGroup) {
//				PreferenceGroup preferenceGroup = (PreferenceGroup) preference;
//				for (int j = 0; j < preferenceGroup.getPreferenceCount(); ++j) {
//					updatePreference(preferenceGroup.getPreference(j));
//				}
//			} else {
//				updatePreference(preference);
//			}
//		}
//	}
//
//	@Override
//	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
//			String key) {
//		updatePreference(findPreference(key));
//	}
//
//	private void updatePreference(Preference preference) {
//		if (preference instanceof ListPreference) {
//			ListPreference listPreference = (ListPreference) preference;
//			CharSequence tempEntry = listPreference.getEntry();
//			if (tempEntry == null) {
//				listPreference.setSummary("50%");
//			} else {
//				//			CharSequence tempSummary = listPreference.getSummary();
//				listPreference.setSummary(tempEntry);
//			}
//		}
//	}

}
