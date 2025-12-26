package com.example.testapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.R;
import com.example.testapp.models.Court;
import com.example.testapp.models.DaySchedule;

import java.util.ArrayList;
import java.util.List;

public class CourtAdapter extends RecyclerView.Adapter<CourtAdapter.CourtViewHolder> {

    private List<Court> courts = new ArrayList<>();
    private final OnCourtClickListener listener;

    public interface OnCourtClickListener {
        void onCourtClick(Court court);
    }

    public CourtAdapter(OnCourtClickListener listener) {
        this.listener = listener;
    }

    public void setCourts(List<Court> courts) {
        this.courts = courts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CourtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_court, parent, false);
        return new CourtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourtViewHolder holder, int position) {
        Court court = courts.get(position);
        holder.bind(court, listener);
    }

    @Override
    public int getItemCount() {
        return courts.size();
    }

    public static class CourtViewHolder extends RecyclerView.ViewHolder {
        private final TextView courtName;
        private final TextView courtLocation;
        private final TextView courtHours;
        private final TextView courtDays;
        private final TextView courtStatus;
        private final CardView cardView;

        public CourtViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            courtName = itemView.findViewById(R.id.courtName);
            courtLocation = itemView.findViewById(R.id.courtLocation);
            courtHours = itemView.findViewById(R.id.courtHours);
            courtDays = itemView.findViewById(R.id.courtDays);
            courtStatus = itemView.findViewById(R.id.courtStatus);
        }

        void bind(Court court, OnCourtClickListener listener) {
            courtName.setText(court.getName());
            courtLocation.setText(court.getLocation());
            
            // Show weekly schedule
            String scheduleText = formatWeeklySchedule(court);
            courtHours.setText(scheduleText);
            
            // Format active days
            String activeDaysText = formatActiveDaysFromSchedule(court);
            courtDays.setText(itemView.getContext().getString(R.string.court_active_days, activeDaysText));
            
            // Use string resource for status
            courtStatus.setText(court.isAvailable() ? 
                itemView.getContext().getString(R.string.court_available) : 
                itemView.getContext().getString(R.string.court_unavailable));
            courtStatus.setTextColor(court.isAvailable() ? 
                android.graphics.Color.parseColor("#4CAF50") : 
                android.graphics.Color.parseColor("#F44336"));

            itemView.setOnClickListener(v -> listener.onCourtClick(court));
        }
        
        private String formatWeeklySchedule(Court court) {
            // Get today's day of week (1=Sunday, 7=Saturday)
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int today = calendar.get(java.util.Calendar.DAY_OF_WEEK); // 1=Sunday in Calendar
            
            DaySchedule todaySchedule = court.getScheduleForDay(today);
            if (todaySchedule != null && todaySchedule.isActive()) {
                return "היום: " + todaySchedule.getOpeningHour() + " - " + todaySchedule.getClosingHour();
            } else if (todaySchedule != null && !todaySchedule.isActive()) {
                return "היום: סגור";
            }
            
            // Fallback to legacy format
            return itemView.getContext().getString(R.string.court_hours_format, 
                court.getOpeningHour(), court.getClosingHour());
        }
        
        private String formatActiveDaysFromSchedule(Court court) {
            String[] dayNames = {"א'", "ב'", "ג'", "ד'", "ה'", "ו'", "ש'"};
            StringBuilder result = new StringBuilder();
            
            for (int i = 1; i <= 7; i++) {
                DaySchedule schedule = court.getScheduleForDay(i);
                if (schedule != null && schedule.isActive()) {
                    if (result.length() > 0) {
                        result.append(", ");
                    }
                    result.append(dayNames[i - 1]);
                }
            }
            
            if (result.length() == 0) {
                // Try legacy format
                return formatActiveDays(court.getActiveDays());
            }
            
            return result.toString();
        }
        
        private String formatActiveDays(String activeDays) {
            if (activeDays == null || activeDays.isEmpty()) {
                return itemView.getContext().getString(R.string.court_all_week);
            }
            
            String[] days = activeDays.split(",");
            String[] dayNames = {"א'", "ב'", "ג'", "ד'", "ה'", "ו'", "ש'"};
            StringBuilder result = new StringBuilder();
            
            for (String day : days) {
                try {
                    int dayNum = Integer.parseInt(day.trim());
                    if (dayNum >= 1 && dayNum <= 7) {
                        if (result.length() > 0) {
                            result.append(", ");
                        }
                        result.append(dayNames[dayNum - 1]);
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid day numbers
                }
            }
            
            return result.length() > 0 ? result.toString() : itemView.getContext().getString(R.string.court_all_week);
        }
    }
}
