package com.example.testapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.example.testapp.models.Player;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PlayerDetailsActivity extends AppCompatActivity {

    private static final String[] SHIRT_SIZES = {"8", "10", "12", "14", "16", "18", "S", "M", "L", "XL", "XXL", "XXXL"};

    private EditText firstNameEditText, lastNameEditText, gradeEditText, schoolEditText;
    private EditText playerPhoneEditText, parentPhoneEditText, idNumberEditText, birthDateEditText, jerseyNumberEditText;
    private Spinner shirtSizeSpinner;
    private Button saveButton;
    private ProgressBar progressBar;
    private DatabaseReference playersRef;
    private DatabaseReference usersRef;
    private String userId;
    private String playerId;
    private String teamId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_details);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        initializeViews();
        setupShirtSizeSpinner();
        
        playersRef = FirebaseDatabase.getInstance().getReference("players");
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        
        userId = getIntent().getStringExtra("userId");
        if (userId == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        playerId = getIntent().getStringExtra("playerId");
        teamId = getIntent().getStringExtra("teamId");
        
        setupDatePicker();
        loadPlayerData();
        
        saveButton.setOnClickListener(v -> savePlayerDetails());
    }

    private void setupDatePicker() {
        birthDateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                PlayerDetailsActivity.this,
                android.R.style.Theme_Material_Light_Dialog_Alert,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    birthDateEditText.setText(date);
                },
                year, month, day
            );
            datePickerDialog.show();
        });
    }

    private void initializeViews() {
        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        gradeEditText = findViewById(R.id.gradeEditText);
        schoolEditText = findViewById(R.id.schoolEditText);
        playerPhoneEditText = findViewById(R.id.playerPhoneEditText);
        parentPhoneEditText = findViewById(R.id.parentPhoneEditText);
        idNumberEditText = findViewById(R.id.idNumberEditText);
        birthDateEditText = findViewById(R.id.birthDateEditText);
        jerseyNumberEditText = findViewById(R.id.jerseyNumberEditText);
        shirtSizeSpinner = findViewById(R.id.shirtSizeSpinner);
        saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupShirtSizeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, SHIRT_SIZES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shirtSizeSpinner.setAdapter(adapter);
    }

    private void loadPlayerData() {
        progressBar.setVisibility(View.VISIBLE);
        
        if (userId != null && !userId.isEmpty()) {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        com.example.testapp.models.User user = snapshot.getValue(com.example.testapp.models.User.class);
                        if (user != null) {
                            String fullName = user.getName() != null ? user.getName() : "";
                            if (!fullName.isEmpty()) {
                                String[] nameParts = fullName.split(" ", 2);
                                if (nameParts.length >= 1 && !nameParts[0].isEmpty()) {
                                    firstNameEditText.setText(nameParts[0]);
                                }
                                if (nameParts.length >= 2 && !nameParts[1].isEmpty()) {
                                    lastNameEditText.setText(nameParts[1]);
                                }
                            }
                            
                            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                                playerPhoneEditText.setText(user.getPhone());
                            }
                            
                            String playerIdFromUser = user.getPlayerId();
                            
                            if (playerIdFromUser != null && !playerIdFromUser.isEmpty()) {
                                loadPlayerByPlayerId(playerIdFromUser);
                            } else {
                                loadPlayerSpecificData();
                            }
                        }
                    } else {
                        loadPlayerSpecificData();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    loadPlayerSpecificData();
                }
            });
        } else {
            loadPlayerSpecificData();
        }
    }
    
    private void loadPlayerByPlayerId(String playerIdFromUser) {
        playersRef.child(playerIdFromUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                
                if (snapshot.exists()) {
                    Player player = snapshot.getValue(Player.class);
                    if (player != null) {
                        // Update playerId from the Firebase key
                        playerId = playerIdFromUser;
                        populatePlayerFields(player);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PlayerDetailsActivity.this, "שגיאה בטעינת פרטי השחקן: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadPlayerSpecificData() {
        
        if (playerId != null && !playerId.isEmpty()) {
            loadPlayerByPlayerId(playerId);
        } else {
            loadPlayerByUserId();
        }
    }

    private void loadPlayerByUserId() {
        if (userId == null || userId.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        playersRef.orderByChild("userId").equalTo(userId).limitToFirst(1)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    progressBar.setVisibility(View.GONE);
                    if (snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Player player = child.getValue(Player.class);
                            if (player != null) {
                                // Update playerId from the Firebase key
                                playerId = child.getKey();
                                populatePlayerFields(player);
                                break;
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    progressBar.setVisibility(View.GONE);
                }
            });
    }
    
    private void populatePlayerFields(Player player) {
        if (TextUtils.isEmpty(firstNameEditText.getText().toString().trim()) && player.getFirstName() != null) {
            firstNameEditText.setText(player.getFirstName());
        }
        if (TextUtils.isEmpty(lastNameEditText.getText().toString().trim()) && player.getLastName() != null) {
            lastNameEditText.setText(player.getLastName());
        }

        gradeEditText.setText(player.getGrade() != null ? player.getGrade() : "");
        schoolEditText.setText(player.getSchool() != null ? player.getSchool() : "");
        
        if ((player.getPlayerPhone() != null && !player.getPlayerPhone().isEmpty()) && 
            TextUtils.isEmpty(playerPhoneEditText.getText().toString().trim())) {
            playerPhoneEditText.setText(player.getPlayerPhone());
        }
        
        parentPhoneEditText.setText(player.getParentPhone() != null ? player.getParentPhone() : "");
        idNumberEditText.setText(player.getIdNumber() != null ? player.getIdNumber() : "");
        birthDateEditText.setText(player.getBirthDate() != null ? player.getBirthDate() : "");
        jerseyNumberEditText.setText(player.getJerseyNumber() != null ? player.getJerseyNumber() : "");
        
        for (int i = 0; i < SHIRT_SIZES.length; i++) {
            if (SHIRT_SIZES[i].equals(player.getShirtSize())) {
                shirtSizeSpinner.setSelection(i);
                break;
            }
        }
    }

    private void savePlayerDetails() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String grade = gradeEditText.getText().toString().trim();
        String school = schoolEditText.getText().toString().trim();
        String playerPhone = playerPhoneEditText.getText().toString().trim();
        String parentPhone = parentPhoneEditText.getText().toString().trim();
        String idNumber = idNumberEditText.getText().toString().trim();
        String birthDate = birthDateEditText.getText().toString().trim();
        String jerseyNumber = jerseyNumberEditText.getText().toString().trim();
        String shirtSize = shirtSizeSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(firstName)) {
            firstNameEditText.setError("שדה חובה");
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            lastNameEditText.setError("שדה חובה");
            return;
        }
        
        // Safety check: ensure userId is not null
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "שגיאה: מזהה משתמש לא תקין", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        // Check jersey number availability across all of the player's teams
        if (!TextUtils.isEmpty(jerseyNumber)) {
            checkJerseyNumberInAllPlayerTeams(jerseyNumber, userId, isAvailable -> {
                if (!isAvailable) {
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                    Toast.makeText(PlayerDetailsActivity.this, "מספר גופיה זה כבר בשימוש באחת מהקבוצות שלך", Toast.LENGTH_LONG).show();
                    jerseyNumberEditText.setText("");
                    return;
                }
                proceedWithSave(firstName, lastName, grade, school, playerPhone, parentPhone, idNumber, birthDate, jerseyNumber, shirtSize);
            });
        } else {
            proceedWithSave(firstName, lastName, grade, school, playerPhone, parentPhone, idNumber, birthDate, jerseyNumber, shirtSize);
        }
    }
    
    private void proceedWithSave(String firstName, String lastName, String grade, String school, 
                                 String playerPhone, String parentPhone, String idNumber, 
                                 String birthDate, String jerseyNumber, String shirtSize) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                com.example.testapp.models.User user;
                
                if (snapshot.exists()) {
                    // User exists - update it
                    user = snapshot.getValue(com.example.testapp.models.User.class);
                    if (user != null) {
                        user.setName(firstName + " " + lastName);
                        user.setPhone(playerPhone);
                        usersRef.child(userId).setValue(user);
                    }
                } else {
                    // User doesn't exist - create it
                    user = new com.example.testapp.models.User();
                    user.setUserId(userId);
                    user.setName(firstName + " " + lastName);
                    user.setPhone(playerPhone);
                    user.setEmail(FirebaseAuth.getInstance().getCurrentUser() != null ? 
                                 FirebaseAuth.getInstance().getCurrentUser().getEmail() : "");
                    user.setRole("PLAYER");
                    user.setCreatedAt(System.currentTimeMillis());
                    usersRef.child(userId).setValue(user);
                }
                
                updateAllPlayerRecords(firstName, lastName, grade, school, playerPhone, parentPhone, idNumber, birthDate, jerseyNumber, shirtSize);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                Toast.makeText(PlayerDetailsActivity.this, "שגיאה בעדכון: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateAllPlayerRecords(String firstName, String lastName, String grade, String school, 
                                        String playerPhone, String parentPhone, String idNumber, 
                                        String birthDate, String jerseyNumber, String shirtSize) {
        playersRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                            Player player = playerSnapshot.getValue(Player.class);
                            if (player != null) {
                                player.setFirstName(firstName);
                                player.setLastName(lastName);
                                player.setGrade(grade);
                                player.setSchool(school);
                                player.setPlayerPhone(playerPhone);
                                player.setParentPhone(parentPhone);
                                player.setIdNumber(idNumber);
                                player.setBirthDate(birthDate);
                                player.setJerseyNumber(jerseyNumber != null ? jerseyNumber : "");
                                player.setShirtSize(shirtSize);
                                player.setUpdatedAt(System.currentTimeMillis());
                                
                                playersRef.child(playerSnapshot.getKey()).setValue(player);
                            }
                        }
                        
                        progressBar.setVisibility(View.GONE);
                        saveButton.setEnabled(true);
                        Toast.makeText(PlayerDetailsActivity.this, "הפרטים עודכנו בהצלחה בכל הקבוצות", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String newPlayerId = playersRef.push().getKey();
                        if (newPlayerId == null) {
                            progressBar.setVisibility(View.GONE);
                            saveButton.setEnabled(true);
                            Toast.makeText(PlayerDetailsActivity.this, "שגיאה ביצירת פרופיל שחקן", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Player player = new Player();
                        player.setPlayerId(newPlayerId);
                        player.setUserId(userId);
                        player.setFirstName(firstName);
                        player.setLastName(lastName);
                        player.setGrade(grade);
                        player.setSchool(school);
                        player.setPlayerPhone(playerPhone);
                        player.setParentPhone(parentPhone);
                        player.setIdNumber(idNumber);
                        player.setBirthDate(birthDate);
                        player.setJerseyNumber(jerseyNumber != null ? jerseyNumber : "");
                        player.setShirtSize(shirtSize);
                        player.setCreatedAt(System.currentTimeMillis());
                        player.setUpdatedAt(System.currentTimeMillis());

                        playersRef.child(newPlayerId).setValue(player)
                            .addOnSuccessListener(aVoid -> {
                                usersRef.child(userId).child("playerId").setValue(newPlayerId);
                                progressBar.setVisibility(View.GONE);
                                saveButton.setEnabled(true);
                                Toast.makeText(PlayerDetailsActivity.this, "הפרטים נשמרו", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                saveButton.setEnabled(true);
                                Toast.makeText(PlayerDetailsActivity.this, "שגיאה ביצירת פרופיל שחקן: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    progressBar.setVisibility(View.GONE);
                    saveButton.setEnabled(true);
                    Toast.makeText(PlayerDetailsActivity.this, "שגיאה בעדכון: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    /**
     * Verifies a jersey number is available across every team the player belongs to.
     * A player can join multiple teams, and each team must not have duplicate jersey numbers.
     */
    private void checkJerseyNumberInAllPlayerTeams(String jerseyNumber, String currentUserId,
                                                   OnJerseyCheckListener listener) {
        // Step 1: fetch the current player's team list
        usersRef.child(currentUserId).child("teamIds")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    List<String> playerTeamIds = new ArrayList<>();
                    
                    if (snapshot.exists()) {
                        for (DataSnapshot teamSnapshot : snapshot.getChildren()) {
                            String teamId = teamSnapshot.getValue(String.class);
                            if (teamId != null) {
                                playerTeamIds.add(teamId);
                            }
                        }
                    }
                    
                    // If the player is not in any team, the jersey number is always available
                    if (playerTeamIds.isEmpty()) {
                        listener.onResult(true);
                        return;
                    }
                    
                    // Step 2: check every other player in the system
                    playersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot playersSnapshot) {
                            // For each other player in the system
                            for (DataSnapshot playerSnapshot : playersSnapshot.getChildren()) {
                                Player otherPlayer = playerSnapshot.getValue(Player.class);
                                String otherPlayerKey = playerSnapshot.getKey();
                                
                                // Skip the current player
                                if (otherPlayer == null || 
                                    otherPlayerKey.equals(playerId) ||
                                    currentUserId.equals(otherPlayer.getUserId())) {
                                    continue;
                                }
                                
                                // Check whether the other player has the same jersey number
                                if (!jerseyNumber.equals(otherPlayer.getJerseyNumber())) {
                                    continue;
                                }
                                
                                // Step 3: verify whether the other player is in any of the current player's teams
                                String otherUserId = otherPlayer.getUserId();
                                if (otherUserId == null) continue;
                                
                                // Need to inspect the other player's teams
                                checkOtherPlayerTeams(otherUserId, playerTeamIds, listener);
                                return; // Found another player with the same number - now check their teams
                            }
                            
                            // No other player found with this jersey number
                            listener.onResult(true);
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            listener.onResult(true); // In case of error, assume available
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    listener.onResult(true);
                }
            });
    }
    
    /**
     * Checks whether another player with the same jersey number is in any of the current player's teams
     */
    private void checkOtherPlayerTeams(String otherUserId, List<String> currentPlayerTeamIds,
                                      OnJerseyCheckListener listener) {
        usersRef.child(otherUserId).child("teamIds")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot teamSnapshot : snapshot.getChildren()) {
                            String otherPlayerTeamId = teamSnapshot.getValue(String.class);
                            
                            // Check for overlap between the teams
                            if (otherPlayerTeamId != null && currentPlayerTeamIds.contains(otherPlayerTeamId)) {
                                // Overlap found: both players are in the same team with the same jersey number
                                listener.onResult(false);
                                return;
                            }
                        }
                    }
                    
                    // No overlap between teams - jersey number is available
                    listener.onResult(true);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    listener.onResult(true);
                }
            });
    }
    
    private interface OnJerseyCheckListener {
        void onResult(boolean isAvailable);
    }
}
