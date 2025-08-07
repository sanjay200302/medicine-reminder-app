package com.example.medicine;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "medicines")
public class Medicine {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
    public String dosage;
    public String frequency;
    public String time; // Store as "HH:mm"
    public String ringtoneUri; // Store the ringtone URI
}
