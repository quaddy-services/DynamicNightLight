package de.quaddyservices.dynamicnightlight;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;
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
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

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
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.i(getClass().getName(), "touch " + event);
		doFinish();
		return super.onTouchEvent(event);
	}

	private void doFinish() {
		runnable = null;
		finish();
		Log.i(getClass().getName(), "doFinish:"+this);

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

		runnable = new Runnable() {
			public void run() {
				Log.d(getClass().getName(), "timer");
				startTimer();
			}
		};
		startTimer();
		Log.i(getClass().getName(), "onCreateNightLight:"+this);

	}

	int offsetX = 2;
	int countX = 0;

	int countColor = 0;
	int offsetColor = 10;

	private void startTimer() {
		TextView tempEditText = (TextView) findViewById(R.id.textView1);
		final View contentView = findViewById(R.id.fullscreen_content);
		int height = contentView.getHeight();
		Log.d(getClass().getName(), "height=" + height);
		int tempTextHeight = tempEditText.getHeight();
		Log.d(getClass().getName(), "tempEditText=" + tempTextHeight);
		tempEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, height / 2);
		Log.d(getClass().getName(), "tempEditText=" + tempTextHeight);
		FrameLayout.LayoutParams layoutParams = (android.widget.FrameLayout.LayoutParams) tempEditText
				.getLayoutParams();
		layoutParams.setMargins(countX, 0, 0, 0);
		tempEditText.setLayoutParams(layoutParams);

		String tempString;
		countX = countX + offsetX;
		if (countX == 0) {
			offsetX = 2;
		} else if (countX == 80) {
			offsetX = -2;
		}
		tempString = DateFormat.getTimeFormat(getActivity()).format(
				new java.util.Date(System.currentTimeMillis()));
		tempEditText.setText(tempString);
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

		TextView tempTop = (TextView) findViewById(R.id.textTop);
		TextView tempBottom = (TextView) findViewById(R.id.textBottom);

		tempTop.setText("-------");
		tempBottom.setText("-------");

		int tempColourTop;
		if (countColor > -100) {
			tempColourTop = Math.min(255, countColor + 100);
		} else {
			tempColourTop = 0;
		}
		int tempIntColTop = 0xff000000 + tempColourTop * 256 * 256
				+ tempColourTop * 256 + tempColourTop;
		tempTop.setBackgroundColor(tempIntColTop);
		tempTop.setTextColor(tempIntColTop);

		int tempColourBottom;
		if (countColor < 100) {
			tempColourBottom = Math.min(255, 100 - countColor);
		} else {
			tempColourBottom = 0;
		}
		int tempIntColBot = 0xff000000 + tempColourBottom * 256 * 256
				+ tempColourBottom * 256 + tempColourBottom;
		tempBottom.setBackgroundColor(tempIntColBot);
		tempBottom.setTextColor(tempIntColBot);
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
	};

}
