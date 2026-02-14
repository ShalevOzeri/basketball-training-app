package com.example.testapp.integration;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.testapp.models.Training;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Integration Tests for Firebase Database Operations
 * 
 * Firebase integration tests - test that server connection works
 * ⚠️ Requires: internet connection and logged-in user
 */
@RunWith(AndroidJUnit4.class)
public class FirebaseIntegrationTest {

    private DatabaseReference testRef;
    private String testTrainingId;
    private FirebaseAuth auth;

    @Before
    public void setUp() throws InterruptedException {
        auth = FirebaseAuth.getInstance();
        
        // Connect with application user
        CountDownLatch loginLatch = new CountDownLatch(1);
        final boolean[] loginSuccess = {false};
        
        auth.signInWithEmailAndPassword("shalevozeri951@gmail.com", "121074Aa")
                .addOnSuccessListener(authResult -> {
                    loginSuccess[0] = true;
                    loginLatch.countDown();
                })
                .addOnFailureListener(e -> {
                    loginLatch.countDown();
                });
        
        loginLatch.await(10, TimeUnit.SECONDS);
        
        if (!loginSuccess[0]) {
            throw new RuntimeException("Failed to authenticate test user");
        }
        
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        testRef = database.getReference("test_data");
        testRef.removeValue();
    }

    @After
    public void tearDown() {
        if (testTrainingId != null) {
            testRef.child("trainings").child(testTrainingId).removeValue();
        }
        testRef.removeValue();
        
        // Disconnect from user
        if (auth != null) {
            auth.signOut();
        }
    }

    @Test
    public void writeAndReadTraining_Success() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔥 Testing: Write and Read Training from Firebase");
        System.out.println("========================================");
        Training testTraining = new Training();
        testTraining.setTrainingId("test_training_123");
        testTraining.setTeamName("קבוצת בדיקה");
        testTraining.setCourtName("מגרש בדיקה");
        testTraining.setStartTime("18:00");
        testTraining.setEndTime("20:00");
        testTraining.setDate(System.currentTimeMillis());
        
        CountDownLatch latch = new CountDownLatch(1);
        final Training[] readTraining = new Training[1];
        final String[] error = new String[1];
        
        testRef.child("trainings").child(testTraining.getTrainingId())
                .setValue(testTraining)
                .addOnSuccessListener(aVoid -> {
                    testRef.child("trainings").child(testTraining.getTrainingId())
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                readTraining[0] = snapshot.getValue(Training.class);
                                latch.countDown();
                            })
                            .addOnFailureListener(e -> {
                                error[0] = "Failed to read: " + e.getMessage();
                                latch.countDown();
                            });
                })
                .addOnFailureListener(e -> {
                    error[0] = "Failed to write: " + e.getMessage();
                    latch.countDown();
                });
        
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        
        if (!completed) {
            fail("Firebase operation timed out after 10 seconds. Check internet connection.");
        }
        
        if (error[0] != null) {
            fail("Firebase error: " + error[0]);
        }
        
        assertNotNull("Training was not read from Firebase", readTraining[0]);
        assertEquals(testTraining.getTrainingId(), readTraining[0].getTrainingId());
        assertEquals(testTraining.getTeamName(), readTraining[0].getTeamName());
        assertEquals(testTraining.getCourtName(), readTraining[0].getCourtName());
    }

    @Test
    public void deleteTraining_Success() throws InterruptedException {
        System.out.println("\n========================================");
        System.out.println("🔥 Testing: Delete Training from Firebase");
        System.out.println("========================================");
        String trainingId = "test_delete_123";
        Training testTraining = new Training();
        testTraining.setTrainingId(trainingId);
        testTraining.setTeamName("קבוצה למחיקה");
        
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] exists = {true};
        final String[] error = new String[1];
        
        testRef.child("trainings").child(trainingId).setValue(testTraining)
                .addOnSuccessListener(aVoid -> {
                    testRef.child("trainings").child(trainingId).removeValue()
                            .addOnSuccessListener(aVoid2 -> {
                                testRef.child("trainings").child(trainingId).get()
                                        .addOnSuccessListener(snapshot -> {
                                            exists[0] = snapshot.exists();
                                            latch.countDown();
                                        })
                                        .addOnFailureListener(e -> {
                                            error[0] = "Failed to verify deletion: " + e.getMessage();
                                            latch.countDown();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                error[0] = "Failed to delete: " + e.getMessage();
                                latch.countDown();
                            });
                })
                .addOnFailureListener(e -> {
                    error[0] = "Failed to create: " + e.getMessage();
                    latch.countDown();
                });
        
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        
        if (!completed) {
            fail("Firebase operation timed out after 10 seconds. Check internet connection.");
        }
        
        if (error[0] != null) {
            fail("Firebase error: " + error[0]);
        }
        
        assertFalse("Training should not exist after deletion", exists[0]);
    }
}
