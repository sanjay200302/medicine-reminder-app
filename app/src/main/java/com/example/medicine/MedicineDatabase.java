package com.example.medicine;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Medicine.class, MedicineHistory.class}, version = 4)
public abstract class MedicineDatabase extends RoomDatabase {
    public abstract MedicineDao medicineDao();
public abstract MedicineHistoryDao medicineHistoryDao();
    private static MedicineDatabase instance;
    public static synchronized MedicineDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    MedicineDatabase.class, "medicine_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
