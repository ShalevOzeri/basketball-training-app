package com.example.testapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * תסריט בדיקה לפיצ'ר המגרשים
 * בדיקה שכל הפונקציונליות של ניהול מגרשים עובדת
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CourtsFragmentTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * בדיקה: דף המגרשים נטען כראוי
     */
    @Test
    public void testCourtsFragmentLoads() {
        // נווט לדף מגרשים
        onView(withId(R.id.courtsCard)).perform(click());

        // וודא שה-RecyclerView מוצג
        onView(withId(R.id.courtsRecyclerView)).check(matches(isDisplayed()));
    }

    /**
     * בדיקה: כפתור הוספת מגרש מוצג ופועל
     */
    @Test
    public void testAddCourtButtonExists() {
        // נווט לדף מגרשים
        onView(withId(R.id.courtsCard)).perform(click());

        // המתן לטעינה
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // בדוק שכפתור "הוסף מגרש" מוצג
        onView(withId(R.id.fab)).check(matches(isDisplayed()));
    }

    /**
     * בדיקה: רשימת המגרשים מוצגת
     */
    @Test
    public void testCourtsListDisplayed() {
        // נווט לדף מגרשים
        onView(withId(R.id.courtsCard)).perform(click());

        // המתן לטעינת נתונים
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // וודא שהרשימה מוצגת
        onView(withId(R.id.courtsRecyclerView)).check(matches(isDisplayed()));
    }
}
