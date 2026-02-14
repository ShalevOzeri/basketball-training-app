package com.example.testapp.models;

import com.example.testapp.models.Court;
import com.example.testapp.models.DaySchedule;

import org.junit.Test;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit Tests for Court model
 * Tests court creation, schedule management, and availability logic
 */
public class CourtTest {

    @Test
    public void testCreateCourt_AllFieldsSet() {
        System.out.println("🧪 Test: Creating court with all fields");
        // Act
        Court court = new Court(
            "court123",
            "מגרש מרכזי",
            "אולם ספורט גני תקווה",
            "06:00",
            "23:00"
        );
        
        // Assert
        assertEquals("court123", court.getCourtId());
        assertEquals("מגרש מרכזי", court.getName());
        assertEquals("אולם ספורט גני תקווה", court.getLocation());
        assertTrue(court.isAvailable());
        assertEquals("06:00", court.getOpeningHour());
        assertEquals("23:00", court.getClosingHour());
        assertEquals("1,2,3,4,5,6,7", court.getActiveDays()); // All days active
        assertTrue(court.getCreatedAt() > 0);
    }

    @Test
    public void testCourtInitialization_DefaultSchedule() {
        System.out.println("🧪 Test: Court initialization - default schedule");
        // Act
        Court court = new Court();
        
        // Assert - All days should have default schedule (08:00-22:00, active)
        assertNotNull(court.getDay1());
        assertNotNull(court.getDay2());
        assertNotNull(court.getDay3());
        assertNotNull(court.getDay4());
        assertNotNull(court.getDay5());
        assertNotNull(court.getDay6());
        assertNotNull(court.getDay7());
        
        assertTrue(court.getDay1().isActive());
        assertEquals("08:00", court.getDay1().getOpeningHour());
        assertEquals("22:00", court.getDay1().getClosingHour());
    }

    @Test
    public void testGetScheduleForDay_AllDays() {
        System.out.println("🧪 Test: Getting schedule for all days");
        // Arrange
        Court court = new Court();
        court.setDay1(new DaySchedule(true, "06:00", "22:00"));
        court.setDay2(new DaySchedule(true, "08:00", "20:00"));
        court.setDay3(new DaySchedule(false, "00:00", "00:00"));
        court.setDay4(new DaySchedule(true, "10:00", "18:00"));
        court.setDay5(new DaySchedule(true, "06:00", "23:00"));
        court.setDay6(new DaySchedule(false, "00:00", "00:00"));
        court.setDay7(new DaySchedule(true, "08:00", "14:00"));
        
        // Act & Assert
        DaySchedule sunday = court.getScheduleForDay(1);
        assertEquals("06:00", sunday.getOpeningHour());
        assertEquals("22:00", sunday.getClosingHour());
        assertTrue(sunday.isActive());
        
        DaySchedule monday = court.getScheduleForDay(2);
        assertEquals("08:00", monday.getOpeningHour());
        assertEquals("20:00", monday.getClosingHour());
        
        DaySchedule tuesday = court.getScheduleForDay(3);
        assertFalse(tuesday.isActive());
        
        DaySchedule wednesday = court.getScheduleForDay(4);
        assertEquals("10:00", wednesday.getOpeningHour());
        assertEquals("18:00", wednesday.getClosingHour());
    }

    @Test
    public void testSetScheduleForDay_UpdatesCorrectDay() {
        System.out.println("🧪 Test: Updating schedule for specific day");
        // Arrange
        Court court = new Court();
        DaySchedule newSchedule = new DaySchedule(true, "07:00", "21:00");
        
        // Act
        court.setScheduleForDay(3, newSchedule); // Tuesday
        
        // Assert
        DaySchedule tuesday = court.getScheduleForDay(3);
        assertEquals("07:00", tuesday.getOpeningHour());
        assertEquals("21:00", tuesday.getClosingHour());
        assertTrue(tuesday.isActive());
    }

    @Test
    public void testIsActiveOnDay_ActiveDay() {
        System.out.println("🧪 Test: Active day");
        // Arrange
        Court court = new Court();
        court.setDay1(new DaySchedule(true, "08:00", "20:00"));
        
        // Act & Assert
        assertTrue(court.isActiveOnDay(1)); // Sunday active
    }

    @Test
    public void testIsActiveOnDay_InactiveDay() {
        System.out.println("🧪 Test: Inactive day");
        // Arrange
        Court court = new Court();
        court.setDay6(new DaySchedule(false, "00:00", "00:00")); // Friday closed
        
        // Act & Assert
        assertFalse(court.isActiveOnDay(6)); // Friday inactive
    }

    @Test
    public void testWeeklySchedule_GetAndSet() {
        System.out.println("🧪 Test: Weekly schedule - Get & Set");
        // Arrange
        Court court = new Court();
        DaySchedule customSchedule1 = new DaySchedule(true, "06:00", "18:00");
        DaySchedule customSchedule2 = new DaySchedule(false, "00:00", "00:00");
        
        // Act
        court.setScheduleForDay(1, customSchedule1);
        court.setScheduleForDay(2, customSchedule2);
        
        Map<String, DaySchedule> weeklySchedule = court.getWeeklySchedule();
        
        // Assert
        assertNotNull(weeklySchedule);
        assertEquals(7, weeklySchedule.size());
        
        DaySchedule day1 = weeklySchedule.get("1");
        assertEquals("06:00", day1.getOpeningHour());
        assertEquals("18:00", day1.getClosingHour());
        assertTrue(day1.isActive());
        
        DaySchedule day2 = weeklySchedule.get("2");
        assertFalse(day2.isActive());
    }

    @Test
    public void testSetWeeklySchedule_FromMap() {
        System.out.println("🧪 Test: Updating weekly schedule from Map");
        // Arrange
        Court court = new Court();
        Map<String, DaySchedule> customSchedule = new java.util.HashMap<>();
        customSchedule.put("1", new DaySchedule(true, "07:00", "19:00"));
        customSchedule.put("2", new DaySchedule(true, "08:00", "20:00"));
        customSchedule.put("3", new DaySchedule(false, "00:00", "00:00"));
        customSchedule.put("4", new DaySchedule(true, "09:00", "21:00"));
        customSchedule.put("5", new DaySchedule(true, "06:00", "22:00"));
        customSchedule.put("6", new DaySchedule(false, "00:00", "00:00"));
        customSchedule.put("7", new DaySchedule(true, "10:00", "16:00"));
        
        // Act
        court.setWeeklySchedule(customSchedule);
        
        // Assert
        assertEquals("07:00", court.getDay1().getOpeningHour());
        assertEquals("19:00", court.getDay1().getClosingHour());
        assertFalse(court.getDay3().isActive());
        assertEquals("21:00", court.getDay4().getClosingHour());
    }

    @Test
    public void testCourtAvailability_Toggle() {
        System.out.println("🧪 Test: Toggling court availability");
        // Arrange
        Court court = new Court();
        
        // Act & Assert - Set to available
        court.setAvailable(true);
        assertTrue(court.isAvailable());
        
        // Make unavailable
        court.setAvailable(false);
        assertFalse(court.isAvailable());
        
        // Make available again
        court.setAvailable(true);
        assertTrue(court.isAvailable());
    }

    @Test
    public void testCourtWithCustomActiveDays() {
        System.out.println("🧪 Test: Court with custom active days");
        // Act
        Court court = new Court(
            "court456",
            "מגרש צל",
            "פארק הירקון",
            "08:00",
            "20:00",
            "1,2,3,4,5" // Only weekdays
        );
        
        // Assert
        assertEquals("1,2,3,4,5", court.getActiveDays());
    }

    @Test
    public void testCourtSetters_UpdateAllFields() {
        System.out.println("🧪 Test: Updating all fields");
        // Arrange
        Court court = new Court();
        
        // Act
        court.setCourtId("court999");
        court.setName("מגרש חדש");
        court.setLocation("תל אביב");
        court.setCourtType("Indoor hall");
        court.setAvailable(true);
        court.setOpeningHour("07:00");
        court.setClosingHour("22:00");
        court.setActiveDays("1,3,5");
        court.setCreatedAt(1000000L);
        
        // Assert
        assertEquals("court999", court.getCourtId());
        assertEquals("מגרש חדש", court.getName());
        assertEquals("תל אביב", court.getLocation());
        assertEquals("Indoor hall", court.getCourtType());
        assertTrue(court.isAvailable());
        assertEquals("07:00", court.getOpeningHour());
        assertEquals("22:00", court.getClosingHour());
        assertEquals("1,3,5", court.getActiveDays());
        assertEquals(1000000L, court.getCreatedAt());
    }

    @Test
    public void testCourtToString_ReturnsName() {
        System.out.println("🧪 Test: toString - returning name");
        // Arrange
        Court court = new Court();
        court.setName("מגרש אולימפי");
        
        // Act
        String result = court.toString();
        
        // Assert
        assertEquals("מגרש אולימפי", result);
    }

    @Test
    public void testScheduleForInvalidDay_ReturnsDefault() {
        System.out.println("🧪 Test: Schedule for invalid day");
        // Arrange
        Court court = new Court();
        
        // Act
        DaySchedule invalid = court.getScheduleForDay(0); // Invalid day
        DaySchedule invalid2 = court.getScheduleForDay(8); // Invalid day
        
        // Assert - Should return default schedule
        assertNotNull(invalid);
        assertEquals("08:00", invalid.getOpeningHour());
        assertEquals("22:00", invalid.getClosingHour());
        assertTrue(invalid.isActive());
        
        assertNotNull(invalid2);
        assertEquals("08:00", invalid2.getOpeningHour());
    }

    @Test
    public void testCourtNullSchedule_HandledGracefully() {
        System.out.println("🧪 Test: Handling null schedule");
        // Arrange
        Court court = new Court();
        court.setDay1(null); // Simulate null schedule
        
        // Act
        DaySchedule schedule = court.getScheduleForDay(1);
        
        // Assert - Should return default when null
        assertNotNull(schedule);
        assertEquals("08:00", schedule.getOpeningHour());
        assertEquals("22:00", schedule.getClosingHour());
    }
}
