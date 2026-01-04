package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.adapters.AddPlayersAdapter;
import com.example.testapp.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPlayersActivity extends AppCompatActivity {

    private static final String TAG = "AddPlayersActivity";

    private EditText searchEditText;
    private RecyclerView playersRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private MaterialButton confirmButton;
    private AddPlayersAdapter adapter;
    private String teamId;
    private String teamName;
    private List<User> allPlayers;
    private List<User> filteredPlayers;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_players);

        teamId = getIntent().getStringExtra("teamId");
        teamName = getIntent().getStringExtra("teamName");

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("הוסף שחקנים ל" + teamName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        searchEditText = findViewById(R.id.searchEditText);
        playersRecyclerView = findViewById(R.id.playersRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        confirmButton = findViewById(R.id.confirmButton);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        allPlayers = new ArrayList<>();
        filteredPlayers = new ArrayList<>();

        setupRecyclerView();
        loadPlayers();
        setupSearchListener();
        setupConfirmButton();
    }

    private void setupRecyclerView() {
        playersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AddPlayersAdapter(filteredPlayers);
        playersRecyclerView.setAdapter(adapter);
    }

    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPlayers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterPlayers(String query) {
        filteredPlayers.clear();
        
        if (query.isEmpty()) {
            filteredPlayers.addAll(allPlayers);
        } else {
            String lowerQuery = query.toLowerCase();
            for (User player : allPlayers) {
                String playerName = (player.getName() != null ? player.getName() : "").toLowerCase();
                if (playerName.contains(lowerQuery)) {
                    filteredPlayers.add(player);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private void loadPlayers() {
        progressBar.setVisibility(View.VISIBLE);
        
        usersRef.orderByChild("role").equalTo("PLAYER").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                allPlayers.clear();
                
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        // Set userId from Firebase key
                        user.setUserId(userSnapshot.getKey());
                        
                        // Check if user is already in this team
                        List<String> userTeamIds = user.getTeamIds();
                        if (userTeamIds == null || !userTeamIds.contains(teamId)) {
                            allPlayers.add(user);
                        }
                    }
                }
                
                filteredPlayers.addAll(allPlayers);
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                updateEmptyView();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddPlayersActivity.this, "שגיאה בטעינת שחקנים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyView() {
        if (filteredPlayers.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            playersRecyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            playersRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void setupConfirmButton() {
        confirmButton.setOnClickListener(v -> {
            List<User> selectedPlayers = adapter.getSelectedPlayers();
            
            if (selectedPlayers.isEmpty()) {
                Toast.makeText(this, "בחר לפחות שחקן אחד", Toast.LENGTH_SHORT).show();
                return;
            }
            
            progressBar.setVisibility(View.VISIBLE);
            final int total = selectedPlayers.size();
            final int[] done = {0};
            
            for (User player : selectedPlayers) {
                addPlayerToTeam(player, () -> {
                    done[0]++;
                    if (done[0] == total) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AddPlayersActivity.this, total + " שחקנים נוספו", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }, () -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AddPlayersActivity.this, "שגיאה בהוספת שחקן", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private interface SimpleCallback {
        void call();
    }

    private void addPlayerToTeam(User player, SimpleCallback onSuccess, SimpleCallback onError) {
        if (player.getUserId() == null || player.getUserId().isEmpty()) {
            Toast.makeText(this, "שגיאה: מזהה משתמש חסר", Toast.LENGTH_SHORT).show();
            onError.call();
            return;
        }
        
        // Read current teamIds from Firebase to avoid overwriting existing data
        DatabaseReference playerRef = usersRef.child(player.getUserId());
        playerRef.child("teamIds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> teamIds = new ArrayList<>();
                
                // Load existing teamIds from Firebase
                if (snapshot.exists()) {
                    for (DataSnapshot teamSnapshot : snapshot.getChildren()) {
                        String tid = teamSnapshot.getValue(String.class);
                        if (tid != null) {
                            teamIds.add(tid);
                        }
                    }
                }
                
                // Add current team if not already in list
                if (!teamIds.contains(teamId)) {
                    teamIds.add(teamId);
                }
                
                // Use updateChildren instead of setValue for better permission handling
                Map<String, Object> updates = new HashMap<>();
                updates.put("teamIds", teamIds);
                
                playerRef.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        onSuccess.call();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AddPlayersActivity.this, "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        onError.call();
                    });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                onError.call();
            }
        });
    }
}
