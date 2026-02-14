package com.example.testapp.utils;

import org.junit.Test;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for TimeSlotUtils
 * 
 * Tests the functions that handle time slots:
 * - Creating a list of time slots
 * - Checking if time is within a certain range
 */
public class TimeSlotUtilsTest {

    @Test
    public void generateTimeSlots_MorningSession_ReturnsCorrectSlots() {
        System.out.println("🧪 Testing time slot generation - morning");
        // Test: Generate time slots from 09:00 to 11:00
        List<String> slots = TimeSlotUtils.generateTimeSlots("09:00", "11:00");
        
        assertNotNull("Slots should not be null", slots);
        assertEquals("Should have 5 slots (09:00, 09:30, 10:00, 10:30, 11:00)", 5, slots.size());
        assertEquals("First slot should be 09:00", "09:00", slots.get(0));
        assertEquals("Second slot should be 09:30", "09:30", slots.get(1));
        assertEquals("Third slot should be 10:00", "10:00", slots.get(2));
        assertEquals("Fourth slot should be 10:30", "10:30", slots.get(3));
        assertEquals("Fifth slot should be 11:00", "11:00", slots.get(4));
    }

    @Test
    public void generateTimeSlots_EveningSession_ReturnsCorrectSlots() {
        System.out.println("🧪 Testing time slot generation - evening");
        // Test: Generate time slots from 18:00 to 20:00
        List<String> slots = TimeSlotUtils.generateTimeSlots("18:00", "20:00");
        
        assertNotNull("Slots should not be null", slots);
        assertEquals("Should have 5 slots", 5, slots.size());
        assertEquals("First slot should be 18:00", "18:00", slots.get(0));
        assertEquals("Last slot should be 20:00", "20:00", slots.get(4));
    }

    @Test
    public void generateTimeSlots_OneHour_ReturnsThreeSlots() {
        System.out.println("🧪 Testing time slots - one hour");
        // Test: Time slots for one hour (00, 30, 60)
        List<String> slots = TimeSlotUtils.generateTimeSlots("14:00", "15:00");
        
        assertNotNull("Slots should not be null", slots);
        assertEquals("Should have 3 slots (14:00, 14:30, 15:00)", 3, slots.size());
        assertEquals("14:00", slots.get(0));
        assertEquals("14:30", slots.get(1));
        assertEquals("15:00", slots.get(2));
    }

    @Test
    public void generateTimeSlots_ThirtyMinutes_ReturnsTwoSlots() {
        System.out.println("🧪 Testing time slots - 30 minutes");
        // Test: Time slots for 30 minutes
        List<String> slots = TimeSlotUtils.generateTimeSlots("10:00", "10:30");
        
        assertNotNull("Slots should not be null", slots);
        assertEquals("Should have 2 slots", 2, slots.size());
        assertEquals("10:00", slots.get(0));
        assertEquals("10:30", slots.get(1));
    }

    @Test
    public void generateTimeSlots_StartingAtHalfHour_WorksCorrectly() {
        System.out.println("🧪 Testing start at half hour");
        // Test: Start at half hour
        List<String> slots = TimeSlotUtils.generateTimeSlots("09:30", "11:00");
        
        assertNotNull("Slots should not be null", slots);
        assertEquals("Should have 4 slots", 4, slots.size());
        assertEquals("09:30", slots.get(0));
        assertEquals("10:00", slots.get(1));
        assertEquals("10:30", slots.get(2));
        assertEquals("11:00", slots.get(3));
    }

    @Test
    public void generateTimeSlots_CrossingNoon_WorksCorrectly() {
        System.out.println("🧪 Testing crossing noon");
        // Test: Time slots crossing noon
        List<String> slots = TimeSlotUtils.generateTimeSlots("11:30", "13:00");
        
        assertNotNull("Slots should not be null", slots);
        assertEquals("Should have 4 slots", 4, slots.size());
        assertEquals("11:30", slots.get(0));
        assertEquals("12:00", slots.get(1));
        assertEquals("12:30", slots.get(2));
        assertEquals("13:00", slots.get(3));
    }

    @Test
    public void generateTimeSlots_FullDay_ReturnsCorrectCount() {
        System.out.println("🧪 Testing time slots - full day");
        // Test: Full day (08:00 - 22:00)
        List<String> slots = TimeSlotUtils.generateTimeSlots("08:00", "22:00");
        
        assertNotNull("Slots should not be null", slots);
        // From 08:00 to 22:00 is 14 hours * 2 (every half hour) + 1 = 29 slots
        assertEquals("Should have 29 slots for 14 hours", 29, slots.size());
        assertEquals("First slot should be 08:00", "08:00", slots.get(0));
        assertEquals("Last slot should be 22:00", "22:00", slots.get(slots.size() - 1));
    }

    @Test
    public void generateTimeSlots_InvalidFormat_ReturnsEmptyList() {
        System.out.println("🧪 Testing invalid format");
        // Test: Invalid format returns empty list
        List<String> slots = TimeSlotUtils.generateTimeSlots("invalid", "11:00");
        
        assertNotNull("Slots should not be null even with invalid input", slots);
        assertTrue("Should return empty list for invalid input", slots.isEmpty());
    }

    @Test
    public void generateTimeSlots_EndBeforeStart_ReturnsEmptyList() {
        System.out.println("🧪 Testing end before start");
        // Test: End time before start time
        List<String> slots = TimeSlotUtils.generateTimeSlots("14:00", "12:00");
        
        assertNotNull("Slots should not be null", slots);
        assertTrue("Should return empty list when end is before start", slots.isEmpty());
    }

    @Test
    public void isTimeInRange_TimeInsideRange_ReturnsTrue() {
        System.out.println("🧪 Testing time in range - inside range");
        // Test: Time inside range
        boolean result = TimeSlotUtils.isTimeInRange("10:30", "10:00", "11:00");
        
        assertTrue("10:30 should be in range 10:00-11:00", result);
    }

    @Test
    public void isTimeInRange_TimeAtStart_ReturnsTrue() {
        System.out.println("🧪 Testing time in range - at start of range");
        // Test: Time at start of range
        boolean result = TimeSlotUtils.isTimeInRange("10:00", "10:00", "11:00");
        
        assertTrue("10:00 should be in range 10:00-11:00 (inclusive start)", result);
    }

    @Test
    public void isTimeInRange_TimeAtEnd_ReturnsFalse() {
        System.out.println("🧪 Testing time in range - at end of range (exclusive)");
        // Test: Time at end of range (exclusive)
        boolean result = TimeSlotUtils.isTimeInRange("11:00", "10:00", "11:00");
        
        assertFalse("11:00 should NOT be in range 10:00-11:00 (exclusive end)", result);
    }

    @Test
    public void isTimeInRange_TimeBeforeRange_ReturnsFalse() {
        System.out.println("🧪 Testing time in range - before range");
        // Test: Time before range
        boolean result = TimeSlotUtils.isTimeInRange("09:30", "10:00", "11:00");
        
        assertFalse("09:30 should not be in range 10:00-11:00", result);
    }

    @Test
    public void isTimeInRange_TimeAfterRange_ReturnsFalse() {
        System.out.println("🧪 Testing time in range - after range");
        // Test: Time after range
        boolean result = TimeSlotUtils.isTimeInRange("11:30", "10:00", "11:00");
        
        assertFalse("11:30 should not be in range 10:00-11:00", result);
    }

    @Test
    public void isTimeInRange_MorningTime_ReturnsTrue() {
        System.out.println("🧪 Testing morning time in range");
        // Test: Morning time
        boolean result = TimeSlotUtils.isTimeInRange("09:15", "09:00", "12:00");
        
        assertTrue("09:15 should be in range 09:00-12:00", result);
    }

    @Test
    public void isTimeInRange_EveningTime_ReturnsTrue() {
        System.out.println("🧪 Testing evening time in range");
        // Test: Evening time
        boolean result = TimeSlotUtils.isTimeInRange("19:45", "18:00", "21:00");
        
        assertTrue("19:45 should be in range 18:00-21:00", result);
    }

    @Test
    public void isTimeInRange_InvalidTimeFormat_ReturnsFalse() {
        System.out.println("🧪 Testing invalid time format");
        // Test: Invalid time format
        boolean result = TimeSlotUtils.isTimeInRange("invalid", "10:00", "11:00");
        
        assertFalse("Invalid time format should return false", result);
    }

    @Test
    public void isTimeInRange_InvalidRangeFormat_ReturnsFalse() {
        System.out.println("🧪 Testing invalid range format");
        // Test: Invalid range format
        boolean result = TimeSlotUtils.isTimeInRange("10:30", "invalid", "11:00");
        
        assertFalse("Invalid range format should return false", result);
    }

    @Test
    public void isTimeInRange_MidnightTime_WorksCorrectly() {
        System.out.println("🧪 Testing midnight time");
        // Test: Midnight time
        boolean result = TimeSlotUtils.isTimeInRange("00:30", "00:00", "01:00");
        
        assertTrue("00:30 should be in range 00:00-01:00", result);
    }

    @Test
    public void isTimeInRange_LateNight_WorksCorrectly() {
        System.out.println("🧪 Testing late night time");
        // Test: Late night time
        boolean result = TimeSlotUtils.isTimeInRange("23:30", "23:00", "23:59");
        
        assertTrue("23:30 should be in range 23:00-23:59", result);
    }
}
