package com.example.testapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

import com.example.testapp.models.Player;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PlayerDetailsActivity extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, gradeEditText, schoolEditText;
    private EditText playerPhoneEditText, parentPhoneEditText, idNumberEditText, birthDateEditText;
    private Spinner shirtSizeSpinner;
    private Button saveButton;
    private ProgressBar progressBar;
    private DatabaseReference playersRef;
    private DatabaseReference usersRef;
    private String userId;
    private String playerId;  // Add playerId field

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
        
        // Get userId from intent, or use current logged-in user
        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        playerId = getIntent().getStringExtra("playerId");
        
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
        shirtSizeSpinner = findViewById(R.id.shirtSizeSpinner);
        saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupShirtSizeSpinner() {
        String[] sizes = {"8", "10", "12", "14", "16", "18", "S", "M", "L", "XL", "XXL", "XXXL"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sizes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shirtSizeSpinner.setAdapter(adapter);
    }

    private void loadPlayerData() {
        progressBar.setVisibility(View.VISIBLE);
        android.util.Log.d("PlayerDetailsActivity", "Loading player data for userId: " + userId);
        
        // First check if we can load User data
        if (userId != null && !userId.isEmpty()) {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    android.util.Log.d("PlayerDetailsActivity", "User data snapshot received, exists: " + snapshot.exists());
                    if (snapshot.exists()) {
                        com.example.testapp.models.User user = snapshot.getValue(com.example.testapp.models.User.class);
                        if (user != null) {
                            android.util.Log.d("PlayerDetailsActivity", "User loaded: " + user.getName());
                            // Pre-fill name and phone from User data
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
                            
                            // Get playerId from User record
                            String playerIdFromUser = user.getPlayerId();
                            android.util.Log.d("PlayerDetailsActivity", "PlayerId from user: " + playerIdFromUser);
                            
                            // Load player data using playerId from User
                            if (playerIdFromUser != null && !playerIdFromUser.isEmpty()) {
                                loadPlayerByPlayerId(playerIdFromUser);
                            } else {
                                android.util.Log.d("PlayerDetailsActivity", "No playerId in user record");
                                loadPlayerSpecificData();
                            }
                        }
                    } else {
                        android.util.Log.d("PlayerDetailsActivity", "User snapshot doesn't exist for: " + userId);
                        loadPlayerSpecificData();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    android.util.Log.e("PlayerDetailsActivity", "Error loading user data: " + error.getCode() + " - " + error.getMessage());
                    // Continue to load player data even if user load fails
                    loadPlayerSpecificData();
                }
            });
        } else {
            android.util.Log.d("PlayerDetailsActivity", "userId is null or empty, proceeding to load player data");
            loadPlayerSpecificData();
        }
    }
    
    private void loadPlayerByPlayerId(String playerIdFromUser) {
        android.util.Log.d("PlayerDetailsActivity", "Loading player by playerId from user: " + playerIdFromUser);
        playersRef.child(playerIdFromUser).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                android.util.Log.d("PlayerDetailsActivity", "Player data loaded by playerId, exists: " + snapshot.exists());
                progressBar.setVisibility(View.GONE);
                
                if (snapshot.exists()) {
                    Player player = snapshot.getValue(Player.class);
                    if (player != null) {
                        populatePlayerFields(player);
                    }
                } else {
                    android.util.Log.d("PlayerDetailsActivity", "No player found with playerId: " + playerIdFromUser);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                android.util.Log.e("PlayerDetailsActivity", "Error loading player by playerId: " + error.getMessage());
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PlayerDetailsActivity.this, "שגיאה בטעינת פרטי השחקן: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadPlayerSpecificData() {
        android.util.Log.d("PlayerDetailsActivity", "Loading player-specific data, playerId from intent: " + playerId);
        
        if (playerId != null && !playerId.isEmpty()) {
            loadPlayerByPlayerId(playerId);
        } else {
            // Fallback: try by userId (requires players index on userId in rules)
            loadPlayerByUserId();
        }
    }

    private void loadPlayerByUserId() {
        if (userId == null || userId.isEmpty()) {
            android.util.Log.d("PlayerDetailsActivity", "Cannot load by userId: userId empty");
            progressBar.setVisibility(View.GONE);
            return;
        }

        android.util.Log.d("PlayerDetailsActivity", "Fallback load player by userId: " + userId);
        playersRef.orderByChild("userId").equalTo(userId).limitToFirst(1)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    progressBar.setVisibility(View.GONE);
                    if (snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Player player = child.getValue(Player.class);
                            if (player != null) {
                                android.util.Log.d("PlayerDetailsActivity", "Loaded player by userId: " + player.getPlayerId());
                                populatePlayerFields(player);
                                break;
                            }
                        }
                    } else {
                        android.util.Log.d("PlayerDetailsActivity", "No player found for userId fallback");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    progressBar.setVisibility(View.GONE);
                    android.util.Log.e("PlayerDetailsActivity", "Error loading player by userId: " + error.getMessage());
                }
            });
    }
    
    private void populatePlayerFields(Player player) {
        // Names: fill if empty
        if (TextUtils.isEmpty(firstNameEditText.getText().toString().trim()) && player.getFirstName() != null) {
            firstNameEditText.setText(player.getFirstName());
        }
        if (TextUtils.isEmpty(lastNameEditText.getText().toString().trim()) && player.getLastName() != null) {
            lastNameEditText.setText(player.getLastName());
        }

        gradeEditText.setText(player.getGrade() != null ? player.getGrade() : "");
        schoolEditText.setText(player.getSchool() != null ? player.getSchool() : "");
        
        // If playerPhone is set in Player record and playerPhoneEditText is empty, use it
        if ((player.getPlayerPhone() != null && !player.getPlayerPhone().isEmpty()) && 
            TextUtils.isEmpty(playerPhoneEditText.getText().toString().trim())) {
            playerPhoneEditText.setText(player.getPlayerPhone());
        }
        
        parentPhoneEditText.setText(player.getParentPhone() != null ? player.getParentPhone() : "");
        idNumberEditText.setText(player.getIdNumber() != null ? player.getIdNumber() : "");
        birthDateEditText.setText(player.getBirthDate() != null ? player.getBirthDate() : "");
        
        // Set shirt size
        String[] sizes = {"8", "10", "12", "14", "16", "18", "S", "M", "L", "XL", "XXL", "XXXL"};
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i].equals(player.getShirtSize())) {
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
        String shirtSize = shirtSizeSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(firstName)) {
            firstNameEditText.setError("שדה חובה");
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            lastNameEditText.setError("שדה חובה");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        // Update User record with name and phone
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    com.example.testapp.models.User user = snapshot.getValue(com.example.testapp.models.User.class);
                    if (user != null) {
                        user.setName(firstName + " " + lastName);
                        user.setPhone(playerPhone);
                        usersRef.child(userId).setValue(user);
                    }
                }
                
                // Then update all player records
                updateAllPlayerRecords(firstName, lastName, grade, school, playerPhone, parentPhone, idNumber, birthDate, shirtSize);
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
                                        String birthDate, String shirtSize) {
        // Update ALL player records for this user across all teams
        playersRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Update each player record in each team
                        for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                            Player player = playerSnapshot.getValue(Player.class);
                            if (player != null) {
                                // Update all personal details
                                player.setFirstName(firstName);
                                player.setLastName(lastName);
                                player.setGrade(grade);
                                player.setSchool(school);
                                player.setPlayerPhone(playerPhone);
                                player.setParentPhone(parentPhone);
                                player.setIdNumber(idNumber);
                                player.setBirthDate(birthDate);
                                player.setShirtSize(shirtSize);
                                player.setUpdatedAt(System.currentTimeMillis());
                                
                                // Save updated player (preserves teamId)
                                playersRef.child(playerSnapshot.getKey()).setValue(player);
                            }
                        }
                        
                        progressBar.setVisibility(View.GONE);
                        saveButton.setEnabled(true);
                        Toast.makeText(PlayerDetailsActivity.this, "הפרטים עודכנו בהצלחה בכל הקבוצות", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        // No player record yet: create one primary record and link to user
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
                        player.setShirtSize(shirtSize);
                        player.setCreatedAt(System.currentTimeMillis());
                        player.setUpdatedAt(System.currentTimeMillis());

                        playersRef.child(newPlayerId).setValue(player)
                            .addOnSuccessListener(aVoid -> {
                                // Link user to this playerId
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
}
