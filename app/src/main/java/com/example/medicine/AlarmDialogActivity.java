package com.example.medicine;

import com.example.mymedicinereminder.AlarmReceiver;
import android.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager;

public class AlarmDialogActivity extends Activity {
    private android.media.Ringtone alarmRingtone;
    private String medName;
    private String ringtoneUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String medicineName = getIntent().getStringExtra("medicine_name");

        // Play alarm sound (always, for alarm use-case)
        try {
            android.media.AudioManager audioManager = (android.media.AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioManager != null) {
                int maxVolume = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM);
                audioManager.setStreamVolume(android.media.AudioManager.STREAM_ALARM, maxVolume, 0);
            }
            android.net.Uri alarmUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM);
            if (alarmUri == null) {
                alarmUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION);
            }
            alarmRingtone = android.media.RingtoneManager.getRingtone(this, alarmUri);
            if (alarmRingtone != null && !alarmRingtone.isPlaying()) {
                alarmRingtone.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Show over lock screen
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        // Show alert dialog
        new android.app.AlertDialog.Builder(this)
            .setTitle("Medicine Reminder")
            .setMessage("Time to take: " + (medicineName != null ? medicineName : "your medicine"))
            .setCancelable(false)
            .setPositiveButton("Dismiss", (dialog, which) -> {
                stopAlarmSound();
                finish();
            })
            .show();
    }

    private void stopAlarmSound() {
        if (alarmRingtone != null && alarmRingtone.isPlaying()) {
            alarmRingtone.stop();
        }
    }

    private void snoozeAlarm(int minutes) {
        // Reschedule the alarm for X minutes later
        android.app.AlarmManager alarmManager = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
        android.content.Intent intent = new android.content.Intent(this, AlarmReceiver.class);
        intent.putExtra("med_name", medName);
        intent.putExtra("ringtone_uri", ringtoneUri);
        android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(this, medName.hashCode(), intent, android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);
        long triggerAtMillis = System.currentTimeMillis() + minutes * 60 * 1000;
        alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }

    private void showTakenConfirmation(String medName) {
        // Use standard AlertDialog for Activity context
        View view = getLayoutInflater().inflate(R.layout.dialog_taken_confirmation, null);
        TextView tvPrompt = view.findViewById(R.id.tvTakenPrompt);
        tvPrompt.setText("Did you take your medicine '" + medName + "'?");
        ImageView imgCircleStatus = view.findViewById(R.id.imgCircleStatus);
        Button btnYes = view.findViewById(R.id.btnYes);
        Button btnNo = view.findViewById(R.id.btnNo);
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create();
        btnYes.setOnClickListener(v -> {
            imgCircleStatus.setImageResource(R.drawable.ic_circle_right);
            btnYes.setEnabled(false);
            btnNo.setEnabled(false);
            new android.os.Handler().postDelayed(() -> {
                saveHistory(medName, true);
                dialog.dismiss();
            }, 800);
        });
        btnNo.setOnClickListener(v -> {
            imgCircleStatus.setImageResource(R.drawable.ic_circle_wrong);
            btnYes.setEnabled(false);
            btnNo.setEnabled(false);
            new android.os.Handler().postDelayed(() -> {
                saveHistory(medName, false);
                dialog.dismiss();
            }, 800);
        });
        dialog.show();
    }

    private void saveHistory(String medName, boolean takenOnTime) {
        MedicineHistory history = new MedicineHistory();
        String userId = getIntent().getStringExtra("userId");
        if (userId == null) userId = "unknown";
        history.userId = userId;
        history.medicineName = medName;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        history.timeTaken = sdf.format(new java.util.Date());
        history.takenOnTime = takenOnTime;
        new Thread(() -> {
            MedicineDatabase db = MedicineDatabase.getInstance(getApplicationContext());
            db.medicineHistoryDao().insert(history);
        }).start();
    }
}
