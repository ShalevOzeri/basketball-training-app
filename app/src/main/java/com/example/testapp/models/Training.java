package com.example.testapp.models;

public class Training {
    private String trainingId;
    private String teamId;
    private String teamName;
    private String teamColor;
    private String courtId;
    private String courtName;
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
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    public void setDate(long date) { this.date = date; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    // Helper method to check for time conflicts
    public boolean conflictsWith(Training other) {
        if (!this.courtId.equals(other.courtId) || !this.dayOfWeek.equals(other.dayOfWeek)) {
            return false;
        }
        
        int thisStart = timeToMinutes(this.startTime);
        int thisEnd = timeToMinutes(this.endTime);
        int otherStart = timeToMinutes(other.startTime);
        int otherEnd = timeToMinutes(other.endTime);
        
        return !(thisEnd <= otherStart || thisStart >= otherEnd);
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    public int getDurationInMinutes() {
        return timeToMinutes(endTime) - timeToMinutes(startTime);
    }
}
