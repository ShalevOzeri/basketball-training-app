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

    public interface OnTrainingClickListener {
        void onTrainingClick(Training training);
    }

    public TrainingAdapter(OnTrainingClickListener listener) {
        this.listener = listener;
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
        holder.bind(training, listener);
    }

    @Override
    public int getItemCount() {
        return trainings.size();
    }

    static class TrainingViewHolder extends RecyclerView.ViewHolder {
        TextView teamName, courtName, dayOfWeek, timeRange, duration;
        CardView cardView;
        View colorIndicator;

        TrainingViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            teamName = itemView.findViewById(R.id.teamName);
            courtName = itemView.findViewById(R.id.courtName);
            dayOfWeek = itemView.findViewById(R.id.dayOfWeek);
            timeRange = itemView.findViewById(R.id.timeRange);
            duration = itemView.findViewById(R.id.duration);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
        }

        void bind(Training training, OnTrainingClickListener listener) {
            teamName.setText(training.getTeamName());
            courtName.setText("מגרש: " + training.getCourtName());
            dayOfWeek.setText(training.getDayOfWeek());
            timeRange.setText(training.getStartTime() + " - " + training.getEndTime());
            duration.setText(training.getDurationInMinutes() + " דקות");
            
            try {
                colorIndicator.setBackgroundColor(Color.parseColor(training.getTeamColor()));
            } catch (Exception e) {
                colorIndicator.setBackgroundColor(Color.parseColor("#3DDC84"));
            }

            itemView.setOnClickListener(v -> listener.onTrainingClick(training));
        }
    }
}
