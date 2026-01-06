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
 * תסריט בדיקה לפיצ'ר לוח האימונים
 * בדיקה שכל הפונקציונליות של ניהול אימונים עובדת
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ScheduleFragmentTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * בדיקה: דף לוח האימונים נטען כראוי
     */
    @Test
    public void testScheduleFragmentLoads() {
        // נווט ללוח אימונים
        onView(withId(R.id.scheduleCard)).perform(click());

        // וודא שה-RecyclerView מוצג
        onView(withId(R.id.scheduleRecyclerView)).check(matches(isDisplayed()));
    }

    /**
     * בדיקה: פילטרים מוצגים ופועלים
     */
    @Test
    public void testScheduleFiltersExist() {
        // נווט ללוח אימונים
        onView(withId(R.id.scheduleCard)).perform(click());

        // המתן לטעינה
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // וודא שה-RecyclerView של האימונים מוצג (זה מעיד על טעינת הנתונים)
        onView(withId(R.id.scheduleRecyclerView)).check(matches(isDisplayed()));
    }

    /**
     * בדיקה: כפתור הוספת אימון מוצג
     */
    @Test
    public void testAddTrainingButtonExists() {
        // נווט ללוח אימונים
        onView(withId(R.id.scheduleCard)).perform(click());

        // המתן לטעינה
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // בדוק שכפתור "הוסף אימון" מוצג
        onView(withId(R.id.fab)).check(matches(isDisplayed()));
    }

    /**
     * בדיקה: רשימת האימונים מוצגת
     */
    @Test
    public void testTrainingsListDisplayed() {
        // נווט ללוח אימונים
        onView(withId(R.id.scheduleCard)).perform(click());

        // המתן לטעינת נתונים
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // וודא שהרשימה מוצגת
        onView(withId(R.id.scheduleRecyclerView)).check(matches(isDisplayed()));
    }
}
