package com.example.testapp.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.testapp.models.Training;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TrainingRepository {
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
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Training training = snapshot.getValue(Training.class);
                    if (training != null) {
                        trainings.add(training);
                    }
                }
                trainingsLiveData.setValue(trainings);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                errorLiveData.setValue(error.getMessage());
            }
        });
    }

    public void addTraining(Training training, OnConflictCheckListener listener) {
        // Check for conflicts before adding
        trainingsRef.orderByChild("courtId").equalTo(training.getCourtId())
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    boolean hasConflict = false;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Training existingTraining = snapshot.getValue(Training.class);
                        if (existingTraining != null && training.conflictsWith(existingTraining)) {
                            hasConflict = true;
                            break;
                        }
                    }

                    if (hasConflict) {
                        listener.onConflict();
                    } else {
                        String key = trainingsRef.push().getKey();
                        if (key != null) {
                            training.setTrainingId(key);
                            trainingsRef.child(key).setValue(training)
                                .addOnSuccessListener(aVoid -> listener.onSuccess())
                                .addOnFailureListener(e -> {
                                    errorLiveData.setValue(e.getMessage());
                                    listener.onFailure(e.getMessage());
                                });
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    errorLiveData.setValue(error.getMessage());
                    listener.onFailure(error.getMessage());
                }
            });
    }

    public void updateTraining(Training training) {
        trainingsRef.child(training.getTrainingId()).setValue(training)
            .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void deleteTraining(String trainingId) {
        trainingsRef.child(trainingId).removeValue()
            .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
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

    public interface OnConflictCheckListener {
        void onSuccess();
        void onConflict();
        void onFailure(String error);
    }
}
