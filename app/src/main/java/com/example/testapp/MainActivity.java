package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.testapp.models.User;
import com.example.testapp.repository.UserRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private CardView courtsCard, teamsCard, scheduleCard, allCourtsCard, manageUsersCard, playerDetailsCard;
    private MaterialToolbar toolbar;
    private UserRepository userRepository;
    private User currentUser;
    private DatabaseReference settingsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        userRepository = new UserRepository();
        
        setupToolbar();
        loadCurrentUserAndCheckPermissions();
        setupCardClicks();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        courtsCard = findViewById(R.id.courtsCard);
        teamsCard = findViewById(R.id.teamsCard);
        scheduleCard = findViewById(R.id.scheduleCard);
        allCourtsCard = findViewById(R.id.allCourtsCard);
        manageUsersCard = findViewById(R.id.manageUsersCard);
        playerDetailsCard = findViewById(R.id.playerDetailsCard);
        settingsRef = FirebaseDatabase.getInstance().getReference("settings");
        
        // Hide cards by default - will show based on role
        if (manageUsersCard != null) {
            manageUsersCard.setVisibility(View.GONE);
        }
        if (courtsCard != null) {
            courtsCard.setVisibility(View.GONE);
        }
        if (teamsCard != null) {
            teamsCard.setVisibility(View.GONE);
        }
        if (playerDetailsCard != null) {
            playerDetailsCard.setVisibility(View.GONE);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("TIMEOUT");
        }
    }

    private void setupCardClicks() {
        courtsCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CourtsActivity.class);
            startActivity(intent);
        });

        teamsCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TeamsActivity.class);
            startActivity(intent);
        });

        scheduleCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScheduleActivity.class);
            startActivity(intent);
        });

        allCourtsCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AllCourtsViewActivity.class);
            startActivity(intent);
        });
        
        if (manageUsersCard != null) {
            manageUsersCard.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ManageUsersActivity.class);
                startActivity(intent);
            });
        }
        
        if (playerDetailsCard != null) {
            playerDetailsCard.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, PlayerDetailsActivity.class);
                startActivity(intent);
            });
        }
    }
    
    private void loadCurrentUserAndCheckPermissions() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                if (currentUser != null) {
                    // Update toolbar with user role
                    updateToolbarWithRole(currentUser);
                    
                    // Show features based on role
                    if (currentUser.isAdmin()) {
                        // Admin sees everything
                        if (manageUsersCard != null) manageUsersCard.setVisibility(View.VISIBLE);
                        if (courtsCard != null) courtsCard.setVisibility(View.VISIBLE);
                        if (teamsCard != null) teamsCard.setVisibility(View.VISIBLE);
                    } else if (currentUser.isCoordinator()) {
                        // Coordinator sees courts and teams management
                        if (courtsCard != null) courtsCard.setVisibility(View.VISIBLE);
                        if (teamsCard != null) teamsCard.setVisibility(View.VISIBLE);
                    } else if (currentUser.isCoach()) {
                        // Coach sees their teams
                        if (teamsCard != null) teamsCard.setVisibility(View.VISIBLE);
                    } else if (currentUser.isPlayer()) {
                        // Player sees player details card
                        if (playerDetailsCard != null) playerDetailsCard.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "שגיאה בטעינת נתוני משתמש", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void updateToolbarWithRole(User user) {
        String roleText = "";
        switch (user.getRole()) {
            case "ADMIN":
                roleText = "מנהל";
                break;
            case "COORDINATOR":
                roleText = "רכז";
                break;
            case "COACH":
                roleText = "מאמן";
                break;
            case "PLAYER":
                roleText = "שחקן";
                break;
        }
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("TIMEOUT • " + user.getName() + " • " + roleText);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        userRepository.logout();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

