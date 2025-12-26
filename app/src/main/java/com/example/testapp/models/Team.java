package com.example.testapp.models;

public class Team {
    private String teamId;
    private String name;
    private String ageGroup; // "U12", "U14", "U16", etc.
    private String level; // "Beginner", "Intermediate", "Advanced"
    private String coachId;
    private String coachName;
    private String color; // Hex color for visual representation
    private int numberOfPlayers;
    private long createdAt;

    public Team() {
        // Required empty constructor for Firebase
    }

    public Team(String teamId, String name, String ageGroup, String level, String coachId, String coachName, String color) {
        this.teamId = teamId;
        this.name = name;
        this.ageGroup = ageGroup;
        this.level = level;
        this.coachId = coachId;
        this.coachName = coachName;
        this.color = color;
        this.numberOfPlayers = 0;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public String getTeamId() { return teamId; }
    public String getName() { return name; }
    public String getAgeGroup() { return ageGroup; }
    public String getLevel() { return level; }
    public String getCoachId() { return coachId; }
    public String getCoachName() { return coachName; }
    public String getColor() { return color; }
    public int getNumberOfPlayers() { return numberOfPlayers; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setTeamId(String teamId) { this.teamId = teamId; }
    public void setName(String name) { this.name = name; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }
    public void setLevel(String level) { this.level = level; }
    public void setCoachId(String coachId) { this.coachId = coachId; }
    public void setCoachName(String coachName) { this.coachName = coachName; }
    public void setColor(String color) { this.color = color; }
    public void setNumberOfPlayers(int numberOfPlayers) { this.numberOfPlayers = numberOfPlayers; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return name + " (" + ageGroup + ")";
    }
}
