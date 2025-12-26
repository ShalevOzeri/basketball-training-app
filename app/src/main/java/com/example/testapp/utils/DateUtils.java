package com.example.testapp.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {
    
    private static final TimeZone ISRAEL_TIMEZONE = TimeZone.getTimeZone("Asia/Jerusalem");
    private static final Locale HEBREW_LOCALE = new Locale("he", "IL");
    
    public static String getDayOfWeek(long timestamp) {
        Calendar calendar = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        calendar.setTimeInMillis(timestamp);
        
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.SUNDAY: return "ראשון";
            case Calendar.MONDAY: return "שני";
            case Calendar.TUESDAY: return "שלישי";
            case Calendar.WEDNESDAY: return "רביעי";
            case Calendar.THURSDAY: return "חמישי";
            case Calendar.FRIDAY: return "שישי";
            case Calendar.SATURDAY: return "שבת";
            default: return "";
        }
    }
    
    public static int getDayOfWeekNumber(long timestamp) {
        Calendar calendar = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        calendar.setTimeInMillis(timestamp);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }
    
    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", HEBREW_LOCALE);
        sdf.setTimeZone(ISRAEL_TIMEZONE);
        return sdf.format(new Date(timestamp));
    }
    
    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", HEBREW_LOCALE);
        sdf.setTimeZone(ISRAEL_TIMEZONE);
        return sdf.format(new Date(timestamp));
    }
    
    public static String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", HEBREW_LOCALE);
        sdf.setTimeZone(ISRAEL_TIMEZONE);
        return sdf.format(new Date(timestamp));
    }
    
    public static long getCurrentIsraeliTime() {
        Calendar calendar = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        return calendar.getTimeInMillis();
    }
    
    public static boolean isToday(long timestamp) {
        Calendar cal1 = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        Calendar cal2 = Calendar.getInstance(ISRAEL_TIMEZONE, HEBREW_LOCALE);
        cal2.setTimeInMillis(timestamp);
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
