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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        Intent batteryIntent = this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));


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
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPct = Math.round(level * 100 / (float) scale);
            tvBatteryLevel.setText(String.valueOf(batteryPct));

            int highBatteryLevel = Integer.parseInt(preferences.getString("highBatteryLevel", "90"));
            int lowBatteryLevel = Integer.parseInt(preferences.getString("lowBatteryLevel", "20"));

            String notifyKindValue = preferences.getString("notifyKinds", "touchBothLevel");

            if ("touchBothLevel".equals(notifyKindValue)) {
                if ((status == BatteryManager.BATTERY_STATUS_CHARGING && batteryPct >= highBatteryLevel) ||
                        ((status == BatteryManager.BATTERY_STATUS_NOT_CHARGING || status == BatteryManager.BATTERY_STATUS_DISCHARGING) && batteryPct <= lowBatteryLevel)) {
                    doNotify(ctxt);
                }
            }

            if ("touchLowLevel".equals(notifyKindValue) &&
                    (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING || status == BatteryManager.BATTERY_STATUS_DISCHARGING) &&
                    batteryPct <= lowBatteryLevel) {
                doNotify(ctxt);
            }

            if ("touchHighLevel".equals(notifyKindValue) &&
                    status == BatteryManager.BATTERY_STATUS_CHARGING &&
                    batteryPct >= highBatteryLevel) {
                doNotify(ctxt);
            }
        }

        private void doNotify(Context ctxt) {
            boolean needRingtone = preferences.getBoolean("needRingtone", false);
            boolean needVibrate = preferences.getBoolean("needVibrate", true);

            long vibrateMs = Long.parseLong(preferences.getString("vibrateMs", "1100"));
            long vibrateStopMs = Long.parseLong(preferences.getString("vibrateStopMs", "700"));
            long vibrateCount = Long.parseLong(preferences.getString("vibrateCount", "3"));

            List<Long> vibrateSequence = new ArrayList<>();
            for (int i = 0; i < vibrateCount; i++) {
                vibrateSequence.add(vibrateStopMs);
                vibrateSequence.add(vibrateMs);
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
                sysVibrator.vibrate(convertList(vibrateSequence), -1);
            }
        }

        private long[] convertList(List<Long> list) {
            long[] result = new long[list.size()];
            for (int i = 0; i < list.size(); i++) {
                result[i] = list.get(i);
            }
            return result;
        }
    };

    public void btSettingsClicked(View v) {
        Intent i = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(i);
    }

}