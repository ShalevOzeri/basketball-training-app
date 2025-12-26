package com.example.testapp.utils;

import android.graphics.Color;

public class ColorUtils {
    
    private static final String[] TEAM_COLORS = {
        "#F44336", // Red
        "#E91E63", // Pink
        "#9C27B0", // Purple
        "#673AB7", // Deep Purple
        "#3F51B5", // Indigo
        "#2196F3", // Blue
        "#03A9F4", // Light Blue
        "#00BCD4", // Cyan
        "#009688", // Teal
        "#4CAF50", // Green
        "#8BC34A", // Light Green
        "#CDDC39", // Lime
        "#FFEB3B", // Yellow
        "#FFC107", // Amber
        "#FF9800", // Orange
        "#FF5722", // Deep Orange
    };
    
    public static String getRandomColor() {
        int randomIndex = (int) (Math.random() * TEAM_COLORS.length);
        return TEAM_COLORS[randomIndex];
    }
    
    public static String getColorForIndex(int index) {
        return TEAM_COLORS[index % TEAM_COLORS.length];
    }
    
    public static int parseColor(String colorString, int defaultColor) {
        try {
            return Color.parseColor(colorString);
        } catch (Exception e) {
            return defaultColor;
        }
    }
    
    public static boolean isColorLight(String colorString) {
        try {
            int color = Color.parseColor(colorString);
            double darkness = 1 - (0.299 * Color.red(color) + 
                                 0.587 * Color.green(color) + 
                                 0.114 * Color.blue(color)) / 255;
            return darkness < 0.5;
        } catch (Exception e) {
            return true;
        }
    }
}
