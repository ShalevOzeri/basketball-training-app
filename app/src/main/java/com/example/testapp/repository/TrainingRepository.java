package com.example.testapp.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.testapp.models.Training;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrainingRepository {
    private static final String TAG = "TrainingRepository";
    private final DatabaseReference trainingsRef;
    private final MutableLiveData<List<Training>> trainingsLiveData;
    private final MutableLiveData<String> errorLiveData;

    public TrainingRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        trainingsRef = database.getReference("trainings");
        trainingsLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        loadTrainings();
    }

    private void loadTrainings() {
        trainingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Training> trainings = new ArrayList<>();
                int totalRecords = 0;
                int skippedRecords = 0;
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    totalRecords++;
                    Training training = snapshot.getValue(Training.class);
                    if (training != null) {
                        // Validate training has all required fields
                        if (training.getTrainingId() == null || training.getTrainingId().isEmpty()) {
                            android.util.Log.w("TrainingRepository", "Skipping training without trainingId");
                            skippedRecords++;
                            continue;
                        }
                        
                        // Skip trainings without courtId (data integrity issue)
                        if (training.getCourtId() == null || training.getCourtId().isEmpty()) {
                            android.util.Log.w("TrainingRepository", "Skipping training with null/empty courtId: " + training.getTeamName());
                            skippedRecords++;
                            continue;
                        }
                        
                        trainings.add(training);
                    }
                }
                android.util.Log.d("TrainingRepository", "Loaded trainings: " + trainings.size() + " valid, " + skippedRecords + " skipped out of " + totalRecords + " total");
                trainingsLiveData.setValue(trainings);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                errorLiveData.setValue(error.getMessage());
            }
        });
    }
    
    private int timeToMinutes(String time) {
        try {
            if (time == null || time.isEmpty()) return -1;
            String[] parts = time.split(":");
            if (parts.length != 2) return -1;
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            if (h < 0 || h > 23 || m < 0 || m > 59) return -1;
            return h * 60 + m;
        } catch (Exception e) {
            return -1;
        }
    }
    
    public void addTraining(Training training, OnConflictCheckListener listener) {
        // Check for conflicts before adding
        android.util.Log.d("TrainingRepository", "Checking for conflicts for training: Team=" + training.getTeamName() + ", Court=" + training.getCourtId());
        
        trainingsRef.orderByChild("courtId").equalTo(training.getCourtId())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    android.util.Log.d("TrainingRepository", "Conflict check response received with " + dataSnapshot.getChildrenCount() + " trainings");
                    
                    boolean hasConflict = false;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Training existingTraining = snapshot.getValue(Training.class);
                        if (existingTraining != null && training.conflictsWith(existingTraining)) {
                            android.util.Log.w("TrainingRepository", "Conflict found with: " + existingTraining.getTeamName());
                            hasConflict = true;
                            break;
                        }
                    }

                    if (hasConflict) {
                        android.util.Log.w("TrainingRepository", "Training conflict detected");
                        listener.onConflict();
                    } else {
                        String key = trainingsRef.push().getKey();
                        if (key != null) {
                            training.setTrainingId(key);
                            android.util.Log.d("TrainingRepository", "No conflicts. Saving training with ID: " + key);
                            
                            trainingsRef.child(key).setValue(training)
                                .addOnSuccessListener(aVoid -> {
                                    android.util.Log.d("TrainingRepository", "Training saved successfully: " + key);
                                    listener.onSuccess();
                                })
                                .addOnFailureListener(e -> {
                                    android.util.Log.e("TrainingRepository", "Failed to save training: " + e.getMessage(), e);
                                    errorLiveData.setValue(e.getMessage());
                                    listener.onFailure(e.getMessage());
                                });
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    android.util.Log.e("TrainingRepository", "Conflict check cancelled: " + error.getMessage(), error.toException());
                    errorLiveData.setValue("שגיאת רשת: " + error.getMessage());
                    listener.onFailure("שגיאת רשת: " + error.getMessage());
                }
            });
    }

    public void updateTraining(Training training) {
        trainingsRef.child(training.getTrainingId()).setValue(training)
            .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void deleteTraining(String trainingId) {
        android.util.Log.d("TrainingRepository", "Deleting training with ID: " + trainingId);
        trainingsRef.child(trainingId).removeValue()
            .addOnSuccessListener(aVoid -> {
                android.util.Log.d("TrainingRepository", "Training deleted successfully: " + trainingId);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("TrainingRepository", "Failed to delete training: " + trainingId, e);
                errorLiveData.setValue(e.getMessage());
            });
    }

    public LiveData<List<Training>> getTrainings() {
        return trainingsLiveData;
    }

    public LiveData<List<Training>> getTrainingsByTeam(String teamId) {
        MutableLiveData<List<Training>> teamTrainings = new MutableLiveData<>();
        trainingsRef.orderByChild("teamId").equalTo(teamId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Training> trainings = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Training training = snapshot.getValue(Training.class);
                        if (training != null) {
                            trainings.add(training);
                        }
                    }
                    teamTrainings.setValue(trainings);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    errorLiveData.setValue(error.getMessage());
                }
            });
        return teamTrainings;
    }

    public LiveData<List<Training>> getTrainingsByCourt(String courtId) {
        MutableLiveData<List<Training>> courtTrainings = new MutableLiveData<>();
        trainingsRef.orderByChild("courtId").equalTo(courtId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Training> trainings = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Training training = snapshot.getValue(Training.class);
                        if (training != null) {
                            trainings.add(training);
                        }
                    }
                    courtTrainings.setValue(trainings);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    errorLiveData.setValue(error.getMessage());
                }
            });
        return courtTrainings;
    }

    public LiveData<String> getErrors() {
        return errorLiveData;
    }

    // Additional method for simple training addition (used by ScheduleGridFragment)
    public void addTraining(Training training, OnTrainingAddedListener listener) {
        Log.d(TAG, "OnTrainingAddedListener: Checking for conflicts for training: Team=" + training.getTeamId() + 
            ", Court=" + training.getCourtId() + ", Time=" + training.getStartTime() + "-" + training.getEndTime() +
            ", Date=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(training.getDate()));
        
        // Check for conflicts before adding
        trainingsRef.orderByChild("courtId").equalTo(training.getCourtId())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    long snapshotCount = dataSnapshot.getChildrenCount();
                    Log.d(TAG, "OnTrainingAddedListener: Conflict check response received with " + snapshotCount + " trainings");
                    
                    boolean hasConflict = false;
                    String conflictingTeamId = null;
                    String conflictingTime = null;
                    String conflictingDate = null;
                    
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Training existingTraining = snapshot.getValue(Training.class);
                        if (existingTraining != null) {
                            Log.d(TAG, "OnTrainingAddedListener: Checking existing training: Team=" + existingTraining.getTeamId() + 
                                ", Time=" + existingTraining.getStartTime() + "-" + existingTraining.getEndTime() +
                                ", Date=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(existingTraining.getDate()));
                            
                            if (training.conflictsWith(existingTraining)) {
                                hasConflict = true;
                                conflictingTeamId = existingTraining.getTeamId();
                                conflictingTime = existingTraining.getStartTime() + "-" + existingTraining.getEndTime();
                                conflictingDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(existingTraining.getDate());
                                Log.e(TAG, "OnTrainingAddedListener: CONFLICT found with: Team=" + conflictingTeamId + 
                                    ", Time=" + conflictingTime + ", Date=" + conflictingDate);
                                break;
                            }
                        }
                    }

                    if (hasConflict) {
                        String detailedError = "קיימת התנגשות עם אימון אחר (קבוצה: " + conflictingTeamId + 
                            ", זמן: " + conflictingTime + ", תאריך: " + conflictingDate + ")";
                        Log.e(TAG, "OnTrainingAddedListener: " + detailedError);
                        listener.onError(detailedError);
                    } else {
                        Log.d(TAG, "OnTrainingAddedListener: No conflicts. Saving training...");
                        String key = trainingsRef.push().getKey();
                        if (key != null) {
                            training.setTrainingId(key);
                            Log.d(TAG, "OnTrainingAddedListener: Saving training with ID: " + key);
                            trainingsRef.child(key).setValue(training)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "OnTrainingAddedListener: Training saved successfully: " + key);
                                    listener.onTrainingAdded(key);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "OnTrainingAddedListener: Failed to save training: " + e.getMessage());
                                    errorLiveData.setValue(e.getMessage());
                                    listener.onError(e.getMessage());
                                });
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e(TAG, "OnTrainingAddedListener: Database error: " + error.getMessage());
                    errorLiveData.setValue(error.getMessage());
                    listener.onError(error.getMessage());
                }
            });
    }

    public interface OnConflictCheckListener {
        void onSuccess();
        void onConflict();
        void onFailure(String error);
    }
    
    public interface OnTrainingAddedListener {
        void onTrainingAdded(String trainingId);
        void onError(String error);
    }
}
