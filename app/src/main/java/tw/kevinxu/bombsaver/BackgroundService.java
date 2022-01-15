package tw.kevinxu.bombsaver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class BackgroundService extends Service {

    public final static int NOTIFICATION_ID = 101;
    public final static String CHANNEL_ONE_ID = "tw.kevinxu.bombsaver.notify";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        BatteryReceiver batteryReceiver = new BatteryReceiver();
        this.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        String CHANNEL_ONE_NAME = "Bomb Saver";
        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
        }

        Notification n  = new Notification.Builder(this)
                .setChannelId(CHANNEL_ONE_ID)
                .setContentText("正在拯救你的電池")
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setOngoing(true)
                .build();
        startForeground(NOTIFICATION_ID, n);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent(this, AutoRunReceiver.class);
        sendBroadcast(broadcastIntent);
    }

    private void toastText(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }
}
