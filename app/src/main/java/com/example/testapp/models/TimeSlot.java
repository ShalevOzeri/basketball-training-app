package com.example.testapp.models;

/**
 * Represents a time slot in the schedule grid
 */
public class TimeSlot {
    private String courtId;
    private String courtName;
    private String startTime; // "08:00"
    private String endTime;   // "09:30"
    private long date;        // Timestamp for the specific date
    private boolean isAvailable;
    private Training training; // Scheduled training in this slot, if any
    
    public TimeSlot() {
        // Required empty constructor
    }
    
    public TimeSlot(String courtId, String courtName, String startTime, String endTime, long date) {
        this.courtId = courtId;
        this.courtName = courtName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.isAvailable = true;
        this.training = null;
    }
    
    // Getters
    public String getCourtId() { return courtId; }
    public String getCourtName() { return courtName; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public long getDate() { return date; }
    public boolean isAvailable() { return isAvailable; }
    public Training getTraining() { return training; }
    
    // Setters
    public void setCourtId(String courtId) { this.courtId = courtId; }
    public void setCourtName(String courtName) { this.courtName = courtName; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setDate(long date) { this.date = date; }
    public void setAvailable(boolean available) { isAvailable = available; }
    public void setTraining(Training training) { 
        this.training = training;
        this.isAvailable = (training == null);
    }
    
    /**
     * Checks whether this time slot conflicts with another training session
     */
    public boolean conflictsWithTraining(Training other) {
        if (other == null || !courtId.equals(other.getCourtId())) {
            return false;
        }
        
        // Check if same day
        if (!isSameDay(this.date, other.getDate())) {
            return false;
        }
        
        int slotStart = timeToMinutes(this.startTime);
        int slotEnd = timeToMinutes(this.endTime);
        int otherStart = timeToMinutes(other.getStartTime());
        int otherEnd = timeToMinutes(other.getEndTime());
        
        // Check for overlap
        return !(slotEnd <= otherStart || slotStart >= otherEnd);
    }
    
    private boolean isSameDay(long first, long second) {
        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal1.setTimeInMillis(first);
        cal2.setTimeInMillis(second);
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
               cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);
    }
    
    private int timeToMinutes(String time) {
        if (time == null || time.isEmpty()) return 0;
        String[] parts = time.split(":");
        if (parts.length != 2) return 0;
        try {
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    @Override
    public String toString() {
        return startTime + " - " + endTime;
    }
}
