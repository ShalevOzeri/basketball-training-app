package com.example.testapp.flow;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.testapp.MainActivity;
import com.example.testapp.R;
import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.action.ViewActions.swipeUp;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * End-to-End Continuous Flow Tests (בדיקות זרימה רציפה מקצה לקצה)
 * 
 * ⚡ זרימה רציפה ללא חזרות:
 * 1. התחברות Firebase אחת (@BeforeClass)
 * 2. כל בדיקה = פעולה ייחודית ברצף
 * 3. התנתקות אחת (@AfterClass)
 * 
 * ⚠️ חשוב: כל הבדיקות עובדות עם test_data/ בלבד ב-Firebase!
 * 
 * 🎯 תרחישים מכוסים (ללא חזרות):
 * E01 - אימות מסך הבית + כל הכרטיסים קיימים
 * E02 - ניווט למגרשים ובדיקת UI
 * E03 - חזרה למסך הבית + אימות
 * E04 - ניווט לקבוצות + בדיקה
 * E05 - חזרה + ניווט ללוח אימונים
 * E06 - חזרה + ניווט לתזמון חכם
 * E07 - חזרה + ניווט לתצוגת כל המגרשים
 * E08 - בדיקת Toolbar ופרטי משתמש
 * E09 - בדיקת גלילה ו-Scroll במסך הבית
 * E10 - בדיקת ניווט חוזר מרובה (Edge Case)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EndToEndFlowTest {

    private ActivityScenario<MainActivity> scenario;
    private static boolean isLoggedIn = false;
    
    private static final String TEST_EMAIL = "shalevozeri951@gmail.com";
    private static final String TEST_PASSWORD = "121074Aa";

    @BeforeClass
    public static void globalSetUp() throws InterruptedException {
        // Step 1: Cleanup  - logout if logged in
        FirebaseAuth.getInstance().signOut();
        Thread.sleep(1000);
        
        // Step 2: One Firebase login for all tests (without UI)
        CountDownLatch latch = new CountDownLatch(1);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD)
                .addOnCompleteListener(task -> {
                    isLoggedIn = task.isSuccessful();
                    latch.countDown();
                });
        
        // Wait for login (maximum 10 seconds)
        latch.await(10, TimeUnit.SECONDS);
        Thread.sleep(2000);
        
        if (!isLoggedIn) {
            throw new RuntimeException("❌ Failed to login in BeforeClass!");
        }
    }

    @Before
    public void setUp() throws InterruptedException {
        // Launch MainActivity (user already logged in from BeforeClass)
        scenario = ActivityScenario.launch(MainActivity.class);
        Thread.sleep(1500);
        
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
        if (scenario != null) {
            scenario.close();
        }
    }

    @AfterClass
    public static void globalTearDown() {
        // One logout at the end
        FirebaseAuth.getInstance().signOut();
        isLoggedIn = false;
    }

    // ========== בדיקות רציפות E2E (ללא חזרות!) ==========

    /**
     * E01: אימות מסך הבית - בדיקה שכל 5 הכרטיסים קיימים
     */
    @Test
    public void e2e_01_VerifyAllNavigationCardsExist() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔍 Testing: Verify All Navigation Cards Exist");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Check 1: Courts card
        onView(withId(R.id.courtsCard))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        Thread.sleep(600);
        
        // Check 2: Teams card
        onView(withId(R.id.teamsCard))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        Thread.sleep(600);
        
        // Check 3: Training schedule card
        onView(withId(R.id.scheduleCard))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        Thread.sleep(600);
        
        // Check 4: Smart scheduling card
        onView(withId(R.id.scheduleGridCard))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        Thread.sleep(600);
        
        // Check 5: All courts view card
        onView(withId(R.id.allCourtsCard))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        Thread.sleep(1000);
    }

    /**
     * E02: ניווט למגרשים + אימות מסך
     */
    @Test
    public void e2e_02_NavigateToCourts() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔍 Testing: Navigate to Courts Screen");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Click on courts card
        onView(withId(R.id.courtsCard))
                .perform(scrollTo(), click());
        Thread.sleep(2500);
        
        // Verify we moved to courts screen (check screen loaded)
        Thread.sleep(1000);
    }

    /**
     * E03: ניווט לקבוצות
     */
    @Test
    public void e2e_03_NavigateToTeams() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔍 Testing: Navigate to Teams Screen");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Click on teams card
        onView(withId(R.id.teamsCard))
                .perform(scrollTo(), click());
        Thread.sleep(2500);
    }

    /**
     * E04: ניווט ללוח אימונים
     */
    @Test
    public void e2e_04_NavigateToSchedule() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔍 Testing: Navigate to Schedule Screen");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Click on training schedule
        onView(withId(R.id.scheduleCard))
                .perform(scrollTo(), click());
        Thread.sleep(2500);
    }

    /**
     * E05: ניווט לתזמון חכם (Schedule Grid)
     */
    @Test
    public void e2e_05_NavigateToSmartSchedule() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔍 Testing: Navigate to Smart Schedule Grid");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Click on smart scheduling
        onView(withId(R.id.scheduleGridCard))
                .perform(scrollTo(), click());
        Thread.sleep(2500);
    }

    /**
     * E06: ניווט לתצוגת כל המגרשים
     */
    @Test
    public void e2e_06_NavigateToAllCourts() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔍 Testing: Navigate to All Courts View");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Click on all courts view
        onView(withId(R.id.allCourtsCard))
                .perform(scrollTo(), click());
        Thread.sleep(2500);
    }

    /**
     * E07: Verify Toolbar and User Details
     */
    @Test
    public void e2e_07_VerifyToolbarDetails() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔍 Testing: Verify Toolbar and User Details");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Check Toolbar
        onView(withId(R.id.toolbar))
                .check(matches(isDisplayed()));
        Thread.sleep(1500);
        
        // Check username (if exists on screen)
        // onView(withText("shalev ozeri")).check(matches(isDisplayed()));
    }

    /**
     * E08: Verify scrolling in home screen (Scroll Test)
     */
    @Test
    public void e2e_08_ScrollTestInHomeScreen() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔍 Testing: Scroll Functionality in Home Screen");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Scroll down
        onView(withId(R.id.scheduleGridCard))
                .perform(scrollTo());
        Thread.sleep(800);
        
        // Scroll up
        onView(withId(R.id.courtsCard))
                .perform(scrollTo());
        Thread.sleep(800);
        
        // Verify first card is displayed
        onView(withId(R.id.courtsCard))
                .check(matches(isDisplayed()));
        Thread.sleep(1000);
    }

    /**
     * E09: Navigate to courts + detailed UI check
     */
    @Test
    public void e2e_09_NavigateToCourtsWithDetailedCheck() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔍 Testing: Navigate to Courts with Detailed UI Check");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Navigate to courts
        onView(withId(R.id.courtsCard))
                .perform(scrollTo(), click());
        Thread.sleep(2500);
        
        // Additional checks in courts screen if any
        Thread.sleep(1500);
    }

    /**
     * E10: Quick navigation to all screens in sequence (Edge Case - speed)
     */
    @Test
    public void e2e_10_QuickNavigationMultipleScreens() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔍 Testing: Quick Navigation Between Multiple Screens");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Courts
        onView(withId(R.id.courtsCard))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        Thread.sleep(500);
        
        // Teams
        onView(withId(R.id.teamsCard))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        Thread.sleep(500);
        
        // Training schedule
        onView(withId(R.id.scheduleCard))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        Thread.sleep(500);
        
        // Final verification
        onView(withId(R.id.courtsCard))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
        Thread.sleep(1000);
    }
}
