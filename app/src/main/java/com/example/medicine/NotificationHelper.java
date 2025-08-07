package com.example.medicine;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {
    public static final String CHANNEL_ID = "medicine_reminder_channel";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            );
            // Enable vibration
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 500, 500});
            // Enable sound
            channel.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public static void showNotification(Context context, String title, String message, String medName, String ringtoneUri, String medicineTime) {
        // Full-screen intent for guaranteed visibility
        android.content.Intent fullScreenIntent = new android.content.Intent(context, AlarmDialogActivity.class);
        fullScreenIntent.putExtra("med_name", medName);
        fullScreenIntent.putExtra("ringtone_uri", ringtoneUri);
        fullScreenIntent.putExtra("medicine_time", medicineTime);
        fullScreenIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
        android.app.PendingIntent fullScreenPendingIntent = android.app.PendingIntent.getActivity(
                context, 0, fullScreenIntent, android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setAutoCancel(true)
            .setVibrate(new long[]{0, 500, 500, 500})
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setFullScreenIntent(fullScreenPendingIntent, true);
        NotificationManagerCompat.from(context).notify((int) System.currentTimeMillis(), builder.build());
    }
}
