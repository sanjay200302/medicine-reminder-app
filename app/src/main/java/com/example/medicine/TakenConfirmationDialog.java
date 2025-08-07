package com.example.medicine;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class TakenConfirmationDialog extends DialogFragment {
    public interface TakenListener {
        void onTakenConfirmed(boolean takenOnTime);
    }
    private String medicineName;
    private TakenListener listener;
    public TakenConfirmationDialog(String medicineName, TakenListener listener) {
        this.medicineName = medicineName;
        this.listener = listener;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_taken_confirmation, null);
        builder.setView(view);
        TextView tvPrompt = view.findViewById(R.id.tvTakenPrompt);
        tvPrompt.setText("Did you take your medicine '" + medicineName + "'?");
        ImageView imgCircleStatus = view.findViewById(R.id.imgCircleStatus);
        Button btnYes = view.findViewById(R.id.btnYes);
        Button btnNo = view.findViewById(R.id.btnNo);
        btnYes.setOnClickListener(v -> {
            imgCircleStatus.setImageResource(R.drawable.ic_circle_right);
            btnYes.setEnabled(false);
            btnNo.setEnabled(false);
            new Handler().postDelayed(() -> {
                if (listener != null) listener.onTakenConfirmed(true);
                dismiss();
            }, 800);
        });
        btnNo.setOnClickListener(v -> {
            imgCircleStatus.setImageResource(R.drawable.ic_circle_wrong);
            btnYes.setEnabled(false);
            btnNo.setEnabled(false);
            new Handler().postDelayed(() -> {
                if (listener != null) listener.onTakenConfirmed(false);
                dismiss();
            }, 800);
        });
        return builder.create();
    }
}
