package de.quaddyservices.dynamicnightlight;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import de.quaddyservices.dynamicnightlight.util.SystemUiHider;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity {

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	private Runnable runnable;

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			delayedHide(3000);
			return false;
		}
	};
	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};
	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	private BroadcastReceiver powerConnectionReceiver;

	private boolean keepOn;

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(getClass().getName(), "onCreate...");

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_fullscreen);

		// final View controlsView =
		// findViewById(R.id.fullscreen_content_controls);
		final View contentView = findViewById(R.id.fullscreen_content);

		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		delayedHide(100);
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						// Schedule a hide().
						delayedHide(3000);
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mSystemUiHider.toggle();
			}
		});

		onCreateNightLight();
		nextCheckBattery = 0;
		checkBattery();
		initPowerConnectionReceiver();
	}

	private long nextCheckBattery = 0;

	private void checkBattery() {
		if (nextCheckBattery < System.currentTimeMillis()) {
			nextCheckBattery = System.currentTimeMillis() + 60000;
			if (isBatteryCharging()) {
				setKeepScreenOn(true);
			} else {
				setKeepScreenOn(false);
			}
		}
	}

	private void initPowerConnectionReceiver() {
		if (powerConnectionReceiver == null) {
			BroadcastReceiver tempPowerConnectionReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					boolean tempCharging = isCharging(intent);
					Log.d(getClass().getName(), "PowerConnectionReceiver:"
							+ intent + " charging=" + tempCharging);
					setKeepScreenOn(tempCharging);
				}
			};
			IntentFilter ifilter = new IntentFilter(
					Intent.ACTION_POWER_CONNECTED);
			registerReceiver(tempPowerConnectionReceiver, ifilter);
			ifilter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
			registerReceiver(tempPowerConnectionReceiver, ifilter);
			powerConnectionReceiver = tempPowerConnectionReceiver;
		}
	}

	/**
	 * http://developer.android.com/training/monitoring-device-state/battery-monitoring.html
	 * 
	 * @return
	 */
	private boolean isBatteryCharging() {
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = registerReceiver(null, ifilter);
		return isCharging(batteryStatus);
	}

	private boolean isCharging(Intent batteryStatus) {
		if (Intent.ACTION_POWER_DISCONNECTED.equals(batteryStatus.getAction())) {
			return false;
		}
		if (Intent.ACTION_POWER_CONNECTED.equals(batteryStatus.getAction())) {
			return true;
		}
		int chargePlug = batteryStatus.getIntExtra(
				BatteryManager.EXTRA_PLUGGED, 0);
		boolean usbCharge = (chargePlug & BatteryManager.BATTERY_PLUGGED_USB) > 0;
		boolean acCharge = (chargePlug & BatteryManager.BATTERY_PLUGGED_AC) > 0;
		return usbCharge || acCharge;
	}

	/**
	 * android:keepScreenOn="true"
	 * 
	 * @param anAlwaysOnFlag
	 */
	private void setKeepScreenOn(boolean anAlwaysOnFlag) {
		Log.i(getClass().getName(), "setKeepScreenOn=" + anAlwaysOnFlag);
		Window tempWindow = getWindow();

		if (anAlwaysOnFlag && !keepOn) {
			tempWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			Toast.makeText(this, getResources().getString(R.string.PowerOn),
					Toast.LENGTH_LONG).show();
		} else if (!anAlwaysOnFlag && keepOn) {
			tempWindow
					.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			Toast.makeText(this, getResources().getString(R.string.PowerOff),
					Toast.LENGTH_LONG).show();
		}
		keepOn = anAlwaysOnFlag;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.i(getClass().getName(), "touch " + event);
		doFinish();
		return super.onTouchEvent(event);
	}

	private void doFinish() {
		runnable = null;
		if (powerConnectionReceiver != null) {
			unregisterReceiver(powerConnectionReceiver);
			powerConnectionReceiver = null;
		}
		Log.i(getClass().getName(), "doFinish:" + this);

	}

	@Override
	public void onBackPressed() {
		Log.i(getClass().getName(), "onBackPressed");
		super.onBackPressed();
		doFinish();
	}

	@Override
	protected void onPause() {
		Log.i(getClass().getName(), "onBackPressed");
		super.onPause();
		doFinish();
	}

	private void onCreateNightLight() {
		Log.i(getClass().getName(), "onCreateNightLight:" + this);
	}

	@Override
	protected void onStart() {
		Log.i(getClass().getName(), "onStart:" + this);
		initPowerConnectionReceiver();
		super.onStart();
		if (runnable == null) {
			runnable = new Runnable() {
				public void run() {
					Log.d(getClass().getName(), "timer");
					startTimer();
				}
			};
		}
		startTimer();
		if (!keepOn) {
			Toast.makeText(this, getResources().getString(R.string.PowerOff),
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onStop() {
		Log.i(getClass().getName(), "onStop:" + this);
		super.onStop();
		runnable = null;
	}

	int offsetX = 1;
	int countX = 0;
	private final int maxCount = 60;

	int countColor = 0;
	int offsetColor = 1;

	private void startTimer() {
		TextView tempTopText = (TextView) findViewById(R.id.textTop);
		TextView tempLeftText = (TextView) findViewById(R.id.textLeft);
		TextView tempRightText = (TextView) findViewById(R.id.textRight);
		BigTextButton tempEditText = (BigTextButton) findViewById(R.id.BigTextButton1);

		countX = countX + offsetX;
		if (countX <= 0) {
			offsetX = 1;
		} else if (countX >= maxCount) {
			offsetX = -1;
		}
		String tempLeft = "";
		String tempRight = "";
		for (int i = 0; i < countX; i++) {
			tempLeft += "i";
		}
		for (int i = maxCount; i > countX; i--) {
			tempRight += "i";
		}

		tempLeftText.setText(tempLeft);
		tempRightText.setText(tempRight);

		// FrameLayout.LayoutParams layoutParams =
		// (android.widget.FrameLayout.LayoutParams) tempEditText
		// .getLayoutParams();
		// layoutParams.setMargins(countX, 0, 0, 0);
		// tempEditText.setLayoutParams(layoutParams);

		String tempString;
		tempString = DateFormat.getTimeFormat(getActivity()).format(
				new java.util.Date(System.currentTimeMillis()));

		SharedPreferences tempPref = PreferenceManager
				.getDefaultSharedPreferences(this);
		String tempPrefString = tempPref.getString("pref_brightness", "50");
		int tempBrightness;
		try {
			tempBrightness = Integer.valueOf(tempPrefString);
		} catch (NumberFormatException e) {
			tempBrightness = 50;
		}
		int tempColourText = 255;
		tempColourText = (int) Math.round(tempColourText * tempBrightness
				/ 100.0);
		int tempIntColourText = 0xff000000 + tempColourText * 256 * 256
				+ tempColourText * 256 + tempColourText;
		tempEditText.setText(tempString);
		tempEditText.setColor(tempIntColourText);
		if (runnable != null) {
			final Handler handler = new Handler();
			handler.postDelayed(runnable, 1000);
		}

		countColor = countColor + offsetColor;
		if (countColor < -255) {
			countColor = -255;
		}
		if (countColor > 255) {
			countColor = 255;
		}
		if (countColor == -255) {
			offsetColor = 10;
		} else if (countColor == 255) {
			offsetColor = -10;
		}

		TextView tempBottom = (TextView) findViewById(R.id.textBottom);

		tempTopText.setText("-------");
		tempBottom.setText("-------");

		int tempColourTop;
		if (countColor > -100) {
			tempColourTop = Math.min(255, countColor + 100);
		} else {
			tempColourTop = 0;
		}
		tempColourTop = (int) Math
				.round(tempColourTop * tempBrightness / 100.0);
		int tempIntColTop = 0xff000000 + tempColourTop * 256 * 256
				+ tempColourTop * 256 + tempColourTop;
		tempTopText.setBackgroundColor(tempIntColTop);
		tempTopText.setTextColor(tempIntColTop);

		int tempColourBottom;
		if (countColor < 100) {
			tempColourBottom = Math.min(255, 100 - countColor);
		} else {
			tempColourBottom = 0;
		}
		tempColourBottom = (int) Math.round(tempColourBottom * tempBrightness
				/ 100.0);
		int tempIntColBot = 0xff000000 + tempColourBottom * 256 * 256
				+ tempColourBottom * 256 + tempColourBottom;
		tempBottom.setBackgroundColor(tempIntColBot);
		tempBottom.setTextColor(tempIntColBot);
		
		checkBattery();
	}

	private Context getActivity() {
		return this;
	}

	@Override
	public void onDetachedFromWindow() {
		doFinish();
		Log.i(getClass().getName(), "onDetachedFromWindow");
		super.onDetachedFromWindow();
	}

	protected void onDestroy() {
		runnable = null;
		Log.i(getClass().getName(), "onDestroy");
		super.onDestroy();
	}

	/**
	 * http://developer.android.com/guide/topics/ui/menus.html
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * http://developer.android.com/guide/topics/ui/menus.html
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_options:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
			//	        case R.id.help:
			//	            showHelp();
			//	            return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
