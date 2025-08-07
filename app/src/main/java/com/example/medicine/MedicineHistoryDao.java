package com.example.medicine;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface MedicineHistoryDao {
    @Insert
    void insert(MedicineHistory history);

    @Query("SELECT * FROM medicine_history WHERE userId = :userId ORDER BY id DESC")
    LiveData<List<MedicineHistory>> getHistoryForUser(String userId);

    @Query("SELECT * FROM medicine_history ORDER BY id DESC")
    LiveData<List<MedicineHistory>> getAllHistory();

    @Query("DELETE FROM medicine_history")
    void deleteAll();
}
