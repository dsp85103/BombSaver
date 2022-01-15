package tw.kevinxu.bombsaver;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {

    // views
    private Button btSettings;
    private TextView tvBatteryLevel;

    private SharedPreferences preferences;
    private Intent mServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configAllNeedView();
        configAllNeedComponent();

        // register battery level receiver
        this.registerReceiver(this.batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // enable service
        mServiceIntent = new Intent(this, BackgroundService.class);
        if (!isMyServiceRunning(BackgroundService.class)) {
            startService(mServiceIntent);
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        Log.i("main", "onDestroy!");
        super.onDestroy();
    }

    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPct = Math.round(level * 100 / (float) scale);
            tvBatteryLevel.setText(String.valueOf(batteryPct));
        }
    };

    private void toastText(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    private void configAllNeedView() {
        tvBatteryLevel = this.findViewById(R.id.tvBatteryLevel);
    }

    private void configAllNeedComponent() {
    }

    public void btSettingsClicked(View v) {
        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(i);
    }
}