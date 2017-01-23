package de.quaddyservices.dynamicnightlight;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private long nextCheckBattery = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private Runnable runnable;

    @Override
    protected void onStart() {
        super.onStart();
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
    }

    private  int offsetX = 1;
    private  int countX = 0;
    private final int maxCount = 60;

    private int countColor = 0;
    private int offsetColor = 1;

    private final Handler handler = new Handler();

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
        tempString = DateFormat.getTimeFormat(this).format(
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(getClass().getName(), "onCreate...");

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        nextCheckBattery = 0;
        checkBattery();
        initPowerConnectionReceiver();

        return true;
    }

    private static final int SETTINGS_STARTED = 334;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.options) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent,SETTINGS_STARTED);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_STARTED) {
            nextCheckBattery = 0;
            checkBattery();
        }
    }

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

    private String lastPowerToast;

    /**
     * android:keepScreenOn="true"
     *
     * @param anAlwaysOnFlag
     */
    private void setKeepScreenOn(boolean anAlwaysOnFlag) {

        Log.i(getClass().getName(), "setKeepScreenOn=" + anAlwaysOnFlag);
        Window tempWindow = getWindow();

        String tempAlwaysOnInfo;
        if (anAlwaysOnFlag) {
            tempWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            tempAlwaysOnInfo = getResources().getString(R.string.PowerOn);
        } else {
            if (isKeepScreenOnBatteryToo()) {
                tempWindow
                        .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                tempAlwaysOnInfo = getResources().getString(R.string.KeepOnEvenOnBattery);
            } else {
                tempWindow
                        .clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                tempAlwaysOnInfo = getResources().getString(R.string.PowerOff);
            }
        }
        if (lastPowerToast == null || !lastPowerToast.equals(tempAlwaysOnInfo)) {
            lastPowerToast = tempAlwaysOnInfo;
            Log.i(getClass().getName(), "Toast:" + tempAlwaysOnInfo);
            Toast.makeText(this, tempAlwaysOnInfo, Toast.LENGTH_LONG).show();
        }

    }

    private boolean isKeepScreenOnBatteryToo() {
        SharedPreferences tempPref = PreferenceManager
                .getDefaultSharedPreferences(this);
        return tempPref.getBoolean("pref_ignoreBattery", Boolean.FALSE);
    }

    private BroadcastReceiver powerConnectionReceiver;

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

}
