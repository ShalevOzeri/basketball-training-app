package com.example.testapp.utils;

import java.util.ArrayList;
import java.util.List;

public class TimeSlotUtils {
    
    public static List<String> generateTimeSlots(String startTime, String endTime) {
        List<String> slots = new ArrayList<>();
        
        try {
            int startHour = Integer.parseInt(startTime.split(":")[0]);
            int startMinute = Integer.parseInt(startTime.split(":")[1]);
            int endHour = Integer.parseInt(endTime.split(":")[0]);
            int endMinute = Integer.parseInt(endTime.split(":")[1]);
            
            int currentHour = startHour;
            int currentMinute = startMinute;
            
            while (currentHour < endHour || (currentHour == endHour && currentMinute <= endMinute)) {
                String slot = String.format("%02d:%02d", currentHour, currentMinute);
                slots.add(slot);
                
                currentMinute += 30;
                if (currentMinute >= 60) {
                    currentMinute = 0;
                    currentHour++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return slots;
    }
    
    public static boolean isTimeInRange(String time, String start, String end) {
        try {
            int timeMinutes = timeToMinutes(time);
            int startMinutes = timeToMinutes(start);
            int endMinutes = timeToMinutes(end);
            
            return timeMinutes >= startMinutes && timeMinutes < endMinutes;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
}
