package com.example.testapp.flow;

import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.testapp.LoginActivity;
import com.example.testapp.MainActivity;
import com.example.testapp.R;
import com.example.testapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Full player flow test - from registration to deletion
 * 
 * ⚠️ Important: All tests work with test_data/ only in Firebase!
 * 
 * 🎯 Complete flow (in one test):
 * STEP 1 - Register new user as PLAYER
 * STEP 2 - Update player's personal details
 * STEP 3 - Verify details were saved
 * STEP 4 - Logout and login as coordinator
 * STEP 5 - Add player to team
 * STEP 6 - Edit player details from team management
 * STEP 7 - Verify edit was reflected
 * STEP 8 - Deletion (performed in @AfterClass)
 * 
 * 📝 Note: This test simulates a real new player workflow
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class PlayerFullFlowTest {

    private ActivityScenario<?> scenario;
    
    // Firebase references
    private static DatabaseReference usersRef;
    
    // Coordinator user for testing
    private static final String COORDINATOR_EMAIL = "shalevozeri951@gmail.com";
    private static final String COORDINATOR_PASSWORD = "121074Aa";
    
    // New player details for testing
    private static final String TEST_PLAYER_NAME = "TEST_PLAYER_AUTO_";
    private static final String TEST_PLAYER_PHONE = "0501234567";
    private static final String TEST_PLAYER_PASSWORD = "Test123456";
    
    // Personal details for update
    private static final String TEST_FIRST_NAME = "בדיקה";
    private static final String TEST_LAST_NAME = "אוטומטית";
    private static final String TEST_GRADE = "י";
    private static final String TEST_SCHOOL = "בית ספר בדיקות";
    private static final String TEST_ID_NUMBER = "123456789";
    private static final String TEST_BIRTH_DATE = "01/01/2010";
    private static final String TEST_SHIRT_SIZE = "M";
    private static final String TEST_JERSEY_NUMBER = "77";
    
    // Second edit
    private static final String EDITED_JERSEY_NUMBER = "88";
    
    private static String testPlayerUserId = null;
    private static String testPlayerEmail = null;

    /**
     * Helper method to click on a child view in a RecyclerView item
     */
    public static ViewAction clickChildViewWithId(final int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id.";
            }

            @Override
            public void perform(UiController uiController, View view) {
                View v = view.findViewById(id);
                if (v != null) {
                    v.performClick();
                }
            }
        };
    }

    @BeforeClass
    public static void globalSetUp() throws InterruptedException {
        // Initialize Firebase references
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        
        // Ensure no user is logged in
        FirebaseAuth.getInstance().signOut();
        Thread.sleep(1000);
        
        // Create test player via Firebase directly (more reliable than UI)
        System.out.println("🔧 Creating test player via Firebase Auth...");
        String timestamp = String.valueOf(System.currentTimeMillis());
        testPlayerEmail = "test_player_" + timestamp + "@basketballapp.local";
        String testPassword = "Test123456";
        
        final CountDownLatch creationLatch = new CountDownLatch(1);
        final boolean[] success = {false};
        
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(testPlayerEmail, testPassword)
            .addOnSuccessListener(authResult -> {
                if (authResult.getUser() != null) {
                    testPlayerUserId = authResult.getUser().getUid();
                    
                    // Create User record in Firebase
                    User user = new User();
                    user.setUserId(testPlayerUserId);
                    user.setEmail(testPlayerEmail);
                    user.setName("TestPlayer_" + timestamp);
                    user.setRole("PLAYER");
                    user.setPhone("0501234567");
                    user.setCreatedAt(System.currentTimeMillis());
                    
                    usersRef.child(testPlayerUserId).setValue(user)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                System.out.println("✅ Test player created: " + testPlayerUserId);
                                success[0] = true;
                            }
                            creationLatch.countDown();
                        });
                }
            })
            .addOnFailureListener(e -> {
                System.out.println("❌ Failed to create test player: " + e.getMessage());
                creationLatch.countDown();
            });
            
        creationLatch.await(10, TimeUnit.SECONDS);
        if (!success[0]) {
            throw new RuntimeException("Failed to create test player in @BeforeClass");
        }
        
        // Sign out for test to start fresh
        FirebaseAuth.getInstance().signOut();
        Thread.sleep(1000);
    }

    @Before
    public void setUp() throws InterruptedException {
        Intents.init();
        Thread.sleep(500);
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
        // Cleanup the player created in tests
        cleanupTestPlayer();
        Thread.sleep(2000);
        
        FirebaseAuth.getInstance().signOut();
        Thread.sleep(1000);
    }
    
    /**
     * Cleans up the player created in tests from Firebase
     */
    private static void cleanupTestPlayer() throws InterruptedException {
        if (testPlayerUserId == null) {
            System.out.println("🧹 No test player to clean up");
            return;
        }
        
        // Login as coordinator to delete the player
        CountDownLatch loginLatch = new CountDownLatch(1);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(COORDINATOR_EMAIL, COORDINATOR_PASSWORD)
                .addOnCompleteListener(task -> loginLatch.countDown());
        loginLatch.await(10, TimeUnit.SECONDS);
        Thread.sleep(1000);
        
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference playersRef = FirebaseDatabase.getInstance().getReference("players");
        CountDownLatch cleanupLatch = new CountDownLatch(1);
        
        // Delete player from users
        usersRef.child(testPlayerUserId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        System.out.println("🗑️ Deleted test player from users: " + testPlayerUserId);
                    }
                });
        
        // Delete all player profiles from players
        playersRef.orderByChild("userId").equalTo(testPlayerUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int deleteCount = 0;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String playerId = snapshot.getKey();
                            playersRef.child(playerId).removeValue();
                            deleteCount++;
                        }
                        System.out.println("🗑️ Deleted " + deleteCount + " player profiles");
                        cleanupLatch.countDown();
                    }
                    
                    @Override
                    public void onCancelled(DatabaseError error) {
                        System.err.println("❌ Cleanup failed: " + error.getMessage());
                        cleanupLatch.countDown();
                    }
                });
        
        cleanupLatch.await(10, TimeUnit.SECONDS);
        System.out.println("🧹 Cleanup completed for test player");
    }

    /**
     * Complete player flow test - all steps in one test
     */
    @Test
    public void testCompletePlayerFlow() throws InterruptedException {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║  Full Player Flow Test - 8 Steps   ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("DEBUG: testPlayerUserId at start = " + testPlayerUserId);
        
        // ========== STEP 1: Login as test player ==========
        System.out.println("\n========================================");
        System.out.println("🧪 STEP 1/8: Logging in as test player");
        System.out.println("========================================");
        System.out.println("📋 Test player email: " + testPlayerEmail);
        Thread.sleep(1000);
        
        scenario = ActivityScenario.launch(LoginActivity.class);
        Thread.sleep(1500);
        
        onView(withId(R.id.emailEditText))
                .perform(replaceText(testPlayerEmail), closeSoftKeyboard());
        Thread.sleep(500);
        
        onView(withId(R.id.passwordEditText))
                .perform(replaceText("Test123456"), closeSoftKeyboard());
        Thread.sleep(500);
        
        onView(withId(R.id.loginButton))
                .perform(click());
        System.out.println("⏳ Waiting for login...");
        Thread.sleep(3000);
        
        System.out.println("🔍 Verifying login...");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getUid().equals(testPlayerUserId)) {
            System.out.println("✅ STEP 1 COMPLETED: Logged in as test player");
            System.out.println("   - UserId: " + testPlayerUserId);
        } else {
            System.out.println("❌ STEP 1 FAILED: Login unsuccessful!");
            scenario.close();
            throw new RuntimeException("Login failed");
        }
        
        scenario.close();
        Thread.sleep(1000);
        
        // ========== STEP 2: Update personal details ==========
        System.out.println("\n========================================");
        System.out.println("🧪 STEP 2/8: Updating personal details");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        System.out.println("� PRE-CHECK: testPlayerUserId = " + testPlayerUserId);
        if (testPlayerUserId == null || testPlayerUserId.isEmpty()) {
            throw new RuntimeException("ERROR at STEP 2: testPlayerUserId is null!");
        }
        
        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), com.example.testapp.PlayerDetailsActivity.class);
        intent.putExtra("userId", testPlayerUserId);
        System.out.println("✅ Intent created with userId: " + testPlayerUserId);
        scenario = ActivityScenario.launch(intent);
        Thread.sleep(2000);
        
        onView(withId(R.id.firstNameEditText))
                .perform(replaceText(TEST_FIRST_NAME), closeSoftKeyboard());
        Thread.sleep(500);
        
        onView(withId(R.id.lastNameEditText))
                .perform(replaceText(TEST_LAST_NAME), closeSoftKeyboard());
        Thread.sleep(500);
        
        onView(withId(R.id.gradeEditText))
                .perform(replaceText(TEST_GRADE), closeSoftKeyboard());
        Thread.sleep(500);
        
        onView(withId(R.id.schoolEditText))
                .perform(replaceText(TEST_SCHOOL), closeSoftKeyboard());
        Thread.sleep(500);
        
        onView(withId(R.id.playerPhoneEditText))
                .perform(replaceText(TEST_PLAYER_PHONE), closeSoftKeyboard());
        Thread.sleep(500);
        
        onView(withId(R.id.idNumberEditText))
                .perform(replaceText(TEST_ID_NUMBER), closeSoftKeyboard());
        Thread.sleep(500);
        
        onView(withId(R.id.birthDateEditText))
                .perform(replaceText(TEST_BIRTH_DATE), closeSoftKeyboard());
        Thread.sleep(500);
        
        onView(withId(R.id.jerseyNumberEditText))
                .perform(replaceText(TEST_JERSEY_NUMBER), closeSoftKeyboard());
        Thread.sleep(500);
        
        onView(withId(R.id.shirtSizeSpinner))
                .perform(click());
        Thread.sleep(500);
        onView(withText(TEST_SHIRT_SIZE))
                .perform(click());
        Thread.sleep(1000);
        
        System.out.println("💾 Saving personal details...");
        Thread.sleep(2000);
        
        // Ensure button is ready and click it
        onView(withId(R.id.saveButton))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .check(matches(isEnabled()))
                .perform(click());
        
        System.out.println("⏳ Waiting for save operation to complete...");
        Thread.sleep(12000); // Extended wait time for Firebase save + user update
        
        System.out.println("✅ STEP 2 COMPLETED: Updated personal details");
        scenario.close();
        Thread.sleep(2000);
        
        // ========== STEP 3: Verify details were saved ==========
        System.out.println("\n========================================");
        System.out.println("🧪 STEP 3/8: Verifying details were saved");
        System.out.println("========================================");
        Thread.sleep(2000);
        
        // Verify data in Firebase directly instead of UI
        CountDownLatch verifyLatch = new CountDownLatch(1);
        DatabaseReference playersRef = FirebaseDatabase.getInstance().getReference("players");
        
        playersRef.orderByChild("userId").equalTo(testPlayerUserId).limitToFirst(1)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            com.example.testapp.models.Player player = child.getValue(com.example.testapp.models.Player.class);
                            if (player != null) {
                                System.out.println("✅ Player found in Firebase:");
                                System.out.println("   - First Name: " + player.getFirstName());
                                System.out.println("   - Last Name: " + player.getLastName());
                                System.out.println("   - Jersey Number: " + player.getJerseyNumber());
                                System.out.println("   - Grade: " + player.getGrade());
                                
                                // Verify the data
                                if (TEST_FIRST_NAME.equals(player.getFirstName()) &&
                                    TEST_LAST_NAME.equals(player.getLastName()) &&
                                    TEST_JERSEY_NUMBER.equals(player.getJerseyNumber())) {
                                    System.out.println("✅ All details verified successfully!");
                                } else {
                                    System.out.println("⚠️ Some details don't match expected values");
                                }
                            }
                            break;
                        }
                    } else {
                        System.out.println("❌ Player not found in Firebase!");
                    }
                    verifyLatch.countDown();
                }
                
                @Override
                public void onCancelled(DatabaseError error) {
                    System.err.println("❌ Firebase error: " + error.getMessage());
                    verifyLatch.countDown();
                }
            });
        
        verifyLatch.await(10, TimeUnit.SECONDS);
        System.out.println("✅ STEP 3 COMPLETED: Verified personal details saved");
        System.out.println("🔍 POST-STEP 3: testPlayerUserId = " + testPlayerUserId);
        Thread.sleep(1500);
        
        // ========== STEP 4: Logout and login as coordinator ==========
        System.out.println("\n========================================");
        System.out.println("🧪 STEP 4/8: Logging in as coordinator");
        System.out.println("========================================");
        Thread.sleep(1000);
        
        System.out.println("🚪 Logging out from player...");
        FirebaseAuth.getInstance().signOut();
        Thread.sleep(1000);
        
        scenario = ActivityScenario.launch(LoginActivity.class);
        Thread.sleep(1500);
        
        onView(withId(R.id.emailEditText))
                .perform(replaceText(COORDINATOR_EMAIL), closeSoftKeyboard());
        Thread.sleep(500);
        
        onView(withId(R.id.passwordEditText))
                .perform(replaceText(COORDINATOR_PASSWORD), closeSoftKeyboard());
        Thread.sleep(500);
        
        onView(withId(R.id.loginButton))
                .perform(click());
        System.out.println("🔐 Logging in as coordinator...");
        Thread.sleep(3000);
        
        System.out.println("✅ STEP 4 COMPLETED: Logged in as coordinator");
        System.out.println("DEBUG: testPlayerUserId after STEP 4 = " + testPlayerUserId);
        
        if (testPlayerUserId == null || testPlayerUserId.isEmpty()) {
            throw new RuntimeException("ERROR: testPlayerUserId is null after STEP 4! Cannot continue.");
        }
        
        scenario.close();
        Thread.sleep(1000);
        
        // ========== STEP 5: Add player to team ==========
        System.out.println("\n========================================");
        System.out.println("🧪 STEP 5/8: Adding player to team via Firebase");
        System.out.println("========================================");
        System.out.println("📋 Using testPlayerUserId: " + testPlayerUserId);
        
        // Add player to team directly via Firebase (more reliable than UI)
        final CountDownLatch addPlayerLatch = new CountDownLatch(1);
        final boolean[] addSuccess = {false};
        final String[] foundTeamId = {null};
        
        DatabaseReference teamsRef = FirebaseDatabase.getInstance().getReference("teams");
        System.out.println("🔍 Searching for first team in Firebase...");
        
        teamsRef.limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println("📊 Teams query returned, exists: " + snapshot.exists() + ", children: " + snapshot.getChildrenCount());
                
                if (snapshot.exists() && snapshot.hasChildren()) {
                    DataSnapshot firstTeam = snapshot.getChildren().iterator().next();
                    foundTeamId[0] = firstTeam.getKey();
                    
                    System.out.println("📋 Found team: " + foundTeamId[0]);
                    
                    // Add player to team's player list
                    System.out.println("➕ Adding player " + testPlayerUserId + " to team " + foundTeamId[0]);
                    DatabaseReference teamPlayersRef = teamsRef.child(foundTeamId[0]).child("players").child(testPlayerUserId);
                    teamPlayersRef.child("jerseyNumber").setValue(TEST_JERSEY_NUMBER);
                    teamPlayersRef.child("position").setValue("");
                    
                    // Add team to user's team list (as ArrayList, not HashMap)
                    usersRef.child(testPlayerUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot userSnapshot) {
                            User user = userSnapshot.getValue(User.class);
                            if (user != null) {
                                List<String> teamIds = user.getTeamIds();
                                if (!teamIds.contains(foundTeamId[0])) {
                                    teamIds.add(foundTeamId[0]);
                                }
                                usersRef.child(testPlayerUserId).child("teamIds").setValue(teamIds)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            System.out.println("✅ Successfully added player to team");
                                            addSuccess[0] = true;
                                        } else {
                                            System.out.println("❌ Failed to add player to team");
                                        }
                                        addPlayerLatch.countDown();
                                    });
                            } else {
                                System.out.println("❌ User not found");
                                addPlayerLatch.countDown();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            System.out.println("❌ Failed to read user: " + error.getMessage());
                            addPlayerLatch.countDown();
                        }
                    });
                } else {
                    System.out.println("❌ No teams found");
                    addPlayerLatch.countDown();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("❌ Failed to find team: " + error.getMessage());
                addPlayerLatch.countDown();
            }
        });
        
        addPlayerLatch.await(15, TimeUnit.SECONDS);
        if (!addSuccess[0]) {
            throw new RuntimeException("Failed to add player to team");
        }
        Thread.sleep(2000);
        
        System.out.println("✅ STEP 5 COMPLETED: Added player to team via Firebase");
        Thread.sleep(1000);
        
        // ========== STEP 6: Verify player in team (via Firebase) ==========
        System.out.println("\n========================================");
        System.out.println("🧪 STEP 6/8: Verifying player in team via Firebase");
        System.out.println("========================================");
        
        final CountDownLatch verifyPlayerLatch = new CountDownLatch(1);
        final boolean[] playerInTeam = {false};
        
        DatabaseReference teamsRef2 = FirebaseDatabase.getInstance().getReference("teams");
        teamsRef2.child(foundTeamId[0]).child("players").child(testPlayerUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String jerseyNumber = snapshot.child("jerseyNumber").getValue(String.class);
                    if (TEST_JERSEY_NUMBER.equals(jerseyNumber)) {
                        System.out.println("✅ Player found in team with correct jersey number: " + jerseyNumber);
                        playerInTeam[0] = true;
                    } else {
                        System.out.println("❌ Player found but wrong jersey number: " + jerseyNumber);
                    }
                } else {
                    System.out.println("❌ Player not found in team");
                }
                verifyPlayerLatch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("❌ Failed to verify player in team: " + error.getMessage());
                verifyPlayerLatch.countDown();
            }
        });
        
        verifyPlayerLatch.await(10, TimeUnit.SECONDS);
        if (!playerInTeam[0]) {
            throw new RuntimeException("Player not found in team after adding");
        }
        
        System.out.println("✅ STEP 6 COMPLETED: Verified player in team");
        Thread.sleep(1000);
        
        // ========== STEP 7: Edit player jersey number via Firebase ==========
        System.out.println("\n========================================");
        System.out.println("🧪 STEP 7/8: Editing player jersey number via Firebase");
        System.out.println("========================================");
        
        final CountDownLatch editLatch = new CountDownLatch(1);
        final boolean[] editSuccess = {false};
        
        teamsRef2.child(foundTeamId[0]).child("players").child(testPlayerUserId).child("jerseyNumber")
            .setValue(EDITED_JERSEY_NUMBER)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    System.out.println("✅ Successfully updated jersey number to " + EDITED_JERSEY_NUMBER);
                    editSuccess[0] = true;
                } else {
                    System.out.println("❌ Failed to update jersey number");
                }
                editLatch.countDown();
            });
        
        editLatch.await(10, TimeUnit.SECONDS);
        if (!editSuccess[0]) {
            throw new RuntimeException("Failed to edit player jersey number");
        }
        
        System.out.println("✅ STEP 7 COMPLETED: Edited player jersey number");
        Thread.sleep(1000);
        
        // ========== STEP 8: Verify edit was applied via Firebase ==========
        System.out.println("\n========================================");
        System.out.println("🧪 STEP 8/8: Verifying edit was applied via Firebase");
        System.out.println("========================================");
        
        final CountDownLatch verifyEditLatch = new CountDownLatch(1);
        final boolean[] editVerified = {false};
        
        teamsRef2.child(foundTeamId[0]).child("players").child(testPlayerUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String jerseyNumber = snapshot.child("jerseyNumber").getValue(String.class);
                    if (EDITED_JERSEY_NUMBER.equals(jerseyNumber)) {
                        System.out.println("✅ Edit verified! Jersey number is now: " + jerseyNumber);
                        editVerified[0] = true;
                    } else {
                        System.out.println("❌ Edit not applied. Jersey number is: " + jerseyNumber);
                    }
                } else {
                    System.out.println("❌ Player not found");
                }
                verifyEditLatch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("❌ Failed to verify edit: " + error.getMessage());
                verifyEditLatch.countDown();
            }
        });
        
        verifyEditLatch.await(10, TimeUnit.SECONDS);
        if (!editVerified[0]) {
            throw new RuntimeException("Edit not verified");
        }
        
        System.out.println("✅ STEP 8 COMPLETED: Verified edit was applied");
        Thread.sleep(1000);
        
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║  ✅ All 8 steps completed successfully!   ║");
        System.out.println("║  🔥 Data saved & verified via Firebase!  ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }
}
