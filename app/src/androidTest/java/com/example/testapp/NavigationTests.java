package com.example.testapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * תסריט בדיקה לניווט בין המסכים
 * בדיקה שכל הכפתורים במסך הבית עובדים ומנווטים למסך הנכון
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class NavigationTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * בדיקה: לחיצה על כרטיס "קבוצות" מנווטת לדף הקבוצות
     */
    @Test
    public void testNavigateToTeams() {
        // לחץ על כרטיס הקבוצות
        onView(withId(R.id.teamsCard)).perform(click());

        // וודא שהגענו לדף הקבוצות (בדוק שה-RecyclerView מוצג)
        onView(withId(R.id.teamsRecyclerView)).check(matches(isDisplayed()));
    }

    /**
     * בדיקה: לחיצה על כרטיס "מגרשים" מנווטת לדף המגרשים
     */
    @Test
    public void testNavigateToCourts() {
        // לחץ על כרטיס המגרשים
        onView(withId(R.id.courtsCard)).perform(click());

        // וודא שהגענו לדף המגרשים
        onView(withId(R.id.courtsRecyclerView)).check(matches(isDisplayed()));
    }

    /**
     * בדיקה: לחיצה על כרטיס "לוח אימונים" מנווטת לדף האימונים
     */
    @Test
    public void testNavigateToSchedule() {
        // לחץ על כרטיס לוח האימונים
        onView(withId(R.id.scheduleCard)).perform(click());

        // וודא שהגענו לדף האימונים
        onView(withId(R.id.scheduleRecyclerView)).check(matches(isDisplayed()));
    }

    /**
     * בדיקה: לחיצה על כרטיס "תצוגת מגרשים" מנווטת לתצוגה החזותית
     */
    @Test
    public void testNavigateToAllCourtsView() {
        // לחץ על כרטיס תצוגת מגרשים
        onView(withId(R.id.allCourtsCard)).perform(click());

        // וודא שהגענו לתצוגת מגרשים (בדוק את ה-ScrollView)
        onView(withId(R.id.scrollView)).check(matches(isDisplayed()));
    }

    /**
     * בדיקה: ניווט חזרה מדף קבוצות למסך הבית
     */
    @Test
    public void testNavigateBackFromTeams() {
        // נווט לקבוצות
        onView(withId(R.id.teamsCard)).perform(click());

        // לחץ על כפתור חזרה
        androidx.test.espresso.Espresso.pressBack();

        // וודא שחזרנו למסך הבית
        onView(withId(R.id.allCourtsCard)).check(matches(isDisplayed()));
    }

    /**
     * בדיקה: ניווט בין מספר מסכים ברצף
     */
    @Test
    public void testNavigateMultipleScreens() {
        // נווט לקבוצות
        onView(withId(R.id.teamsCard)).perform(click());
        onView(withId(R.id.teamsRecyclerView)).check(matches(isDisplayed()));

        // חזור למסך הבית
        androidx.test.espresso.Espresso.pressBack();

        // נווט למגרשים
        onView(withId(R.id.courtsCard)).perform(click());
        onView(withId(R.id.courtsRecyclerView)).check(matches(isDisplayed()));

        // חזור למסך הבית
        androidx.test.espresso.Espresso.pressBack();

        // נווט ללוח אימונים
        onView(withId(R.id.scheduleCard)).perform(click());
        onView(withId(R.id.scheduleRecyclerView)).check(matches(isDisplayed()));
    }
}
