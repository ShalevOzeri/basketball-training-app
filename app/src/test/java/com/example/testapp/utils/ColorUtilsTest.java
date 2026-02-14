package com.example.testapp.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for ColorUtils
 * 
 * Tests the functions that handle colors:
 * - Get random color
 * - Get color by index
 * 
 * Note: Tests that require android.graphics.Color (parseColor, isColorLight) 
 * are omitted because they require Robolectric or Android Instrumentation Tests.
 * These tests can be moved to androidTest/ if needed.
 */
public class ColorUtilsTest {

    @Test
    public void getRandomColor_ReturnsValidColorString() {
        System.out.println("🧪 Testing random color - valid value");
        // Test: Random color returns valid string
        String color = ColorUtils.getRandomColor();
        
        assertNotNull("Random color should not be null", color);
        assertTrue("Color should start with #", color.startsWith("#"));
        assertEquals("Color should be 7 characters long (#RRGGBB)", 7, color.length());
    }

    @Test
    public void getRandomColor_ReturnsValidHexFormat() {
        System.out.println("🧪 Testing random color - Hex format");
        // Test: Random color is valid hex (format check only)
        String color = ColorUtils.getRandomColor();
        
        // Test: Format is valid: # + 6 hex characters
        assertTrue("Should start with #", color.startsWith("#"));
        String hexPart = color.substring(1);
        assertEquals("Should have 6 hex characters", 6, hexPart.length());
        assertTrue("Should contain only hex characters", hexPart.matches("[0-9A-Fa-f]{6}"));
    }

    @Test
    public void getColorForIndex_Zero_ReturnsFirstColor() {
        System.out.println("🧪 Testing color by index 0");
        // Test: Index 0 returns first color
        String color = ColorUtils.getColorForIndex(0);
        
        assertNotNull("Color should not be null", color);
        assertEquals("Index 0 should return #F44336 (Red)", "#F44336", color);
    }

    @Test
    public void getColorForIndex_One_ReturnsSecondColor() {
        System.out.println("🧪 Testing color by index 1");
        // Test: Index 1 returns second color
        String color = ColorUtils.getColorForIndex(1);
        
        assertNotNull("Color should not be null", color);
        assertEquals("Index 1 should return #E91E63 (Pink)", "#E91E63", color);
    }

    @Test
    public void getColorForIndex_HighNumber_WrapsAround() {
        System.out.println("🧪 Testing wrap around - large number");
        // Test: Large number wraps back to start (modulo)
        String color0 = ColorUtils.getColorForIndex(0);
        String color16 = ColorUtils.getColorForIndex(16); // There are 16 colors, so 16 should be same as 0
        
        assertEquals("Index 16 should wrap around to index 0", color0, color16);
    }

    @Test
    public void getColorForIndex_MultipleWraps_WorksCorrectly() {
        System.out.println("🧪 Testing multiple wraps");
        // Test: Multiple wraps around the list
        String color2 = ColorUtils.getColorForIndex(2);
        String color18 = ColorUtils.getColorForIndex(18); // 18 % 16 = 2
        
        assertEquals("Index 18 should wrap to index 2", color2, color18);
    }

    @Test
    public void getColorForIndex_ConsecutiveIndexes_ReturnDifferentColors() {
        System.out.println("🧪 Testing consecutive indexes - different colors");
        // Test: Consecutive indexes return different colors
        String color0 = ColorUtils.getColorForIndex(0);
        String color1 = ColorUtils.getColorForIndex(1);
        String color2 = ColorUtils.getColorForIndex(2);
        
        assertNotEquals("Consecutive indexes should have different colors", color0, color1);
        assertNotEquals("Consecutive indexes should have different colors", color1, color2);
        assertNotEquals("Consecutive indexes should have different colors", color0, color2);
    }

    @Test
    public void getColorForIndex_AllIndexes_ReturnValidHexFormat() {
        System.out.println("🧪 Testing 16 colors - valid Hex format");
        // Test: All 16 colors are in valid hex format
        for (int i = 0; i < 16; i++) {
            String color = ColorUtils.getColorForIndex(i);
            
            assertNotNull("Color at index " + i + " should not be null", color);
            assertTrue("Color at index " + i + " should start with #", color.startsWith("#"));
            assertEquals("Color at index " + i + " should be 7 chars", 7, color.length());
            
            String hexPart = color.substring(1);
            assertTrue("Color at index " + i + " should be valid hex", 
                      hexPart.matches("[0-9A-Fa-f]{6}"));
        }
    }

    // The next test is omitted because ColorUtils doesn't handle negative indexes
    // If you want to support negative indexes, ColorUtils needs to be updated
    /*
    @Test
    public void getColorForIndex_NegativeIndex_WorksWithModulo() {
        // Test: Negative index works with modulo
        // Java modulo of negative number can be negative, but the function should handle it
        String color = ColorUtils.getColorForIndex(-1);
        
        assertNotNull("Color should not be null for negative index", color);
        assertTrue("Should return a valid color", color.startsWith("#"));
    }
    */

    @Test
    public void getColorForIndex_LargeIndex_StillWorks() {
        System.out.println("🧪 Testing very large index");
        // Test: Very large index works
        String color = ColorUtils.getColorForIndex(1000);
        
        assertNotNull("Color should not be null for large index", color);
        assertTrue("Should return a valid color", color.startsWith("#"));
        
        // 1000 % 16 = 8
        String color8 = ColorUtils.getColorForIndex(8);
        assertEquals("Large index should wrap correctly", color8, color);
    }

    @Test
    public void teamColors_Count_Is16() {
        System.out.println("🧪 Testing color count - 16 colors");
        // Test: Exactly 16 different colors
        for (int i = 0; i < 16; i++) {
            String color = ColorUtils.getColorForIndex(i);
            assertNotNull("Color " + i + " should exist", color);
        }
        
        // Test that index 16 wraps back to 0
        assertEquals("Should have 16 colors that wrap around", 
                    ColorUtils.getColorForIndex(0), 
                    ColorUtils.getColorForIndex(16));
    }

    @Test
    public void teamColors_AllUnique_WithinArray() {
        System.out.println("🧪 Testing unique colors - no duplicates");
        // Test: All 16 colors are different from each other
        String[] colors = new String[16];
        for (int i = 0; i < 16; i++) {
            colors[i] = ColorUtils.getColorForIndex(i);
        }
        
        // Test that there are no duplicates
        for (int i = 0; i < 16; i++) {
            for (int j = i + 1; j < 16; j++) {
                assertNotEquals("Colors at index " + i + " and " + j + " should be different", 
                              colors[i], colors[j]);
            }
        }
    }

    /* 
     * The following tests are omitted because they require android.graphics.Color
     * which is only available in instrumented tests (androidTest/) and not in unit tests (test/)
     * 
     * If you want to test these functions, you will need to:
     * 1. Move the tests to androidTest/java/com/example/testapp/utils/
     * 2. Or add dependency on Robolectric (Android mock library)
     * 
     * Functions not tested here:
     * - parseColor(String, int) - requires Color.parseColor()
     * - isColorLight(String) - requires Color.parseColor(), Color.red(), Color.green(), Color.blue()
     */
}
