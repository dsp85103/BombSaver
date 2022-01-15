package tw.kevinxu.bombsaver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Vibrator;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

public class BatteryReceiver extends BroadcastReceiver {

    private SharedPreferences preferences;
    private Vibrator sysVibrator;

    private void toastText(Context context, String s) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        sysVibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);

        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int batteryPct = Math.round(level * 100 / (float) scale);

        int highBatteryLevel = Integer.parseInt(preferences.getString("highBatteryLevel", "90"));
        int lowBatteryLevel = Integer.parseInt(preferences.getString("lowBatteryLevel", "20"));

        String notifyKindValue = preferences.getString("notifyKinds", "touchBothLevel");

        if ("touchBothLevel".equals(notifyKindValue)) {
            if (status == BatteryManager.BATTERY_STATUS_CHARGING && batteryPct >= highBatteryLevel) {
                toastText(context, String.format("充電達到上限 %s%% 囉", highBatteryLevel));
                doNotify(context, batteryPct, highBatteryLevel, lowBatteryLevel);
            }
            if ((status == BatteryManager.BATTERY_STATUS_NOT_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_DISCHARGING) && batteryPct <= lowBatteryLevel) {
                toastText(context, String.format("電量達到下限 %s%% 囉", lowBatteryLevel));
                doNotify(context, batteryPct, highBatteryLevel, lowBatteryLevel);
            }
        }

        if ("touchLowLevel".equals(notifyKindValue) &&
                (status == BatteryManager.BATTERY_STATUS_NOT_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_DISCHARGING) &&
                batteryPct <= lowBatteryLevel) {
            toastText(context, String.format("電量達到下限 %s%% 囉", lowBatteryLevel));
            doNotify(context, batteryPct, highBatteryLevel, lowBatteryLevel);
        }

        if ("touchHighLevel".equals(notifyKindValue) &&
                status == BatteryManager.BATTERY_STATUS_CHARGING &&
                batteryPct >= highBatteryLevel) {
            toastText(context, String.format("充電達到上限 %s%% 囉", highBatteryLevel));
            doNotify(context, batteryPct, highBatteryLevel, lowBatteryLevel);
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void doNotify(Context ctxt, int batteryLevel, int high, int low) {
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
            r.stop();
            r.play();
        }
        if (needVibrate) {
            sysVibrator.cancel();
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
}
