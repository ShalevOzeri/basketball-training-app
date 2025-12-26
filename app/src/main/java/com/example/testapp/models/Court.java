package com.example.testapp.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Court {
    private String courtId;
    private String name;
    private String location;
    private boolean isAvailable;
    
    // Weekly schedule stored as separate fields to avoid Firebase serialization issues
    private DaySchedule day1; // Sunday
    private DaySchedule day2; // Monday
    private DaySchedule day3; // Tuesday
    private DaySchedule day4; // Wednesday
    private DaySchedule day5; // Thursday
    private DaySchedule day6; // Friday
    private DaySchedule day7; // Saturday
    
    // Legacy fields for backward compatibility
    private String openingHour; // "06:00"
    private String closingHour; // "23:00"
    private String activeDays; // "1,2,3,4,5" (Sunday=1, Monday=2, etc.)
    private long createdAt;

    public Court() {
        // Required empty constructor for Firebase
        initializeDefaultSchedule();
    }

    private void initializeDefaultSchedule() {
        // Initialize all days with default hours (8:00-22:00, active)
        day1 = new DaySchedule(true, "08:00", "22:00");
        day2 = new DaySchedule(true, "08:00", "22:00");
        day3 = new DaySchedule(true, "08:00", "22:00");
        day4 = new DaySchedule(true, "08:00", "22:00");
        day5 = new DaySchedule(true, "08:00", "22:00");
        day6 = new DaySchedule(true, "08:00", "22:00");
        day7 = new DaySchedule(true, "08:00", "22:00");
    }

    public Court(String courtId, String name, String location, String openingHour, String closingHour) {
        this.courtId = courtId;
        this.name = name;
        this.location = location;
        this.isAvailable = true;
        this.openingHour = openingHour;
        this.closingHour = closingHour;
        this.activeDays = "1,2,3,4,5,6,7"; // All days by default
        this.createdAt = System.currentTimeMillis();
        initializeDefaultSchedule();
    }
    
    public Court(String courtId, String name, String location, String openingHour, String closingHour, String activeDays) {
        this.courtId = courtId;
        this.name = name;
        this.location = location;
        this.isAvailable = true;
        this.openingHour = openingHour;
        this.closingHour = closingHour;
        this.activeDays = activeDays;
        this.createdAt = System.currentTimeMillis();
        initializeDefaultSchedule();
    }

    // Getters
    public String getCourtId() { return courtId; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public boolean isAvailable() { return isAvailable; }
    
    // Day schedule getters
    public DaySchedule getDay1() { return day1; }
    public DaySchedule getDay2() { return day2; }
    public DaySchedule getDay3() { return day3; }
    public DaySchedule getDay4() { return day4; }
    public DaySchedule getDay5() { return day5; }
    public DaySchedule getDay6() { return day6; }
    public DaySchedule getDay7() { return day7; }
    
    // Legacy getters for backward compatibility
    public String getOpeningHour() { return openingHour; }
    public String getClosingHour() { return closingHour; }
    public String getActiveDays() { return activeDays; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setCourtId(String courtId) { this.courtId = courtId; }
    public void setName(String name) { this.name = name; }
    public void setLocation(String location) { this.location = location; }
    public void setAvailable(boolean available) { isAvailable = available; }
    
    // Day schedule setters
    public void setDay1(DaySchedule day1) { this.day1 = day1; }
    public void setDay2(DaySchedule day2) { this.day2 = day2; }
    public void setDay3(DaySchedule day3) { this.day3 = day3; }
    public void setDay4(DaySchedule day4) { this.day4 = day4; }
    public void setDay5(DaySchedule day5) { this.day5 = day5; }
    public void setDay6(DaySchedule day6) { this.day6 = day6; }
    public void setDay7(DaySchedule day7) { this.day7 = day7; }
    
    // Legacy setters for backward compatibility
    public void setOpeningHour(String openingHour) { this.openingHour = openingHour; }
    public void setClosingHour(String closingHour) { this.closingHour = closingHour; }
    public void setActiveDays(String activeDays) { this.activeDays = activeDays; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    // Helper method to get weekly schedule as Map (excluded from Firebase)
    @Exclude
    public Map<String, DaySchedule> getWeeklySchedule() { 
        Map<String, DaySchedule> schedule = new HashMap<>();
        schedule.put("1", day1 != null ? day1 : new DaySchedule(true, "08:00", "22:00"));
        schedule.put("2", day2 != null ? day2 : new DaySchedule(true, "08:00", "22:00"));
        schedule.put("3", day3 != null ? day3 : new DaySchedule(true, "08:00", "22:00"));
        schedule.put("4", day4 != null ? day4 : new DaySchedule(true, "08:00", "22:00"));
        schedule.put("5", day5 != null ? day5 : new DaySchedule(true, "08:00", "22:00"));
        schedule.put("6", day6 != null ? day6 : new DaySchedule(true, "08:00", "22:00"));
        schedule.put("7", day7 != null ? day7 : new DaySchedule(true, "08:00", "22:00"));
        return schedule;
    }
    
    // Helper method to set weekly schedule from Map (excluded from Firebase)
    @Exclude
    public void setWeeklySchedule(Map<String, DaySchedule> schedule) { 
        if (schedule != null) {
            this.day1 = schedule.get("1");
            this.day2 = schedule.get("2");
            this.day3 = schedule.get("3");
            this.day4 = schedule.get("4");
            this.day5 = schedule.get("5");
            this.day6 = schedule.get("6");
            this.day7 = schedule.get("7");
        }
    }
    
    // Helper methods for specific days
    @Exclude
    public DaySchedule getScheduleForDay(int dayOfWeek) {
        switch (dayOfWeek) {
            case 1: return day1 != null ? day1 : new DaySchedule(true, "08:00", "22:00");
            case 2: return day2 != null ? day2 : new DaySchedule(true, "08:00", "22:00");
            case 3: return day3 != null ? day3 : new DaySchedule(true, "08:00", "22:00");
            case 4: return day4 != null ? day4 : new DaySchedule(true, "08:00", "22:00");
            case 5: return day5 != null ? day5 : new DaySchedule(true, "08:00", "22:00");
            case 6: return day6 != null ? day6 : new DaySchedule(true, "08:00", "22:00");
            case 7: return day7 != null ? day7 : new DaySchedule(true, "08:00", "22:00");
            default: return new DaySchedule(true, "08:00", "22:00");
        }
    }
    
    @Exclude
    public void setScheduleForDay(int dayOfWeek, DaySchedule schedule) {
        switch (dayOfWeek) {
            case 1: day1 = schedule; break;
            case 2: day2 = schedule; break;
            case 3: day3 = schedule; break;
            case 4: day4 = schedule; break;
            case 5: day5 = schedule; break;
            case 6: day6 = schedule; break;
            case 7: day7 = schedule; break;
        }
    }
    
    @Exclude
    public boolean isActiveOnDay(int dayOfWeek) {
        DaySchedule schedule = getScheduleForDay(dayOfWeek);
        return schedule != null && schedule.isActive();
    }

    @Override
    public String toString() {
        return name;
    }
}
