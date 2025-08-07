package com.example.medicine;

import android.content.Context;
import android.media.RingtoneManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.annotation.Nullable;
import android.os.AsyncTask;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

// Room imports
import com.example.medicine.Medicine;
import com.example.medicine.MedicineDatabase;
import com.example.medicine.MedicineAdapter;
import com.example.mymedicinereminder.AlarmReceiver;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import android.content.Intent;

public class HomeActivity extends AppCompatActivity {
    private static final String KEY_LAST_USER = "last_logged_in_user";
    private SharedPrefManager prefManager;
    private String lastSelectedRingtoneUri = null;
    private TextView lastRingtoneNameView = null; // For dialog context
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Clear data if new user logs in
        String currentUser = getIntent().getStringExtra("username");
        android.content.SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String lastUser = prefs.getString(KEY_LAST_USER, "");
        if (currentUser != null && !currentUser.equals(lastUser)) {
            // Clear all medicines for new user
            MedicineDatabase db = MedicineDatabase.getInstance(getApplicationContext());
            new Thread(() -> db.medicineDao().deleteAll()).start();
            prefs.edit().putString(KEY_LAST_USER, currentUser).apply();
        }
        // If same user, do nothing (medicines remain)
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        prefManager = new SharedPrefManager(this);

        // Request notification permission for Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        // Show popup if alarm triggered
        if (getIntent().getBooleanExtra("show_popup", false)) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Medicine Reminder")
                .setMessage("It's time to take your medicine!")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        MedicineAdapter adapter = new MedicineAdapter(this);
        recyclerView.setAdapter(adapter);

        TextView tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        MedicineDatabase db = MedicineDatabase.getInstance(getApplicationContext());
        db.medicineDao().getAllMedicines().observe(this, new Observer<List<Medicine>>() {
            @Override
            public void onChanged(List<Medicine> medicines) {
                adapter.setMedicines(medicines);
                if (medicines == null || medicines.isEmpty()) {
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyMessage.setVisibility(View.GONE);
                }
                for (Medicine med : medicines) {
                    setAlarmForMedicine(med);
                }
            }
        });

        adapter.setOnMedicineActionListener(new MedicineAdapter.OnMedicineActionListener() {
            @Override
            public void onEdit(Medicine medicine) {
                showAddMedicineDialog(medicine);
            }
            @Override
            public void onDelete(Medicine medicine) {
                AsyncTask.execute(() -> {
                    db.medicineDao().delete(medicine);
                    cancelAlarmForMedicine(medicine);
                    runOnUiThread(() -> Toast.makeText(HomeActivity.this, "Medicine deleted", Toast.LENGTH_SHORT).show());
                });
            }
        });

        // History button
        findViewById(R.id.btnHistory).setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
        });

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddMedicineDialog();
            }
        });

        // Show email and username from Intent
        TextView tvProfileEmail = findViewById(R.id.tvProfileEmail);
        TextView tvProfileUsername = findViewById(R.id.tvProfileUsername);
        String email = getIntent().getStringExtra("email");
        String username = getIntent().getStringExtra("username");
        if (email != null && !email.isEmpty()) {
            tvProfileEmail.setText(email);
        }
        if (username != null && !username.isEmpty()) {
            tvProfileUsername.setText(username);
        } else {
            // fallback to SharedPrefManager
            
            String storedUsername = prefManager.getUsername();
            if (storedUsername != null && !storedUsername.isEmpty()) {
                tvProfileUsername.setText(storedUsername);
            }
        }

        // Logout button
        Button btnLogout = findViewById(R.id.btnChangeProfileImage);
        btnLogout.setText("Logout");
        btnLogout.setOnClickListener(v -> {
            // Clear user session
            if (prefManager != null) {
                prefManager.clear(); // Assuming clear() removes user data from SharedPreferences
            }
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Profile image picker
        ImageView imgProfile = findViewById(R.id.imgProfile);
        
        String profileUri = prefManager.getProfileImageUri();
        if (profileUri != null && !profileUri.isEmpty()) {
            imgProfile.setImageURI(android.net.Uri.parse(profileUri));
        }
        imgProfile.setOnClickListener(v -> {
            Intent pickIntent = new Intent(Intent.ACTION_PICK);
            pickIntent.setType("image/*");
            startActivityForResult(pickIntent, 2001);
        });
    }

    private void setAlarmForMedicine(Medicine medicine) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a");
            java.util.Date date = sdf.parse(medicine.time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            Calendar now = Calendar.getInstance();
            calendar.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            if (calendar.before(now)) {
                calendar.add(Calendar.DAY_OF_MONTH, 1); // Next day if time passed
            }
            long triggerTimeInMillis = calendar.getTimeInMillis();

            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.putExtra("medicine_name", medicine.name); // Use 'medicine_name' to match AlarmReceiver
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                medicine.id, // Unique request code for each medicine
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerTimeInMillis,
                pendingIntent
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAddMedicineDialog() {
        showAddMedicineDialog(null);
    }

    private void showAddMedicineDialog(@Nullable Medicine medicineToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_medicine, null);
        builder.setView(dialogView);
        builder.setTitle(medicineToEdit == null ? "Add Medicine" : "Edit Medicine");

        EditText etName = dialogView.findViewById(R.id.etName);
        EditText etDosage = dialogView.findViewById(R.id.etDosage);
        EditText etFrequency = dialogView.findViewById(R.id.etFrequency);
        EditText etTime = dialogView.findViewById(R.id.etTime);
        Button btnChangeRingtone = dialogView.findViewById(R.id.btnChangeRingtone);
        TextView tvRingtoneName = dialogView.findViewById(R.id.tvRingtoneName);
        lastRingtoneNameView = tvRingtoneName; // Save reference for onActivityResult

        // Ringtone selection logic
        final String[] ringtoneNames = {"Default", "Alarm", "Notification", "Choose from device"};
        final String[] ringtoneUris = {
            android.provider.Settings.System.DEFAULT_RINGTONE_URI.toString(),
            android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI.toString(),
            android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString(),
            "custom"
        };
        final String[] selectedRingtoneUri = {ringtoneUris[0]};
        final String[] selectedRingtoneName = {ringtoneNames[0]};

        if (medicineToEdit != null) {
            etName.setText(medicineToEdit.name);
            etDosage.setText(medicineToEdit.dosage);
            etFrequency.setText(medicineToEdit.frequency);
            etTime.setText(medicineToEdit.time);
            if (medicineToEdit.ringtoneUri != null && !medicineToEdit.ringtoneUri.isEmpty()) {
                selectedRingtoneUri[0] = medicineToEdit.ringtoneUri;
                // Try to resolve name
                if (medicineToEdit.ringtoneUri.equals(ringtoneUris[1])) selectedRingtoneName[0] = ringtoneNames[1];
                else if (medicineToEdit.ringtoneUri.equals(ringtoneUris[2])) selectedRingtoneName[0] = ringtoneNames[2];
                else if (medicineToEdit.ringtoneUri.equals(ringtoneUris[0])) selectedRingtoneName[0] = ringtoneNames[0];
                else selectedRingtoneName[0] = "Custom";
                tvRingtoneName.setText(selectedRingtoneName[0]);
            }
        }

        btnChangeRingtone.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(this)
                .setTitle("Select Ringtone")
                .setItems(ringtoneNames, (dialog, which) -> {
                    if (!ringtoneNames[which].equals("Choose from device")) {
                        selectedRingtoneUri[0] = ringtoneUris[which];
                        selectedRingtoneName[0] = ringtoneNames[which];
                        tvRingtoneName.setText(selectedRingtoneName[0]);
                        lastSelectedRingtoneUri = selectedRingtoneUri[0];
                    } else {
                        // Open ringtone picker for user to select a custom sound
                        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone");
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (lastSelectedRingtoneUri != null) ? android.net.Uri.parse(lastSelectedRingtoneUri) : null);
                        startActivityForResult(intent, 101);
                    }
                })
                .show();
        });

        // Show selected custom ringtone from picker
        if (medicineToEdit == null) {
            tvRingtoneName.setText(selectedRingtoneName[0]);
        }

        // Handle result from ringtone/file picker
        // Implementation below


        etTime.setOnClickListener(v -> {
            final Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);
            boolean isEdit = etTime.getText().toString().contains("AM") || etTime.getText().toString().contains("PM");
            if (isEdit) {
                try {
                    java.text.DateFormat df = new java.text.SimpleDateFormat("hh:mm a");
                    java.util.Date d = df.parse(etTime.getText().toString());
                    Calendar c = Calendar.getInstance();
                    c.setTime(d);
                    hour = c.get(Calendar.HOUR_OF_DAY);
                    minute = c.get(Calendar.MINUTE);
                } catch (Exception ignored) {}
            }
            TimePickerDialog timePicker;
            timePicker = new TimePickerDialog(dialogView.getContext(), (view, hourOfDay, minute1) -> {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(Calendar.MINUTE, minute1);
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a");
                etTime.setText(sdf.format(cal.getTime()));
            }, hour, minute, false);
            timePicker.show();
        });

        builder.setPositiveButton(medicineToEdit == null ? "Add" : "Update", (dialog, which) -> {
            String name = etName.getText().toString().trim();
            String dosage = etDosage.getText().toString().trim();
            String frequency = etFrequency.getText().toString().trim();
            String time = etTime.getText().toString().trim();

            if (name.isEmpty() || dosage.isEmpty() || frequency.isEmpty() || time.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                if (medicineToEdit == null) {
                    // ADD NEW
                    Medicine medicine = new Medicine();
                    medicine.name = name;
                    medicine.dosage = dosage;
                    medicine.frequency = frequency;
                    medicine.time = time;
                    medicine.ringtoneUri = selectedRingtoneUri[0];
                    insertMedicine(medicine);
                } else {
                    // UPDATE EXISTING
                    medicineToEdit.name = name;
                    medicineToEdit.dosage = dosage;
                    medicineToEdit.frequency = frequency;
                    medicineToEdit.time = time;
                    medicineToEdit.ringtoneUri = selectedRingtoneUri[0];
                    updateMedicine(medicineToEdit);
                }
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void insertMedicine(Medicine medicine) {
        AsyncTask.execute(() -> {
            MedicineDatabase db = MedicineDatabase.getInstance(getApplicationContext());
            db.medicineDao().insert(medicine);
            runOnUiThread(() -> Toast.makeText(this, "Medicine added", Toast.LENGTH_SHORT).show());
        });
    }

    private void updateMedicine(Medicine medicine) {
        AsyncTask.execute(() -> {
            MedicineDatabase db = MedicineDatabase.getInstance(getApplicationContext());
            db.medicineDao().update(medicine);
            setAlarmForMedicine(medicine);
            runOnUiThread(() -> Toast.makeText(this, "Medicine updated", Toast.LENGTH_SHORT).show());
        });
    }

    private void cancelAlarmForMedicine(Medicine medicine) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, medicine.id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 101) { // Ringtone picker
                android.net.Uri uri = data.getParcelableExtra(android.media.RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                if (uri != null && lastRingtoneNameView != null) {
                    lastSelectedRingtoneUri = uri.toString();
                    lastRingtoneNameView.setText("Custom");
                }
            } else if (requestCode == 102) { // File picker for custom audio
                android.net.Uri uri = data.getData();
                if (uri != null && lastRingtoneNameView != null) {
                    lastSelectedRingtoneUri = uri.toString();
                    lastRingtoneNameView.setText("Custom");
                }
            } else if (requestCode == 2001) { // Profile image picker
                android.net.Uri uri = data.getData();
                if (uri != null) {
                    prefManager.saveProfileImageUri(uri.toString());
                    ImageView imgProfile = findViewById(R.id.imgProfile);
                    imgProfile.setImageURI(uri);
                }
            }
        }
    }
}
