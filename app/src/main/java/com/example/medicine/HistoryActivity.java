package com.example.medicine;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        HistoryAdapter adapter = new HistoryAdapter();
        recyclerView.setAdapter(adapter);

        MedicineDatabase db = MedicineDatabase.getInstance(getApplicationContext());
        String userId = getIntent().getStringExtra("userId");
        if (userId == null) userId = "unknown";
        db.medicineHistoryDao().getHistoryForUser(userId).observe(this, historyList -> {
            if (historyList != null && !historyList.isEmpty()) {
                adapter.setHistory(historyList);
            } else {
                Toast.makeText(this, "No history found", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
