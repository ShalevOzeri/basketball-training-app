package com.example.testapp.utils;

import org.junit.Test;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Unit tests for DateUtils
 * 
 * Tests the functions that handle dates and times:
 * - Date format in Hebrew
 * - Time format
 * - Conversion to day of week
 * - Check if date is today
 */
public class DateUtilsTest {

    private static final TimeZone ISRAEL_TIMEZONE = TimeZone.getTimeZone("Asia/Jerusalem");
    private static final Locale HEBREW_LOCALE = new Locale("he", "IL");

    @Test
    public void getDayOfWeek_Sunday_ReturnsRishon() {
        System.out.println("🧪 Testing day of week - Sunday");
        // Create date for Sunday
        Calendar calendar = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        calendar.set(2024, Calendar.JANUARY, 7); // January 7, 2024 is Sunday
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        long timestamp = calendar.getTimeInMillis();
        String dayOfWeek = DateUtils.getDayOfWeek(timestamp);
        
        assertEquals("ראשון", dayOfWeek);
    }

    @Test
    public void getDayOfWeek_Monday_ReturnsShnei() {
        System.out.println("🧪 Testing day of week - Monday");
        Calendar calendar = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        calendar.set(2024, Calendar.JANUARY, 8); // January 8, 2024 is Monday
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        long timestamp = calendar.getTimeInMillis();
        String dayOfWeek = DateUtils.getDayOfWeek(timestamp);
        
        assertEquals("שני", dayOfWeek);
    }

    @Test
    public void getDayOfWeek_Friday_ReturnsShishi() {
        System.out.println("🧪 Testing day of week - Friday");
        Calendar calendar = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        calendar.set(2024, Calendar.JANUARY, 12); // January 12, 2024 is Friday
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        long timestamp = calendar.getTimeInMillis();
        String dayOfWeek = DateUtils.getDayOfWeek(timestamp);
        
        assertEquals("שישי", dayOfWeek);
    }

    @Test
    public void getDayOfWeekNumber_Sunday_Returns1() {
        System.out.println("🧪 Testing day number - Sunday");
        Calendar calendar = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        calendar.set(2024, Calendar.JANUARY, 7); // Sunday
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        long timestamp = calendar.getTimeInMillis();
        int dayNumber = DateUtils.getDayOfWeekNumber(timestamp);
        
        assertEquals(Calendar.SUNDAY, dayNumber);
    }

    @Test
    public void formatTime_Noon_Returns1200() {
        System.out.println("🧪 Testing time format - noon");
        Calendar calendar = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        calendar.set(2024, Calendar.JANUARY, 1, 12, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        long timestamp = calendar.getTimeInMillis();
        String formattedTime = DateUtils.formatTime(timestamp);
        
        assertEquals("12:00", formattedTime);
    }

    @Test
    public void formatTime_Evening_Returns1830() {
        System.out.println("🧪 Testing time format - evening");
        Calendar calendar = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        calendar.set(2024, Calendar.JANUARY, 1, 18, 30, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        long timestamp = calendar.getTimeInMillis();
        String formattedTime = DateUtils.formatTime(timestamp);
        
        assertEquals("18:30", formattedTime);
    }

    @Test
    public void formatDate_FirstOfJanuary_ReturnsCorrectFormat() {
        System.out.println("🧪 Testing date format - January 1st");
        Calendar calendar = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        calendar.set(2024, Calendar.JANUARY, 1, 12, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        long timestamp = calendar.getTimeInMillis();
        String formattedDate = DateUtils.formatDate(timestamp);
        
        assertEquals("01/01/2024", formattedDate);
    }

    @Test
    public void formatDate_LastOfYear_ReturnsCorrectFormat() {
        System.out.println("🧪 Testing date format - end of year");
        Calendar calendar = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        calendar.set(2024, Calendar.DECEMBER, 31, 12, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        long timestamp = calendar.getTimeInMillis();
        String formattedDate = DateUtils.formatDate(timestamp);
        
        assertEquals("31/12/2024", formattedDate);
    }

    @Test
    public void formatDateTime_ReturnsCorrectFormat() {
        System.out.println("🧪 Testing date and time format");
        Calendar calendar = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        calendar.set(2024, Calendar.JANUARY, 15, 14, 30, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        
        long timestamp = calendar.getTimeInMillis();
        String formattedDateTime = DateUtils.formatDateTime(timestamp);
        
        assertEquals("15/01/2024 14:30", formattedDateTime);
    }

    @Test
    public void getCurrentIsraeliTime_ReturnsNonZero() {
        System.out.println("🧪 Testing current Israeli time");
        long currentTime = DateUtils.getCurrentIsraeliTime();
        
        // Check that current time is not zero and not very small
        assertTrue("Current time should be greater than 0", currentTime > 0);
        
        // Check that current time is reasonable (after 2020)
        Calendar cal2020 = Calendar.getInstance();
        cal2020.set(2020, Calendar.JANUARY, 1);
        assertTrue("Current time should be after 2020", currentTime > cal2020.getTimeInMillis());
    }

    @Test
    public void isToday_CurrentTime_ReturnsTrue() {
        System.out.println("🧪 Testing is today - current time");
        long currentTime = System.currentTimeMillis();
        
        boolean isToday = DateUtils.isToday(currentTime);
        
        assertTrue("Current time should be considered today", isToday);
    }

    @Test
    public void isToday_Yesterday_ReturnsFalse() {
        System.out.println("🧪 Testing is today - yesterday");
        Calendar yesterday = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        yesterday.set(Calendar.HOUR_OF_DAY, 12);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);
        yesterday.set(Calendar.MILLISECOND, 0);
        
        long timestamp = yesterday.getTimeInMillis();
        boolean isToday = DateUtils.isToday(timestamp);
        
        assertFalse("Yesterday should not be considered today", isToday);
    }

    @Test
    public void isToday_Tomorrow_ReturnsFalse() {
        System.out.println("🧪 Testing is today - tomorrow");
        Calendar tomorrow = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        tomorrow.set(Calendar.HOUR_OF_DAY, 12);
        tomorrow.set(Calendar.MINUTE, 0);
        tomorrow.set(Calendar.SECOND, 0);
        tomorrow.set(Calendar.MILLISECOND, 0);
        
        long timestamp = tomorrow.getTimeInMillis();
        boolean isToday = DateUtils.isToday(timestamp);
        
        assertFalse("Tomorrow should not be considered today", isToday);
    }

    @Test
    public void isToday_SameDayDifferentHour_ReturnsTrue() {
        System.out.println("🧪 Testing is today - same day different hours");
        Calendar now = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        Calendar midnight = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        midnight.set(Calendar.HOUR_OF_DAY, 0);
        midnight.set(Calendar.MINUTE, 0);
        midnight.set(Calendar.SECOND, 0);
        midnight.set(Calendar.MILLISECOND, 0);
        
        Calendar evening = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        evening.set(Calendar.HOUR_OF_DAY, 23);
        evening.set(Calendar.MINUTE, 59);
        evening.set(Calendar.SECOND, 59);
        
        boolean midnightIsToday = DateUtils.isToday(midnight.getTimeInMillis());
        boolean eveningIsToday = DateUtils.isToday(evening.getTimeInMillis());
        
        assertTrue("Midnight today should be considered today", midnightIsToday);
        assertTrue("Evening today should be considered today", eveningIsToday);
    }
}
