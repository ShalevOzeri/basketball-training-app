package com.example.testapp.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.testapp.models.Court;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CourtRepository {
    private final DatabaseReference courtsRef;
    private final MutableLiveData<List<Court>> courtsLiveData;
    private final MutableLiveData<String> errorLiveData;

    public CourtRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        courtsRef = database.getReference("courts");
        courtsLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        loadCourts();
    }

    private void loadCourts() {
        courtsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Court> courts = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Court court = snapshot.getValue(Court.class);
                    if (court != null) {
                        courts.add(court);
                    }
                }
                courtsLiveData.setValue(courts);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                errorLiveData.setValue(error.getMessage());
            }
        });
    }

    public void addCourt(Court court) {
        String key = courtsRef.push().getKey();
        if (key != null) {
            court.setCourtId(key);
            courtsRef.child(key).setValue(court)
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
        }
    }

    public void updateCourt(Court court) {
        courtsRef.child(court.getCourtId()).setValue(court)
            .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }
    
    public void updateCourt(Court court, OnCourtUpdatedListener listener) {
        courtsRef.child(court.getCourtId()).setValue(court)
            .addOnSuccessListener(aVoid -> listener.onCourtUpdated())
            .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }
    
    public void getCourt(String courtId, OnCourtLoadedListener listener) {
        courtsRef.child(courtId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Court court = dataSnapshot.getValue(Court.class);
                if (court != null) {
                    listener.onCourtLoaded(court);
                } else {
                    listener.onError("Court not found");
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                listener.onError(error.getMessage());
            }
        });
    }

    public void deleteCourt(String courtId) {
        courtsRef.child(courtId).removeValue()
            .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public LiveData<List<Court>> getCourts() {
        return courtsLiveData;
    }

    public LiveData<String> getErrors() {
        return errorLiveData;
    }
    
    // Callback interfaces
    public interface OnCourtUpdatedListener {
        void onCourtUpdated();
        void onError(String error);
    }
    
    public interface OnCourtLoadedListener {
        void onCourtLoaded(Court court);
        void onError(String error);
    }
}
