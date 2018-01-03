package de.quaddyservices.dynamicnightlight.util;

import android.content.Context;
import android.os.Build;
import android.preference.ListPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

/**
 * http://stackoverflow.com/questions/10119852/listpreferences-summary-text-is-not-updated-automatically-whenever-there-is-cha
 * 
 * @author user
 *
 */
public class ListPreferenceCompat extends ListPreference {

	public ListPreferenceCompat(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.d(getClass().getName(), "attrs=" + attrs);
	}

	public ListPreferenceCompat(Context context) {
		super(context);
	}

	// NOTE:
	// The framework forgot to call notifyChanged() in setValue() on previous versions of android.
	// This bug has been fixed in android-4.4_r0.7.
	// Commit: platform/frameworks/base/+/94c02a1a1a6d7e6900e5a459e9cc699b9510e5a2
	// Time: Tue Jul 23 14:43:37 2013 -0700
	//
	// However on previous versions, we have to workaround it by ourselves.
	@Override
	public void setValue(String value) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			super.setValue(value);
		} else {
			String oldValue = getValue();
			super.setValue(value);
			if (!TextUtils.equals(oldValue, value)) {
				notifyChanged();
			}
		}
	}

	@Override
	public void setSummary(CharSequence summary) {
		super.setSummary(summary);
		Log.d(getClass().getName(), "setSummary=" + summary);
	}

	@Override
	public CharSequence getSummary() {
		CharSequence tempSummary = super.getSummary();
		if (tempSummary==null || tempSummary.equals("%s") || tempSummary.equals("")) {
			tempSummary = "50%";
		}
		Log.d(getClass().getName(), "getSummary()=" + tempSummary);

		return tempSummary;
	}
}
