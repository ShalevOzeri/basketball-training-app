package com.example.testapp.models;

import com.example.testapp.models.DaySchedule;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit Tests for DaySchedule model
 * Tests schedule creation, active/inactive states, and display logic
 */
public class DayScheduleTest {

    @Test
    public void testCreateDaySchedule_Active() {
        System.out.println("🧪 Test: Creating active day schedule");
        // Act
        DaySchedule schedule = new DaySchedule(
            true,
            "08:00",
            "22:00"
        );
        
        // Assert
        assertTrue(schedule.isActive());
        assertEquals("08:00", schedule.getOpeningHour());
        assertEquals("22:00", schedule.getClosingHour());
    }

    @Test
    public void testCreateDaySchedule_Inactive() {
        System.out.println("🧪 Test: Creating inactive day schedule");
        // Act
        DaySchedule schedule = new DaySchedule(
            false,
            "00:00",
            "00:00"
        );
        
        // Assert
        assertFalse(schedule.isActive());
        assertEquals("00:00", schedule.getOpeningHour());
        assertEquals("00:00", schedule.getClosingHour());
    }

    @Test
    public void testDefaultConstructor() {
        System.out.println("🧪 Test: Default constructor");
        // Act
        DaySchedule schedule = new DaySchedule();
        
        // Assert
        assertFalse(schedule.isActive()); // Default is inactive
        assertEquals("08:00", schedule.getOpeningHour());
        assertEquals("22:00", schedule.getClosingHour());
    }

    @Test
    public void testToString_Active() {
        System.out.println("🧪 Test: toString - active day");
        // Arrange
        DaySchedule schedule = new DaySchedule(
            true,
            "06:00",
            "23:00"
        );
        
        // Act
        String result = schedule.toString();
        
        // Assert
        assertEquals("06:00 - 23:00", result);
    }

    @Test
    public void testToString_Inactive() {
        System.out.println("🧪 Test: toString - inactive day");
        // Arrange
        DaySchedule schedule = new DaySchedule(
            false,
            "08:00",
            "20:00"
        );
        
        // Act
        String result = schedule.toString();
        
        // Assert
        assertEquals("סגור", result); // "Closed" in Hebrew
    }

    @Test
    public void testSetActive_ToggleState() {
        System.out.println("🧪 Test: Toggling active status");
        // Arrange
        DaySchedule schedule = new DaySchedule(
            true,
            "09:00",
            "21:00"
        );
        
        // Act
        schedule.setActive(false);
        
        // Assert
        assertFalse(schedule.isActive());
        
        // Toggle back
        schedule.setActive(true);
        assertTrue(schedule.isActive());
    }

    @Test
    public void testSetOpeningHour() {
        System.out.println("🧪 Test: Updating opening hour");
        // Arrange
        DaySchedule schedule = new DaySchedule();
        
        // Act
        schedule.setOpeningHour("07:00");
        
        // Assert
        assertEquals("07:00", schedule.getOpeningHour());
    }

    @Test
    public void testSetClosingHour() {
        System.out.println("🧪 Test: Updating closing hour");
        // Arrange
        DaySchedule schedule = new DaySchedule();
        
        // Act
        schedule.setClosingHour("23:30");
        
        // Assert
        assertEquals("23:30", schedule.getClosingHour());
    }

    @Test
    public void testUpdateScheduleHours() {
        System.out.println("🧪 Test: Updating schedule hours");
        // Arrange
        DaySchedule schedule = new DaySchedule(
            true,
            "08:00",
            "20:00"
        );
        
        // Act - Extend hours
        schedule.setOpeningHour("06:00");
        schedule.setClosingHour("22:00");
        
        // Assert
        assertEquals("06:00", schedule.getOpeningHour());
        assertEquals("22:00", schedule.getClosingHour());
        assertTrue(schedule.isActive());
    }

    @Test
    public void testScheduleDifferentTimeFormats() {
        System.out.println("🧪 Test: Different time formats");
        // Act
        DaySchedule earlyMorning = new DaySchedule(true, "06:00", "12:00");
        DaySchedule afternoon = new DaySchedule(true, "12:00", "18:00");
        DaySchedule evening = new DaySchedule(true, "18:00", "23:59");
        
        // Assert
        assertEquals("06:00", earlyMorning.getOpeningHour());
        assertEquals("12:00", earlyMorning.getClosingHour());
        
        assertEquals("12:00", afternoon.getOpeningHour());
        assertEquals("18:00", afternoon.getClosingHour());
        
        assertEquals("18:00", evening.getOpeningHour());
        assertEquals("23:59", evening.getClosingHour());
    }

    @Test
    public void testClosedDay_ToString() {
        System.out.println("🧪 Test: toString - closed day");
        // Arrange
        DaySchedule closedDay1 = new DaySchedule(false, "00:00", "00:00");
        DaySchedule closedDay2 = new DaySchedule(false, "08:00", "20:00"); // Hours don't matter when closed
        
        // Act
        String result1 = closedDay1.toString();
        String result2 = closedDay2.toString();
        
        // Assert
        assertEquals("סגור", result1);
        assertEquals("סגור", result2); // Always shows "סגור" when inactive
    }

    @Test
    public void testFullDaySchedule_24Hours() {
        System.out.println("🧪 Test: 24-hour schedule");
        // Act
        DaySchedule fullDay = new DaySchedule(true, "00:00", "23:59");
        
        // Assert
        assertTrue(fullDay.isActive());
        assertEquals("00:00", fullDay.getOpeningHour());
        assertEquals("23:59", fullDay.getClosingHour());
        assertEquals("00:00 - 23:59", fullDay.toString());
    }

    @Test
    public void testMidnightCrossing_NotSupported() {
        System.out.println("🧪 Test: Midnight crossing - not supported");
        // Note: DaySchedule doesn't validate time logic (e.g., 22:00 - 02:00)
        // It only stores the times as strings
        
        // Act
        DaySchedule schedule = new DaySchedule(true, "22:00", "02:00");
        
        // Assert - Stores values as-is without validation
        assertEquals("22:00", schedule.getOpeningHour());
        assertEquals("02:00", schedule.getClosingHour());
        assertEquals("22:00 - 02:00", schedule.toString());
    }

    @Test
    public void testSetAllFields() {
        System.out.println("🧪 Test: Updating all fields");
        // Arrange
        DaySchedule schedule = new DaySchedule();
        
        // Act
        schedule.setActive(true);
        schedule.setOpeningHour("10:00");
        schedule.setClosingHour("18:00");
        
        // Assert
        assertTrue(schedule.isActive());
        assertEquals("10:00", schedule.getOpeningHour());
        assertEquals("18:00", schedule.getClosingHour());
    }

    @Test
    public void testTypicalWorkdaySchedule() {
        System.out.println("🧪 Test: Typical workday schedule");
        // Arrange
        DaySchedule workday = new DaySchedule(
            true,
            "08:00",
            "20:00"
        );
        
        // Assert
        assertTrue(workday.isActive());
        assertEquals("08:00", workday.getOpeningHour());
        assertEquals("20:00", workday.getClosingHour());
        assertEquals("08:00 - 20:00", workday.toString());
    }

    @Test
    public void testTypicalWeekendSchedule() {
        System.out.println("🧪 Test: Typical weekend schedule");
        // Arrange
        DaySchedule weekend = new DaySchedule(
            true,
            "09:00",
            "14:00"
        );
        
        // Assert
        assertTrue(weekend.isActive());
        assertEquals("09:00", weekend.getOpeningHour());
        assertEquals("14:00", weekend.getClosingHour());
        assertEquals("09:00 - 14:00", weekend.toString());
    }

    @Test
    public void testInactiveDayRetainHours() {
        System.out.println("🧪 Test: Preserving hours on inactive day");
        // Arrange - Even when inactive, hours are stored
        DaySchedule schedule = new DaySchedule(
            false,
            "10:00",
            "16:00"
        );
        
        // Assert
        assertFalse(schedule.isActive());
        assertEquals("10:00", schedule.getOpeningHour());
        assertEquals("16:00", schedule.getClosingHour());
        assertEquals("סגור", schedule.toString()); // Shows closed despite having hours
    }

    @Test
    public void testReactivateDay_KeepsHours() {
        System.out.println("🧪 Test: Reactivation - preserving hours");
        // Arrange
        DaySchedule schedule = new DaySchedule(
            false,
            "07:00",
            "21:00"
        );
        
        // Act - Reactivate
        schedule.setActive(true);
        
        // Assert - Hours are preserved
        assertTrue(schedule.isActive());
        assertEquals("07:00", schedule.getOpeningHour());
        assertEquals("21:00", schedule.getClosingHour());
        assertEquals("07:00 - 21:00", schedule.toString());
    }
}
