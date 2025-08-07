package com.example.medicine;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "medicine_history")
public class MedicineHistory {
    @PrimaryKey(autoGenerate = true)
    public int id;
    @NonNull
    public String userId;
    @NonNull
    public String medicineName;
    public String timeTaken;
    public boolean takenOnTime;
}
