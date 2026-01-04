package com.example.testapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Team implements Parcelable {
    private String teamId;
    private String name;
    private String ageGroup; // "U12", "U14", "U16", etc.
    private String level; // "Beginner", "Intermediate", "Advanced"
    private String coachId;
    private String coachName;
    private String color; // Hex color for visual representation
    private int numberOfPlayers;
    private long createdAt;
    private long updatedAt;

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
        this.updatedAt = System.currentTimeMillis();
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
    public long getUpdatedAt() { return updatedAt; }

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
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return name + " (" + ageGroup + ")";
    }

    // Parcelable implementation
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(teamId);
        dest.writeString(name);
        dest.writeString(ageGroup);
        dest.writeString(level);
        dest.writeString(coachId);
        dest.writeString(coachName);
        dest.writeString(color);
        dest.writeInt(numberOfPlayers);
        dest.writeLong(createdAt);
        dest.writeLong(updatedAt);
    }

    private Team(Parcel in) {
        teamId = in.readString();
        name = in.readString();
        ageGroup = in.readString();
        level = in.readString();
        coachId = in.readString();
        coachName = in.readString();
        color = in.readString();
        numberOfPlayers = in.readInt();
        createdAt = in.readLong();
        updatedAt = in.readLong();
    }

    public static final Creator<Team> CREATOR = new Creator<Team>() {
        @Override
        public Team createFromParcel(Parcel in) {
            return new Team(in);
        }

        @Override
        public Team[] newArray(int size) {
            return new Team[size];
        }
    };
}
