package de.quaddyservices.dynamicnightlight;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

}
