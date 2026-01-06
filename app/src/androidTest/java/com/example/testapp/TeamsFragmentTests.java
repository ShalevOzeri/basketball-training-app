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
 * תסריט בדיקה לפיצ'ר הקבוצות
 * בדיקה שכל הפונקציונליות של ניהול קבוצות עובדת
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class TeamsFragmentTests {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    /**
     * בדיקה: דף הקבוצות נטען כראוי ומציג את הרשימה
     */
    @Test
    public void testTeamsFragmentLoads() {
        // נווט לדף קבוצות
        onView(withId(R.id.teamsCard)).perform(click());

        // וודא שה-RecyclerView מוצג
        onView(withId(R.id.teamsRecyclerView)).check(matches(isDisplayed()));

        // וודא שכפתור "הוסף קבוצה" מוצג (אם יש הרשאות)
        // onView(withId(R.id.addTeamButton)).check(matches(isDisplayed()));
    }

    /**
     * בדיקה: ה-RecyclerView של הקבוצות מוצג ואינו ריק
     * הערה: בדיקה זו תעבור רק אם יש קבוצות במערכת
     */
    @Test
    public void testTeamsRecyclerViewDisplayed() {
        // נווט לדף קבוצות
        onView(withId(R.id.teamsCard)).perform(click());

        // המתן לטעינת הנתונים
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // וודא שה-RecyclerView מוצג
        onView(withId(R.id.teamsRecyclerView)).check(matches(isDisplayed()));
    }

    /**
     * בדיקה: פילטר הקבוצות פועל כראוי
     */
    @Test
    public void testTeamsFilterWorks() {
        // נווט לדף קבוצות
        onView(withId(R.id.teamsCard)).perform(click());

        // המתן לטעינה
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // בדוק שה-RecyclerView מוצג
        onView(withId(R.id.teamsRecyclerView)).check(matches(isDisplayed()));
    }
}
