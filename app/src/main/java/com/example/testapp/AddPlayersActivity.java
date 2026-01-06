package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.adapters.AddPlayersAdapter;
import com.example.testapp.models.Player;
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

    private LinearLayout filterHeader;
    private NestedScrollView filtersScrollView;
    private ImageView expandCollapseIcon;
    private boolean isFiltersExpanded = true; // Start expanded
    private SearchView searchView;
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

        filterHeader = findViewById(R.id.filterHeader);
        filtersScrollView = findViewById(R.id.filtersScrollView);
        expandCollapseIcon = findViewById(R.id.expandCollapseIcon);
        searchView = findViewById(R.id.searchView);
        playersRecyclerView = findViewById(R.id.playersRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        confirmButton = findViewById(R.id.confirmButton);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        allPlayers = new ArrayList<>();
        filteredPlayers = new ArrayList<>();

        setupExpandCollapse();
        setupRecyclerView();
        loadPlayers();
        setupSearchListener();
        setupConfirmButton();
    }

    private void setupExpandCollapse() {
        filterHeader.setOnClickListener(v -> toggleFilters());
    }

    private void toggleFilters() {
        isFiltersExpanded = !isFiltersExpanded;
        
        if (isFiltersExpanded) {
            filtersScrollView.setVisibility(View.VISIBLE);
            expandCollapseIcon.setRotation(180);
            
            // Set max height to 35% of screen height
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            int maxHeight = (int) (screenHeight * 0.35);
            ViewGroup.LayoutParams params = filtersScrollView.getLayoutParams();
            params.height = maxHeight;
            filtersScrollView.setLayoutParams(params);
        } else {
            filtersScrollView.setVisibility(View.GONE);
            expandCollapseIcon.setRotation(0);
            
            ViewGroup.LayoutParams params = filtersScrollView.getLayoutParams();
            params.height = 0;
            filtersScrollView.setLayoutParams(params);
        }
    }

    private void setupRecyclerView() {
        playersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AddPlayersAdapter(filteredPlayers);
        playersRecyclerView.setAdapter(adapter);
    }

    private void setupSearchListener() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterPlayers(newText);
                return true;
            }
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
        
        // First, load the player's profile to get their jersey number
        DatabaseReference playersRef = FirebaseDatabase.getInstance().getReference("players");
        playersRef.orderByChild("userId").equalTo(player.getUserId()).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String playerJerseyNumber = null;
                
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        Player playerProfile = child.getValue(Player.class);
                        if (playerProfile != null) {
                            playerJerseyNumber = playerProfile.getJerseyNumber();
                        }
                        break;
                    }
                }
                
                // If player has a jersey number, check if another player in the team has the same number
                if (playerJerseyNumber != null && !playerJerseyNumber.isEmpty()) {
                    checkJerseyNumberAvailabilityBeforeAdding(playerJerseyNumber, player.getUserId(), player, onSuccess, onError);
                } else {
                    // No jersey number, proceed with adding to team
                    proceedWithAddingPlayerToTeam(player, onSuccess, onError);
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                onError.call();
            }
        });
    }
    
    private void checkJerseyNumberAvailabilityBeforeAdding(String jerseyNumber, String userId, User player, SimpleCallback onSuccess, SimpleCallback onError) {
        // First, get all players with this jersey number
        DatabaseReference playersRef = FirebaseDatabase.getInstance().getReference("players");
        playersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Player> playersWithSameNumber = new ArrayList<>();
                
                if (snapshot.exists()) {
                    for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                        Player existingPlayer = playerSnapshot.getValue(Player.class);
                        
                        // Collect all players with the same jersey number (except current user)
                        if (existingPlayer != null && 
                            jerseyNumber.equals(existingPlayer.getJerseyNumber()) &&
                            !userId.equals(existingPlayer.getUserId())) {
                            playersWithSameNumber.add(existingPlayer);
                        }
                    }
                }
                
                // Now check if any of these players are in the current team
                if (playersWithSameNumber.isEmpty()) {
                    // No one else has this jersey number, proceed with adding
                    proceedWithAddingPlayerToTeam(player, onSuccess, onError);
                } else {
                    // Check each player to see if they're in the team
                    final int[] checkCount = {0};
                    final boolean[] foundDuplicate = {false};
                    
                    for (Player otherPlayer : playersWithSameNumber) {
                        String otherUserId = otherPlayer.getUserId();
                        if (otherUserId != null && !otherUserId.isEmpty()) {
                            usersRef.child(otherUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot userSnapshot) {
                                    checkCount[0]++;
                                    
                                    if (!foundDuplicate[0]) {
                                        User otherUser = userSnapshot.getValue(User.class);
                                        if (otherUser != null && otherUser.getTeamIds() != null && 
                                            otherUser.getTeamIds().contains(teamId)) {
                                            // Found a duplicate in the same team!
                                            foundDuplicate[0] = true;
                                            Toast.makeText(AddPlayersActivity.this, "קיים שחקן אחר בקבוצה עם מספר גופיה זה - השחקן יתווסף ללא מספר גופיה", Toast.LENGTH_LONG).show();
                                            // Clear the jersey number and proceed with adding
                                            clearPlayerJerseyNumber(userId);
                                            proceedWithAddingPlayerToTeam(player, onSuccess, onError);
                                            return;
                                        }
                                    }
                                    
                                    // If all checks are done and no duplicate found, proceed
                                    if (checkCount[0] == playersWithSameNumber.size() && !foundDuplicate[0]) {
                                        proceedWithAddingPlayerToTeam(player, onSuccess, onError);
                                    }
                                }
                                
                                @Override
                                public void onCancelled(DatabaseError error) {
                                    checkCount[0]++;
                                    if (checkCount[0] == playersWithSameNumber.size() && !foundDuplicate[0]) {
                                        proceedWithAddingPlayerToTeam(player, onSuccess, onError);
                                    }
                                }
                            });
                        } else {
                            checkCount[0]++;
                        }
                    }
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                onError.call();
            }
        });
    }
    
    private void clearPlayerJerseyNumber(String userId) {
        // Clear the jersey number in the player's profile
        DatabaseReference playersRef = FirebaseDatabase.getInstance().getReference("players");
        playersRef.orderByChild("userId").equalTo(userId).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                        playerSnapshot.getRef().child("jerseyNumber").setValue("");
                        break;
                    }
                }
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                // Ignore errors when clearing jersey number
            }
        });
    }
    private void proceedWithAddingPlayerToTeam(User player, SimpleCallback onSuccess, SimpleCallback onError) {
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
