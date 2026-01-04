package com.example.testapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.R;
import com.example.testapp.models.Training;

import java.util.ArrayList;
import java.util.List;

public class TrainingAdapter extends RecyclerView.Adapter<TrainingAdapter.TrainingViewHolder> {

    private List<Training> trainings = new ArrayList<>();
    private final OnTrainingClickListener listener;
    private final OnTrainingEditListener editListener;
    private final OnTrainingDeleteListener deleteListener;
    private final OnTrainingDuplicateListener duplicateListener;

    public interface OnTrainingClickListener {
        void onTrainingClick(Training training);
    }

    public interface OnTrainingEditListener {
        void onTrainingEdit(Training training);
    }

    public interface OnTrainingDeleteListener {
        void onTrainingDelete(Training training);
    }

    public interface OnTrainingDuplicateListener {
        void onTrainingDuplicate(Training training);
    }

    public TrainingAdapter(OnTrainingClickListener listener, OnTrainingEditListener editListener, 
                          OnTrainingDeleteListener deleteListener, OnTrainingDuplicateListener duplicateListener) {
        this.listener = listener;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
        this.duplicateListener = duplicateListener;
    }

    public void setTrainings(List<Training> trainings) {
        this.trainings = trainings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrainingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_training, parent, false);
        return new TrainingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainingViewHolder holder, int position) {
        Training training = trainings.get(position);
        holder.bind(training, listener, editListener, deleteListener, duplicateListener);
    }

    @Override
    public int getItemCount() {
        return trainings.size();
    }

    static class TrainingViewHolder extends RecyclerView.ViewHolder {
        TextView teamName, courtName, dayOfWeek, trainingDate, timeRange, duration;
        CardView cardView;
        View colorIndicator;
        com.google.android.material.button.MaterialButton btnEdit, btnDelete, btnDuplicate;

        TrainingViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            teamName = itemView.findViewById(R.id.teamName);
            courtName = itemView.findViewById(R.id.courtName);
            dayOfWeek = itemView.findViewById(R.id.dayOfWeek);
            trainingDate = itemView.findViewById(R.id.trainingDate);
            timeRange = itemView.findViewById(R.id.timeRange);
            duration = itemView.findViewById(R.id.duration);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnDuplicate = itemView.findViewById(R.id.btnDuplicate);
        }

        void bind(Training training, OnTrainingClickListener listener, OnTrainingEditListener editListener, 
                  OnTrainingDeleteListener deleteListener, OnTrainingDuplicateListener duplicateListener) {
            teamName.setText(training.getTeamName());
            courtName.setText("מגרש: " + training.getCourtName());
            dayOfWeek.setText(training.getDayOfWeek());
            
            // Format and display date using UTC timezone
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            String formattedDate = dateFormat.format(new java.util.Date(training.getDate()));
            trainingDate.setText(formattedDate);
            
            timeRange.setText(training.getStartTime() + " - " + training.getEndTime());
            duration.setText(training.getDurationInMinutes() + " דקות");
            
            try {
                colorIndicator.setBackgroundColor(Color.parseColor(training.getTeamColor()));
            } catch (Exception e) {
                colorIndicator.setBackgroundColor(Color.parseColor("#3DDC84"));
            }

            itemView.setOnClickListener(v -> listener.onTrainingClick(training));

            if (editListener != null) {
                btnEdit.setVisibility(View.VISIBLE);
                btnEdit.setOnClickListener(v -> editListener.onTrainingEdit(training));
            } else {
                btnEdit.setVisibility(View.GONE);
            }

            if (deleteListener != null) {
                btnDelete.setVisibility(View.VISIBLE);
                btnDelete.setOnClickListener(v -> deleteListener.onTrainingDelete(training));
            } else {
                btnDelete.setVisibility(View.GONE);
            }

            if (duplicateListener != null) {
                btnDuplicate.setVisibility(View.VISIBLE);
                btnDuplicate.setOnClickListener(v -> {
                    duplicateListener.onTrainingDuplicate(training);
                });
            } else {
                btnDuplicate.setVisibility(View.GONE);
            }
        }
    }
}
