package com.example.testapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.R;
import com.example.testapp.models.TimeSlot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying a full week for a single court
 */
public class WeekScheduleAdapter extends RecyclerView.Adapter<WeekScheduleAdapter.DayViewHolder> {
    
    private Context context;
    private List<DayScheduleData> weekData = new ArrayList<>();
    private OnTimeSlotClickListener listener;
    private OnOccupiedSlotClickListener occupiedSlotListener;
    
    public interface OnTimeSlotClickListener {
        void onTimeSlotClick(TimeSlot timeSlot);
    }
    
    public interface OnOccupiedSlotClickListener {
        void onOccupiedSlotClick(TimeSlot timeSlot);
    }
    
    /**
     * Holder for a single day's schedule data
     */
    public static class DayScheduleData {
        public Calendar date;
        public List<TimeSlot> timeSlots;
        public boolean isActive;
        
        public DayScheduleData(Calendar date, List<TimeSlot> timeSlots, boolean isActive) {
            this.date = date;
            this.timeSlots = timeSlots;
            this.isActive = isActive;
        }
    }
    
    public WeekScheduleAdapter(Context context, OnTimeSlotClickListener listener) {
        this.context = context;
        this.listener = listener;
    }
    
    public void setOccupiedSlotListener(OnOccupiedSlotClickListener listener) {
        this.occupiedSlotListener = listener;
    }
    
    public void setWeekData(List<DayScheduleData> weekData) {
        this.weekData = weekData;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_week_day_schedule, parent, false);
        return new DayViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        DayScheduleData data = weekData.get(position);
        holder.bind(data, listener, occupiedSlotListener);
    }
    
    @Override
    public int getItemCount() {
        return weekData.size();
    }
    
    /**
     * ViewHolder for a single day with its time slots
     */
    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayName;
        TextView tvDayDate;
        RecyclerView recyclerViewTimeSlots;
        
        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayName = itemView.findViewById(R.id.tvDayName);
            tvDayDate = itemView.findViewById(R.id.tvDayDate);
            recyclerViewTimeSlots = itemView.findViewById(R.id.recyclerViewTimeSlots);
            
            // Setup horizontal RecyclerView for time slots
            recyclerViewTimeSlots.setLayoutManager(
                new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false)
            );
        }
        
        void bind(DayScheduleData data, OnTimeSlotClickListener listener, OnOccupiedSlotClickListener occupiedListener) {
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale.Builder().setLanguage("he").setRegion("IL").build());
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            
            tvDayName.setText(dayFormat.format(data.date.getTime()));
            tvDayDate.setText(dateFormat.format(data.date.getTime()));
            
            if (!data.isActive || data.timeSlots.isEmpty()) {
                tvDayName.setAlpha(0.5f);
                tvDayDate.setAlpha(0.5f);
                tvDayDate.setText(tvDayDate.getText() + " - סגור");
                recyclerViewTimeSlots.setAdapter(null);
            } else {
                tvDayName.setAlpha(1.0f);
                tvDayDate.setAlpha(1.0f);
                
                // Setup time slots adapter
                TimeSlotsAdapter timeSlotsAdapter = 
                    new TimeSlotsAdapter(data.timeSlots, listener, occupiedListener);
                recyclerViewTimeSlots.setAdapter(timeSlotsAdapter);
            }
        }
    }
    
    /**
     * Adapter for horizontal time slots within a day
     */
    static class TimeSlotsAdapter extends RecyclerView.Adapter<TimeSlotsAdapter.TimeSlotViewHolder> {
        private List<TimeSlot> timeSlots;
        private OnTimeSlotClickListener listener;
        private OnOccupiedSlotClickListener occupiedSlotListener;
        
        public TimeSlotsAdapter(List<TimeSlot> timeSlots, OnTimeSlotClickListener listener, OnOccupiedSlotClickListener occupiedSlotListener) {
            this.timeSlots = timeSlots;
            this.listener = listener;
            this.occupiedSlotListener = occupiedSlotListener;
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
            holder.bind(timeSlot, listener, occupiedSlotListener);
        }
        
        @Override
        public int getItemCount() {
            return timeSlots.size();
        }
        
        static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
            TextView tvTimeRange;
            TextView tvTeamName;
            View colorIndicator;
            View cardTimeSlot;
            
            public TimeSlotViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTimeRange = itemView.findViewById(R.id.tvTimeRange);
                tvTeamName = itemView.findViewById(R.id.tvTeamName);
                colorIndicator = itemView.findViewById(R.id.colorIndicator);
                cardTimeSlot = itemView.findViewById(R.id.cardTimeSlot);
            }
            
            void bind(TimeSlot timeSlot, OnTimeSlotClickListener listener, OnOccupiedSlotClickListener occupiedListener) {
                tvTimeRange.setText(timeSlot.getStartTime() + "\n-\n" + timeSlot.getEndTime());
                
                if (timeSlot.isAvailable()) {
                    // Available slot
                    colorIndicator.setBackgroundColor(0xFF4CAF50); // Green
                    tvTeamName.setText("פנוי");
                    tvTeamName.setTextColor(0xFF4CAF50);
                    cardTimeSlot.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onTimeSlotClick(timeSlot);
                        }
                    });
                    cardTimeSlot.setClickable(true);
                } else {
                    // Occupied slot - still clickable to allow editing
                    if (timeSlot.getTraining() != null) {
                        tvTeamName.setText(timeSlot.getTraining().getTeamName());
                        
                        // Use team color if available, otherwise use red
                        String teamColor = timeSlot.getTraining().getTeamColor();
                        if (teamColor != null && !teamColor.isEmpty()) {
                            try {
                                int color = android.graphics.Color.parseColor(teamColor);
                                colorIndicator.setBackgroundColor(color);
                                tvTeamName.setTextColor(color);
                            } catch (Exception e) {
                                // Fallback to red if color parsing fails
                                colorIndicator.setBackgroundColor(0xFFF44336);
                                tvTeamName.setTextColor(0xFFF44336);
                            }
                        } else {
                            // Fallback to red if no color is stored
                            colorIndicator.setBackgroundColor(0xFFF44336);
                            tvTeamName.setTextColor(0xFFF44336);
                        }
                    } else {
                        // Occupied but no training data - hide team name
                        tvTeamName.setText("");
                        colorIndicator.setBackgroundColor(0xFFF44336); // Red
                        tvTeamName.setTextColor(0xFFF44336);
                    }
                    
                    // Allow clicking on occupied slots to edit/delete them
                    cardTimeSlot.setOnClickListener(v -> {
                        if (occupiedListener != null) {
                            occupiedListener.onOccupiedSlotClick(timeSlot);
                        }
                    });
                    cardTimeSlot.setClickable(true);
                }
            }
        }
    }
}
