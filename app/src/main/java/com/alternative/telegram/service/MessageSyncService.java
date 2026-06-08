/*
 * ᴍᴇꜱꜱᴀɢᴇꜱʏɴᴄꜱᴇʀᴠɪᴄᴇ.ᴊᴀᴠᴀ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
 * ʙᴀᴄᴋɢʀᴏᴜɴᴅ ꜱᴇʀᴠɪᴄᴇ ꜰᴏʀ ᴍᴇꜱꜱᴀɢᴇ ꜱʏɴᴄʜʀᴏɴɪᴢᴀᴛɪᴏɴ
 * ᴍᴀɪɴᴛᴀɪɴꜱ ᴍᴛᴘʀᴏᴛᴏ ᴄᴏɴɴᴇᴄᴛɪᴏɴ ɪɴ ʙᴀᴄᴋɢʀᴏᴜɴᴅ
 */

package com.alternative.telegram.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.alternative.telegram.MainActivity;
import com.alternative.telegram.R;

public class MessageSyncService extends Service {

    private static final String TAG = "MessageSyncService";
    private static final String CHANNEL_ID = "message_sync_channel";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Message sync service started");

        Notification notification = buildForegroundNotification();
        startForeground(NOTIFICATION_ID, notification);

        // ɪɴɪᴛɪᴀʟɪᴢᴇ ᴍᴛᴘʀᴏᴛᴏ ᴄᴏɴɴᴇᴄᴛɪᴏɴ ʜᴇʀᴇ
        // ᴛʜɪꜱ ɪꜱ ᴀ ꜱᴛᴜʙ — ꜰᴜʟʟ ɪᴍᴘʟᴇᴍᴇɴᴛᴀᴛɪᴏɴ ʀᴇǫᴜɪʀᴇꜱ ᴍᴛᴘʀᴏᴛᴏ ʟɪʙʀᴀʀʏ

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Message Sync",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Background message synchronization");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildForegroundNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MyTelegram")
                .setContentText("Syncing messages...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Message sync service stopped");
    }
}
