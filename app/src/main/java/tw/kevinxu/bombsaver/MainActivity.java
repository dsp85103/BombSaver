package tw.kevinxu.bombsaver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // components
    private Vibrator sysVibrator;

    // views
    private Button btSettings;
    private TextView tvBatteryLevel;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configAllNeedView();
        configAllNeedComponent();

        // register battery level receiver
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

    }

    private void toastText(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    private void configAllNeedView() {
        tvBatteryLevel = this.findViewById(R.id.tvBatteryLevel);
    }

    private void configAllNeedComponent() {
        sysVibrator = (Vibrator) getApplication().getSystemService(Service.VIBRATOR_SERVICE);
    }

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context ctxt, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPct = Math.round(level * 100 / (float) scale);
            tvBatteryLevel.setText(String.valueOf(batteryPct));

            int highBatteryLevel = Integer.parseInt(preferences.getString("highBatteryLevel", "90"));
            int lowBatteryLevel = Integer.parseInt(preferences.getString("lowBatteryLevel", "20"));

            String notifyKindValue = preferences.getString("notifyKinds", "touchBothLevel");
            toastText(notifyKindValue);
            if ("touchBothLevel".equals(notifyKindValue)) {
                if (batteryPct >= highBatteryLevel || batteryPct <= lowBatteryLevel) {
                    doNotify(ctxt);
                }
            }

            if ("touchLowLevel".equals(notifyKindValue) && batteryPct <= lowBatteryLevel) {
                doNotify(ctxt);
            }

            if ("touchHighLevel".equals(notifyKindValue) && batteryPct >= highBatteryLevel) {
                doNotify(ctxt);
            }
        }

        private void doNotify(Context ctxt) {
            boolean needRingtone = preferences.getBoolean("needRingtone", false);
            boolean needVibrate = preferences.getBoolean("needVibrate", true);

            long[] vibrateTime =  new long[6];
            for (int i = 0; i < 5; i = i+2) {
                vibrateTime[i] = 700;
                vibrateTime[i+1] = 1100;
            }
            if (needRingtone) {
                Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if (alarmUri == null) {
                    alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                }
                Ringtone r = RingtoneManager.getRingtone(ctxt, alarmUri);
                r.play();
            }
            if (needVibrate) {
                sysVibrator.vibrate(vibrateTime, -1);
            }
        }
    };

    public void btSettingsClicked(View v) {
        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(i);
    }

}