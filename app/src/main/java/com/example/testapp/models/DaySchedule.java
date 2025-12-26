package com.example.testapp.models;

public class DaySchedule {
    private boolean isActive;
    private String openingHour; // "06:00"
    private String closingHour; // "23:00"

    public DaySchedule() {
        // Required empty constructor for Firebase
        this.isActive = false;
        this.openingHour = "08:00";
        this.closingHour = "22:00";
    }

    public DaySchedule(boolean isActive, String openingHour, String closingHour) {
        this.isActive = isActive;
        this.openingHour = openingHour;
        this.closingHour = closingHour;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getOpeningHour() {
        return openingHour;
    }

    public void setOpeningHour(String openingHour) {
        this.openingHour = openingHour;
    }

    public String getClosingHour() {
        return closingHour;
    }

    public void setClosingHour(String closingHour) {
        this.closingHour = closingHour;
    }

    @Override
    public String toString() {
        if (!isActive) {
            return "סגור";
        }
        return openingHour + " - " + closingHour;
    }
}
