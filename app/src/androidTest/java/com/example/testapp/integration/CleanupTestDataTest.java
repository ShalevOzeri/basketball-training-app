package com.example.testapp.integration;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Special test for cleaning up test data from Firebase
 * 
 * Cleans all data created by automated tests:
 * - Courts starting with TEST_COURT_AUTO_ or TEST_COURT_EDITED_
 * - Teams starting with TEST_TEAM_AUTO_ or TEST_TEAM_EDITED_
 * 
 * ⚠️ Run this test to clean up test data left in the database!
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CleanupTestDataTest {

    private static final String TEST_EMAIL = "shalevozeri951@gmail.com";
    private static final String TEST_PASSWORD = "121074Aa";

    @BeforeClass
    public static void setUp() throws InterruptedException {
        // Connect to Firebase
        FirebaseAuth.getInstance().signOut();
        Thread.sleep(1000);
        
        CountDownLatch latch = new CountDownLatch(1);
        FirebaseAuth.getInstance().signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD)
                .addOnCompleteListener(task -> latch.countDown());
        
        latch.await(10, TimeUnit.SECONDS);
        Thread.sleep(2000);
    }

    @Test
    public void cleanupAllTestData() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🧹 Cleanup: Removing All Test Data from Firebase");
        System.out.println("========================================");
        System.out.println("🧹 Starting cleanup of all test data...");
        
        // Cleanup courts
        cleanupTestCourts();
        Thread.sleep(2000);
        
        // Cleanup teams
        cleanupTestTeams();
        Thread.sleep(2000);
        
        // Cleanup trainings
        cleanupTestTrainings();
        Thread.sleep(2000);
        
        System.out.println("✅ Cleanup completed!");
    }

    private void cleanupTestCourts() throws InterruptedException {
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
                        if (courtName != null && (courtName.startsWith("TEST_COURT_AUTO_") || 
                                                  courtName.startsWith("TEST_COURT_EDITED_"))) {
                            courtsRef.child(courtId).removeValue();
                            deleteCount++;
                            System.out.println("🗑️ Deleting test court: " + courtName);
                        }
                    }
                }
                System.out.println("🧹 Cleaned up " + deleteCount + " test courts from Firebase");
                cleanupLatch.countDown();
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("❌ Courts cleanup failed: " + error.getMessage());
                cleanupLatch.countDown();
            }
        });
        
        cleanupLatch.await(10, TimeUnit.SECONDS);
    }

    private void cleanupTestTeams() throws InterruptedException {
        DatabaseReference teamsRef = FirebaseDatabase.getInstance().getReference("teams");
        CountDownLatch cleanupLatch = new CountDownLatch(1);
        
        teamsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int deleteCount = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String teamId = snapshot.getKey();
                    DataSnapshot nameSnapshot = snapshot.child("name");
                    if (nameSnapshot.exists()) {
                        String teamName = nameSnapshot.getValue(String.class);
                        if (teamName != null && (teamName.startsWith("TEST_TEAM_AUTO_") || 
                                                teamName.startsWith("TEST_TEAM_EDITED_"))) {
                            teamsRef.child(teamId).removeValue();
                            deleteCount++;
                            System.out.println("🗑️ Deleting test team: " + teamName);
                        }
                    }
                }
                System.out.println("🧹 Cleaned up " + deleteCount + " test teams from Firebase");
                cleanupLatch.countDown();
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("❌ Teams cleanup failed: " + error.getMessage());
                cleanupLatch.countDown();
            }
        });
        
        cleanupLatch.await(10, TimeUnit.SECONDS);
    }

    private void cleanupTestTrainings() throws InterruptedException {
        DatabaseReference trainingsRef = FirebaseDatabase.getInstance().getReference("trainings");
        CountDownLatch cleanupLatch = new CountDownLatch(1);
        
        trainingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int deleteCount = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String trainingId = snapshot.getKey();
                    DataSnapshot notesSnapshot = snapshot.child("notes");
                    if (notesSnapshot.exists()) {
                        String notes = notesSnapshot.getValue(String.class);
                        if (notes != null && (notes.startsWith("TEST_TRAINING_AUTO_") || 
                                            notes.startsWith("TEST_TRAINING_EDITED_"))) {
                            trainingsRef.child(trainingId).removeValue();
                            deleteCount++;
                            System.out.println("🗑️ Deleting test training: " + notes);
                        }
                    }
                }
                System.out.println("🧹 Cleaned up " + deleteCount + " test trainings from Firebase");
                cleanupLatch.countDown();
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("❌ Trainings cleanup failed: " + error.getMessage());
                cleanupLatch.countDown();
            }
        });
        
        cleanupLatch.await(10, TimeUnit.SECONDS);
    }
}
