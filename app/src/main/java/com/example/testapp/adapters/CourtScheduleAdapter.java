package com.example.testapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.R;
import com.example.testapp.models.Court;
import com.example.testapp.models.TimeSlot;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying courts with their time slot schedules
 */
public class CourtScheduleAdapter extends RecyclerView.Adapter<CourtScheduleAdapter.CourtViewHolder> {
    
    private Context context;
    private List<CourtScheduleData> scheduleDataList = new ArrayList<>();
    private OnTimeSlotClickListener listener;
    
    public interface OnTimeSlotClickListener {
        void onTimeSlotClick(TimeSlot timeSlot);
    }
    
    /**
     * Container for a court and its time slots
     */
    public static class CourtScheduleData {
        public Court court;
        public List<TimeSlot> timeSlots;
        
        public CourtScheduleData(Court court, List<TimeSlot> timeSlots) {
            this.court = court;
            this.timeSlots = timeSlots;
        }
    }
    
    public CourtScheduleAdapter(Context context, OnTimeSlotClickListener listener) {
        this.context = context;
        this.listener = listener;
    }
    
    public void setScheduleData(List<CourtScheduleData> scheduleDataList) {
        this.scheduleDataList = scheduleDataList;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public CourtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_court_schedule, parent, false);
        return new CourtViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CourtViewHolder holder, int position) {
        CourtScheduleData data = scheduleDataList.get(position);
        holder.bind(data, listener);
    }
    
    @Override
    public int getItemCount() {
        return scheduleDataList.size();
    }
    
    /**
     * ViewHolder for a court and its time slots
     */
    static class CourtViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourtName;
        TextView tvCourtLocation;
        TextView tvCourtType;
        RecyclerView recyclerViewTimeSlots;
        TimeSlotsAdapter timeSlotsAdapter;
        
        public CourtViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourtName = itemView.findViewById(R.id.tvCourtName);
            tvCourtLocation = itemView.findViewById(R.id.tvCourtLocation);
            tvCourtType = itemView.findViewById(R.id.tvCourtType);
            recyclerViewTimeSlots = itemView.findViewById(R.id.recyclerViewTimeSlots);
            
            // Setup horizontal RecyclerView for time slots
            recyclerViewTimeSlots.setLayoutManager(
                new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false)
            );
        }
        
        void bind(CourtScheduleData data, OnTimeSlotClickListener listener) {
            Court court = data.court;
            
            tvCourtName.setText(court.getName());
            tvCourtLocation.setText(court.getLocation() != null ? court.getLocation() : "");
            tvCourtType.setText(court.getCourtType() != null ? court.getCourtType() : "");
            
            // Setup time slots adapter
            timeSlotsAdapter = new TimeSlotsAdapter(data.timeSlots, listener);
            recyclerViewTimeSlots.setAdapter(timeSlotsAdapter);
        }
    }
    
    /**
     * Nested adapter for time slots
     */
    static class TimeSlotsAdapter extends RecyclerView.Adapter<TimeSlotsAdapter.TimeSlotViewHolder> {
        
        private List<TimeSlot> timeSlots;
        private OnTimeSlotClickListener listener;
        
        TimeSlotsAdapter(List<TimeSlot> timeSlots, OnTimeSlotClickListener listener) {
            this.timeSlots = timeSlots;
            this.listener = listener;
        }
        
        @NonNull
        @Override
        public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_time_slot, parent, false);
            return new TimeSlotViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
            TimeSlot timeSlot = timeSlots.get(position);
            holder.bind(timeSlot, listener);
        }
        
        @Override
        public int getItemCount() {
            return timeSlots.size();
        }
        
        /**
         * ViewHolder for a single time slot
         */
        static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView tvTimeRange;
            TextView tvTeamName;
            View colorIndicator;
            
            TimeSlotViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.cardTimeSlot);
                tvTimeRange = itemView.findViewById(R.id.tvTimeRange);
                tvTeamName = itemView.findViewById(R.id.tvTeamName);
                colorIndicator = itemView.findViewById(R.id.colorIndicator);
            }
            
            void bind(TimeSlot timeSlot, OnTimeSlotClickListener listener) {
                tvTimeRange.setText(timeSlot.getStartTime() + "\n-\n" + timeSlot.getEndTime());
                
                if (timeSlot.isAvailable()) {
                    // Available slot
                    tvTeamName.setText("פנוי");
                    tvTeamName.setTextColor(Color.parseColor("#4CAF50")); // Green
                    cardView.setCardBackgroundColor(Color.parseColor("#E8F5E9"));
                    colorIndicator.setBackgroundColor(Color.parseColor("#4CAF50"));
                    cardView.setCardElevation(2f);
                } else if (timeSlot.getTraining() != null) {
                    // Occupied slot
                    String teamName = timeSlot.getTraining().getTeamName();
                    tvTeamName.setText(teamName);
                    tvTeamName.setTextColor(Color.BLACK);
                    
                    // Team color
                    String teamColor = timeSlot.getTraining().getTeamColor();
                    if (teamColor != null && !teamColor.isEmpty()) {
                        try {
                            int color = Color.parseColor(teamColor);
                            colorIndicator.setBackgroundColor(color);
                            // Softer background shade of the same color
                            int lightColor = adjustColorAlpha(color, 0.2f);
                            cardView.setCardBackgroundColor(lightColor);
                        } catch (IllegalArgumentException e) {
                            cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
                            colorIndicator.setBackgroundColor(Color.parseColor("#F44336"));
                        }
                    } else {
                        cardView.setCardBackgroundColor(Color.parseColor("#FFEBEE"));
                        colorIndicator.setBackgroundColor(Color.parseColor("#F44336"));
                    }
                    cardView.setCardElevation(4f);
                }
                
                // Click listener
                cardView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onTimeSlotClick(timeSlot);
                    }
                });
            }
            
            /**
             * Creates a lighter color with the given alpha
             */
            private int adjustColorAlpha(int color, float alphaFactor) {
                int alpha = Math.round(255 * alphaFactor);
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
                return Color.argb(alpha, red, green, blue);
            }
        }
    }
}
