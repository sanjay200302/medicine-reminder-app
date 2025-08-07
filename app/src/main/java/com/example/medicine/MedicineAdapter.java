package com.example.medicine;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {
    private List<Medicine> medicines = new ArrayList<>();
    private Context context;
    private OnMedicineActionListener actionListener;

    public interface OnMedicineActionListener {
        void onEdit(Medicine medicine);
        void onDelete(Medicine medicine);
    }

    public MedicineAdapter(Context context) {
        this.context = context;
    }

    public void setOnMedicineActionListener(OnMedicineActionListener listener) {
        this.actionListener = listener;
    }

    public void setMedicines(List<Medicine> medicines) {
        this.medicines = medicines;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine medicine = medicines.get(position);
        holder.tvName.setText(medicine.name);
        holder.tvDosage.setText("Dosage: " + medicine.dosage);
        holder.tvFrequency.setText("Frequency: " + medicine.frequency);
        holder.tvTime.setText("Time: " + medicine.time);
        holder.btnEdit.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onEdit(medicine);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onDelete(medicine);
        });
    }

    @Override
    public int getItemCount() {
        return medicines.size();
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDosage, tvFrequency, tvTime;
        android.widget.Button btnEdit, btnDelete;
        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvFrequency = itemView.findViewById(R.id.tvFrequency);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
