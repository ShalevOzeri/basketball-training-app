package com.example.testapp.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.testapp.models.Team;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TeamRepository {
    private final DatabaseReference teamsRef;
    private final MutableLiveData<List<Team>> teamsLiveData;
    private final MutableLiveData<String> errorLiveData;

    public TeamRepository() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        teamsRef = database.getReference("teams");
        teamsLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        loadTeams();
    }

    private void loadTeams() {
        teamsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Team> teams = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Team team = snapshot.getValue(Team.class);
                    if (team != null) {
                        teams.add(team);
                    }
                }
                teamsLiveData.setValue(teams);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                errorLiveData.setValue(error.getMessage());
            }
        });
    }

    public void addTeam(Team team) {
        String key = teamsRef.push().getKey();
        if (key != null) {
            team.setTeamId(key);
            teamsRef.child(key).setValue(team)
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
        }
    }

    public void updateTeam(Team team) {
        teamsRef.child(team.getTeamId()).setValue(team)
            .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public void deleteTeam(String teamId) {
        teamsRef.child(teamId).removeValue()
            .addOnFailureListener(e -> errorLiveData.setValue(e.getMessage()));
    }

    public LiveData<List<Team>> getTeams() {
        return teamsLiveData;
    }

    public LiveData<String> getErrors() {
        return errorLiveData;
    }
}
