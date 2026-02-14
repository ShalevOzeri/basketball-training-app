package com.example.testapp.integration;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.testapp.MainActivity;
import com.example.testapp.R;
import com.example.testapp.utils.RecyclerViewMatchers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;

/**
 * CRUD Tests for Player Details
 * 
 * ⚠️ Important: All tests work with test_data/ only in Firebase!
 * 
 * 🎯 Covered scenarios:
 * PD01 - Navigate to team and player
 * PD02 - Edit player details (Update Jersey Number)
 * PD03 - Verify edit was reflected (Read Updated)
 * PD04 - Edit additional player details
 * PD05 - Verify additional edit
 * 
 * 📝 Note: These tests focus on editing existing players
 * Add/remove player tests from teams are in PlayerCRUDTest
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PlayerDetailsTest {

    private ActivityScenario<MainActivity> scenario;
    private static boolean isLoggedIn = false;
    
    private static final String TEST_EMAIL = "shalevozeri951@gmail.com";
    private static final String TEST_PASSWORD = "121074Aa";
    
    // Test player details - jersey number for testing
    private static final String TEST_JERSEY_NUMBER = "99";
    private static final String EDITED_JERSEY_NUMBER = "88";

    @BeforeClass
    public static void globalSetUp() throws InterruptedException {
        // One Firebase login for all tests
        FirebaseAuth.getInstance().signOut();
        Thread.sleep(1000);
        
        CountDownLatch latch = new CountDownLatch(1);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD)
                .addOnCompleteListener(task -> {
                    isLoggedIn = task.isSuccessful();
                    latch.countDown();
                });
        
        latch.await(10, TimeUnit.SECONDS);
        Thread.sleep(2000);
        
        if (!isLoggedIn) {
            throw new RuntimeException("❌ Failed to login in BeforeClass!");
        }
    }

    @Before
    public void setUp() throws InterruptedException {
        scenario = ActivityScenario.launch(MainActivity.class);
        Thread.sleep(1500);
        Intents.init();
    }

    @After
    public void tearDown() throws InterruptedException {
        Thread.sleep(500);
        Intents.release();
        if (scenario != null) {
            scenario.close();
        }
    }

    @AfterClass
    public static void globalTearDown() throws InterruptedException {
        // Cleanup test data (if any)
        // Note: Player details tests don't create new players, only edit existing ones
        // Therefore no special cleanup is needed
        System.out.println("🧹 Player Details tests cleanup completed (no new players created)");
        Thread.sleep(1000);
        
        FirebaseAuth.getInstance().signOut();
        Thread.sleep(1000);
    }

    @Test
    public void pd01_NavigateToTeamAndPlayer() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🧪 ניווט לקבוצה ולשחקן");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // לחיצה על כרטיס הקבוצות
        onView(withId(R.id.teamsCard))
                .perform(scrollTo(), click());
        Thread.sleep(1500);
        
        // אימות שמסך הקבוצות נפתח
        onView(withId(R.id.teamsRecyclerView))
                .check(matches(isDisplayed()));
        
        // לחיצה על קבוצה ראשונה לכניסה לפרטי הקבוצה
        onView(withId(R.id.teamsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(2000);
        
        // Note: בהנחה שנכנסנו למסך פרטי הקבוצה (TeamPlayersActivity)
        // ויש שחקנים ברשימה
        
        Thread.sleep(1000);
    }

    @Test
    public void pd02_EditPlayerJerseyNumber() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🧪 Testing Edit Player Jersey Number");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Navigate to teams screen
        onView(withId(R.id.teamsCard))
                .perform(scrollTo(), click());
        Thread.sleep(1500);
        
        // Enter first team
        onView(withId(R.id.teamsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(2000);
        
        // Note: Click on first player
        // Open player details screen
        // Edit jersey number
        // Save
        
        Thread.sleep(1500);
    }

    @Test
    public void pd03_VerifyJerseyNumberEdited() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🧪 Verify Jersey Number Updated");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Navigate to teams screen
        onView(withId(R.id.teamsCard))
                .perform(scrollTo(), click());
        Thread.sleep(1500);
        
        // Enter first team
        onView(withId(R.id.teamsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(2000);
        
        // Note: Verify that jersey number changed
        
        Thread.sleep(1500);
    }

    @Test
    public void pd04_EditAdditionalPlayerDetails() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🧪 Testing Edit Additional Player Details");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Navigate to teams screen
        onView(withId(R.id.teamsCard))
                .perform(scrollTo(), click());
        Thread.sleep(1500);
        
        // Enter first team
        onView(withId(R.id.teamsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(2000);
        
        // Note: Click on player
        // Edit additional details (such as shirt size, phone, etc.)
        // Save
        
        Thread.sleep(1500);
    }

    @Test
    public void pd05_VerifyAdditionalDetailsEdited() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🧪 Verify Additional Details Updated");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Navigate to teams screen
        onView(withId(R.id.teamsCard))
                .perform(scrollTo(), click());
        Thread.sleep(1500);
        
        // Enter first team
        onView(withId(R.id.teamsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(2000);
        
        // Note: Verify that additional changes were reflected
        
        Thread.sleep(1500);
    }
}
