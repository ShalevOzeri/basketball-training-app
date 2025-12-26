package com.example.testapp.utils;

public class Constants {
    
    // User Roles
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_COORDINATOR = "COORDINATOR";
    public static final String ROLE_COACH = "COACH";
    
    // Days of Week
    public static final String[] DAYS_OF_WEEK = {
        "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"
    };
    
    // Age Groups
    public static final String[] AGE_GROUPS = {
        "U10", "U12", "U14", "U16", "U18", "U20", "Senior"
    };
    
    // Team Levels
    public static final String[] TEAM_LEVELS = {
        "Beginner", "Intermediate", "Advanced", "Professional"
    };
    
    // Time slots (in minutes)
    public static final int SLOT_DURATION = 30; // 30 minutes
    public static final int START_HOUR = 6; // 6 AM
    public static final int END_HOUR = 23; // 11 PM
    
    // Database nodes
    public static final String NODE_USERS = "users";
    public static final String NODE_TEAMS = "teams";
    public static final String NODE_COURTS = "courts";
    public static final String NODE_TRAININGS = "trainings";
}
