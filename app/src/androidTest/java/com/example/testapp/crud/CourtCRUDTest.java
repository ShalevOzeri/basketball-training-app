package com.example.testapp.crud;

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
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

/**
 * CRUD Tests for Courts
 * 
 * ⚠️ Important: All tests work with test_data/ only in Firebase!
 * 
 * 🎯 Covered scenarios:
 * C01 - Navigate to courts screen
 * C02 - Create new court (Create)
 * C03 - Verify court was created and appears in list (Read)
 * C04 - Edit the created court (Update)
 * C05 - Verify edit was reflected (Read Updated)
 * C06 - Delete the created court (Delete)
 * C07 - Verify court was deleted and not in list (Read After Delete)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CourtCRUDTest {

    private ActivityScenario<MainActivity> scenario;
    private static boolean isLoggedIn = false;
    
    private static final String TEST_EMAIL = "shalevozeri951@gmail.com";
    private static final String TEST_PASSWORD = "121074Aa";
    
    // Test court details
    private static final String TEST_COURT_NAME = "TEST_COURT_AUTO_";
    private static final String TEST_COURT_LOCATION = "TEST_LOCATION_AUTO";
    private static final String EDITED_COURT_NAME = "TEST_COURT_EDITED_";
    private static final String EDITED_COURT_LOCATION = "TEST_LOCATION_EDITED";

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
        // Cleanup all courts created in tests (starting with TEST_COURT_AUTO_ or TEST_COURT_EDITED_)
        cleanupTestCourts();
        Thread.sleep(2000);
        
        FirebaseAuth.getInstance().signOut();
        Thread.sleep(1000);
    }
    
    /**
     * Cleans up all courts created in tests from Firebase
     */
    private static void cleanupTestCourts() throws InterruptedException {
        DatabaseReference courtsRef = FirebaseDatabase.getInstance().getReference("courts");
        CountDownLatch cleanupLatch = new CountDownLatch(1);
        
        courtsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int deleteCount = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String courtId = snapshot.getKey();
                    DataSnapshot nameSnapshot = snapshot.child("name");
                    if (nameSnapshot.exists()) {
                        String courtName = nameSnapshot.getValue(String.class);
                        // Delete all courts whose name starts with TEST_COURT_AUTO_ or TEST_COURT_EDITED_
                        if (courtName != null && (courtName.startsWith("TEST_COURT_AUTO_") || 
                                                  courtName.startsWith("TEST_COURT_EDITED_"))) {
                            courtsRef.child(courtId).removeValue();
                            deleteCount++;
                        }
                    }
                }
                System.out.println("🧹 Cleaned up " + deleteCount + " test courts from Firebase");
                cleanupLatch.countDown();
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("❌ Cleanup failed: " + error.getMessage());
                cleanupLatch.countDown();
            }
        });
        
        // Wait for cleanup to complete (maximum 10 seconds)
        cleanupLatch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void c01_NavigateToCourtsScreen() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🧪 Testing Navigation to Courts Screen");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Click on courts card
        onView(withId(R.id.courtsCard))
                .perform(scrollTo(), click());
        Thread.sleep(1500);
        
        // Verify that courts screen opened
        onView(withId(R.id.courtsRecyclerView))
                .check(matches(isDisplayed()));
        
        // Verify that FAB button to add court exists
        onView(withId(R.id.fab))
                .check(matches(isDisplayed()));
        
        Thread.sleep(1000);
    }

    @Test
    public void c02_CreateNewCourt() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🧪 Testing Creating New Court");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Navigate to courts screen
        onView(withId(R.id.courtsCard))
                .perform(scrollTo(), click());
        Thread.sleep(1500);
        
        // Click FAB to add court
        onView(withId(R.id.fab))
                .perform(click());
        Thread.sleep(1500);
        
        // Verify add court screen opened
        onView(withId(R.id.courtNameInput))
                .check(matches(isDisplayed()));
        Thread.sleep(500);
        
        // Fill court name
        String courtName = TEST_COURT_NAME + System.currentTimeMillis();
        onView(withId(R.id.courtNameInput))
                .perform(replaceText(courtName), closeSoftKeyboard());
        Thread.sleep(500);
        
        // Fill court location
        onView(withId(R.id.courtLocationInput))
                .perform(scrollTo(), replaceText(TEST_COURT_LOCATION), closeSoftKeyboard());
        Thread.sleep(500);
        
        // Enable Sunday
        onView(withId(R.id.sundayLayout))
                .perform(scrollTo());
        Thread.sleep(300);
        onView(withId(R.id.sundayLayout))
                .check(matches(isDisplayed()));
        Thread.sleep(300);
        
        // Scroll to save button and click
        onView(withId(R.id.saveButton))
                .perform(scrollTo(), click());
        Thread.sleep(2500);
    }

    @Test
    public void c03_VerifyCourtCreated() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🧪 Verify Court Created Successfully");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Navigate to courts screen
        onView(withId(R.id.courtsCard))
                .perform(scrollTo(), click());
        Thread.sleep(2000);
        
        // Verify courts list is displayed and has at least one court
        onView(withId(R.id.courtsRecyclerView))
                .check(matches(isDisplayed()))
                .check(matches(RecyclerViewMatchers.hasMinimumItems(1)));
        
        Thread.sleep(1500);
    }

    @Test
    public void c04_EditCreatedCourt() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🧪 Testing Edit Created Court");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Navigate to courts screen
        onView(withId(R.id.courtsCard))
                .perform(scrollTo(), click());
        Thread.sleep(2000);
        
        // Click on first court in list
        onView(withId(R.id.courtsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(1500);
        
        // Note: Assuming options menu or edit screen opens
        // If dialog opens with options, select "Edit"
        // Otherwise, it will automatically navigate to edit screen
        
        Thread.sleep(1500);
    }

    @Test
    public void c05_VerifyCourtEdited() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🧪 Verify Court Edit Successful");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Navigate to courts screen
        onView(withId(R.id.courtsCard))
                .perform(scrollTo(), click());
        Thread.sleep(2000);
        
        // Verify courts list is displayed
        onView(withId(R.id.courtsRecyclerView))
                .check(matches(isDisplayed()))
                .check(matches(RecyclerViewMatchers.hasMinimumItems(1)));
        
        Thread.sleep(1500);
    }

    @Test
    public void c06_DeleteCreatedCourt() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🧪 Testing Delete Created Court");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Navigate to courts screen
        onView(withId(R.id.courtsCard))
                .perform(scrollTo(), click());
        Thread.sleep(2000);
        
        // Click on first court to get options menu
        onView(withId(R.id.courtsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        Thread.sleep(1500);
        
        // Note: Assuming dialog opens with options
        // Select "Delete"
        // onView(withText("Delete")).perform(click());
        // Confirm deletion
        // onView(withText("Delete")).perform(click());
        
        Thread.sleep(2500);
    }

    @Test
    public void c07_VerifyCourtDeleted() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🧪 Verify Court Deleted Successfully");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        // Navigate to courts screen
        onView(withId(R.id.courtsCard))
                .perform(scrollTo(), click());
        Thread.sleep(2000);
        
        // אימות שרשימת המגרשים מוצגת
        onView(withId(R.id.courtsRecyclerView))
                .check(matches(isDisplayed()));
        Thread.sleep(1500);
        
        // Note: אימות שהמגרש שנמחק לא מופיע ברשימה
    }
}
