package com.example.medicine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<MedicineHistory> history = new ArrayList<>();

    public void setHistory(List<MedicineHistory> history) {
        this.history = history;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        MedicineHistory record = history.get(position);
        holder.tvName.setText(record.medicineName);
        holder.tvTime.setText(record.timeTaken);
        if (holder.imgCircleStatus != null) {
            if (record.takenOnTime) {
                holder.imgCircleStatus.setImageResource(R.drawable.ic_circle_right);
            } else {
                holder.imgCircleStatus.setImageResource(R.drawable.ic_circle_wrong);
            }
        }
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime;
        android.widget.ImageView imgCircleStatus;
        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvHistoryName);
            tvTime = itemView.findViewById(R.id.tvHistoryTime);
            imgCircleStatus = itemView.findViewById(R.id.imgCircleStatus);
        }
    }
}
