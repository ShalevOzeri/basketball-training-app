package com.example.testapp.models;

import java.io.Serializable;

public class Training implements Serializable {
    private String trainingId;
    private String teamId;
    private String teamName;
    private String teamColor;
    private String courtId;
    private String courtName;
    private String courtType; // "Indoor hall" or "Shaded court"
    private String dayOfWeek; // "Sunday", "Monday", etc.
    private String startTime; // "16:00"
    private String endTime; // "18:00"
    private long date; // Timestamp for specific date
    private String notes;
    private long createdAt;
    private String createdBy;

    public Training() {
        // Required empty constructor for Firebase
    }

    public Training(String trainingId, String teamId, String teamName, String teamColor,
                   String courtId, String courtName, String dayOfWeek,
                   String startTime, String endTime, long date) {
        this.trainingId = trainingId;
        this.teamId = teamId;
        this.teamName = teamName;
        this.teamColor = teamColor;
        this.courtId = courtId;
        this.courtName = courtName;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.notes = "";
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public String getTrainingId() { return trainingId; }
    public String getTeamId() { return teamId; }
    public String getTeamName() { return teamName; }
    public String getTeamColor() { return teamColor; }
    public String getCourtId() { return courtId; }
    public String getCourtName() { return courtName; }
    public String getCourtType() { return courtType; }
    public String getDayOfWeek() { return dayOfWeek; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public long getDate() { return date; }
    public String getNotes() { return notes; }
    public long getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }

    // Setters
    public void setTrainingId(String trainingId) { this.trainingId = trainingId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public void setTeamColor(String teamColor) { this.teamColor = teamColor; }
    public void setCourtId(String courtId) { this.courtId = courtId; }
    public void setCourtName(String courtName) { this.courtName = courtName; }
    public void setCourtType(String courtType) { this.courtType = courtType; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setDate(long date) { this.date = date; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    // Helper method to check for time conflicts
    public boolean conflictsWith(Training other) {
        // Conflict only if same court and same calendar day
        if (!safeEquals(this.courtId, other.courtId) || !isSameDay(this.date, other.date)) {
            return false;
        }

        int thisStart = timeToMinutesSafe(this.startTime);
        int thisEnd = timeToMinutesSafe(this.endTime);
        int otherStart = timeToMinutesSafe(other.startTime);
        int otherEnd = timeToMinutesSafe(other.endTime);

        // If any times are invalid, skip conflict to avoid false positives from bad data
        if (thisStart < 0 || thisEnd < 0 || otherStart < 0 || otherEnd < 0) {
            android.util.Log.w("Training", "Invalid times in conflict check: thisStart=" + thisStart + ", thisEnd=" + thisEnd + 
                ", otherStart=" + otherStart + ", otherEnd=" + otherEnd);
            return false;
        }

        boolean hasConflict = !(thisEnd <= otherStart || thisStart >= otherEnd);
        
        if (hasConflict) {
            android.util.Log.d("Training", "CONFLICT DETECTED: " + this.teamName + " (" + this.startTime + "-" + this.endTime + ") vs " + 
                other.teamName + " (" + other.startTime + "-" + other.endTime + ")");
        }
        
        return hasConflict;
    }

    private boolean isSameDay(long first, long second) {
        java.util.Calendar cal1 = java.util.Calendar.getInstance();
        java.util.Calendar cal2 = java.util.Calendar.getInstance();
        cal1.setTimeInMillis(first);
        cal2.setTimeInMillis(second);
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR)
                && cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);
    }

    private boolean safeEquals(String a, String b) {
        if (a == null) {
            return b == null;
        }
        return a.equals(b);
    }

    private int timeToMinutesSafe(String time) {
        try {
            if (time == null) {
                return -1;
            }
            String[] parts = time.split(":");
            if (parts.length != 2) {
                return -1;
            }
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            if (h < 0 || h > 23 || m < 0 || m > 59) {
                return -1;
            }
            return h * 60 + m;
        } catch (Exception e) {
            return -1;
        }
    }

    public int getDurationInMinutes() {
        return timeToMinutesSafe(endTime) - timeToMinutesSafe(startTime);
    }
}
