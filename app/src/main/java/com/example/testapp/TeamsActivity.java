package com.example.testapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.adapters.TeamAdapter;
import com.example.testapp.models.Team;
import com.example.testapp.models.User;
import com.example.testapp.viewmodel.TeamViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TeamAdapter adapter;
    private TeamViewModel viewModel;
    private FloatingActionButton fab;
    private MaterialToolbar toolbar;
    private DatabaseReference usersRef;
    private DatabaseReference settingsRef;
    private List<User> coachList = new ArrayList<>();
    private String currentUserId;
    private String currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teams);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        setupFab();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.teamsRecyclerView);
        fab = findViewById(R.id.fab);
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        settingsRef = FirebaseDatabase.getInstance().getReference("settings");
        loadCoaches();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("קבוצות");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TeamAdapter(new TeamAdapter.OnTeamInteractionListener() {
            @Override
            public void onTeamClick(Team team) {
                // View team players
                showTeamPlayersActivity(team);
            }

            @Override
            public void onEditClick(Team team) {
                // Show team options (edit, delete)
                showTeamOptionsDialog(team);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TeamViewModel.class);
        
        // Get current user
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        // Get current user's role from Firebase
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    currentUserRole = user.getRole();
                    
                    // Pass role to adapter to hide edit button for coaches
                    adapter.setUserRole(currentUserRole);
                    
                    // Hide FAB for coaches
                    if ("COACH".equals(currentUserRole)) {
                        fab.setVisibility(View.GONE);
                        viewModel.filterByCoach(currentUserId);
                    } else {
                        // ADMIN or COORDINATOR see all teams and can manage
                        fab.setVisibility(View.VISIBLE);
                        viewModel.clearFilter();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(TeamsActivity.this, "שגיאה בטעינת תפקיד", Toast.LENGTH_SHORT).show();
            }
        });
        
        viewModel.getTeams().observe(this, teams -> {
            adapter.setTeams(teams);
        });

        viewModel.getErrors().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFab() {
        fab.setOnClickListener(v -> showAddTeamDialog());
    }
    
    private void loadCoaches() {
        // Load both COACH and COORDINATOR users who can be assigned to teams
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                coachList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setUserId(userSnapshot.getKey());
                        if ("COACH".equals(user.getRole()) || "COORDINATOR".equals(user.getRole())) {
                            coachList.add(user);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(TeamsActivity.this, "שגיאה בטעינת מאמנים ורכזים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddTeamDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("הוספת קבוצה חדשה");

        // Create layout
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Team name
        final android.widget.EditText nameInput = new android.widget.EditText(this);
        nameInput.setHint("שם הקבוצה");
        layout.addView(nameInput);

        // Grade (Class)
        final android.widget.Spinner gradeSpinner = new android.widget.Spinner(this);
        String[] grades = {"ו", "ז", "ח", "ט", "י", "יא", "יב", "בוגרים/בוגרות"};
        android.widget.ArrayAdapter<String> gradeAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, grades);
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gradeSpinner.setAdapter(gradeAdapter);
        layout.addView(gradeSpinner);

        // Color picker
        final android.widget.Spinner colorSpinner = new android.widget.Spinner(this);
        String[] colors = {"ירוק", "כחול", "אדום", "צהוב", "סגול", "כתום", "ורוד", "תכלת", "אפור", "שחור", "חום", "תורכיז"};
        String[] colorValues = {"#3DDC84", "#2196F3", "#F44336", "#FFEB3B", "#9C27B0", "#FF9800", "#E91E63", "#00BCD4", "#9E9E9E", "#212121", "#795548", "#00E5FF"};
        android.widget.ArrayAdapter<String> colorAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, colors);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(colorAdapter);
        layout.addView(colorSpinner);

        // Coach selection (Spinner with coaches from database)
        final android.widget.Spinner coachSpinner = new android.widget.Spinner(this);
        List<String> coachNames = new ArrayList<>();
        coachNames.add("ללא מאמן");
        for (User coach : coachList) {
            coachNames.add(coach.getName());
        }
        android.widget.ArrayAdapter<String> coachAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, coachNames);
        coachAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        coachSpinner.setAdapter(coachAdapter);
        layout.addView(coachSpinner);

        builder.setView(layout);

        builder.setPositiveButton("הוסף", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String grade = gradeSpinner.getSelectedItem().toString();
            String color = colorValues[colorSpinner.getSelectedItemPosition()];
            int selectedCoachPosition = coachSpinner.getSelectedItemPosition();
            
            String coachId = "";
            String coachName = "ללא מאמן";
            
            if (selectedCoachPosition > 0 && selectedCoachPosition <= coachList.size()) {
                User selectedCoach = coachList.get(selectedCoachPosition - 1);
                coachId = selectedCoach.getUserId();
                coachName = selectedCoach.getName();
                
                // Update coach's teamId
                usersRef.child(coachId).child("teamId").setValue(name);
            }

            if (name.isEmpty()) {
                Toast.makeText(this, "יש להזין שם קבוצה", Toast.LENGTH_SHORT).show();
                return;
            }

            com.example.testapp.models.Team team = new com.example.testapp.models.Team(
                null, name, grade, "", coachId, coachName, color
            );

            viewModel.addTeam(team);
            Toast.makeText(this, "קבוצה נוספה: " + name, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("ביטול", null);
        builder.show();
    }

    private void showTeamPlayersActivity(Team team) {
        android.content.Intent intent = new android.content.Intent(this, TeamPlayersActivity.class);
        intent.putExtra("teamId", team.getTeamId());
        intent.putExtra("teamName", team.getName());
        intent.putExtra("team", team);
        startActivity(intent);
    }

    private void showTeamOptionsDialog(Team team) {
        // Only allow editing for ADMIN and COORDINATOR
        if ("COACH".equals(currentUserRole)) {
            Toast.makeText(this, "אין הרשאה לערוך קבוצה", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] options = {"ערוך פרטי קבוצה", "ערוך מאמן", "מחק קבוצה"};
        new AlertDialog.Builder(this)
            .setTitle(team.getName())
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // Edit team details
                    showEditTeamDialog(team);
                } else if (which == 1) {
                    // Edit coach
                    showEditCoachDialog(team);
                } else {
                    // Delete
                    confirmDelete(team);
                }
            })
            .show();
    }
    
    private void showEditTeamDialog(Team team) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("עריכת " + team.getName());

        // Create layout
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // Team name
        final android.widget.EditText nameInput = new android.widget.EditText(this);
        nameInput.setHint("שם הקבוצה");
        nameInput.setText(team.getName());
        layout.addView(nameInput);

        // Grade (Class)
        final android.widget.Spinner gradeSpinner = new android.widget.Spinner(this);
        String[] grades = {"ו", "ז", "ח", "ט", "י", "יא", "יב", "בוגרים/בוגרות"};
        android.widget.ArrayAdapter<String> gradeAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, grades);
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gradeSpinner.setAdapter(gradeAdapter);
        
        // Set current grade
        for (int i = 0; i < grades.length; i++) {
            if (grades[i].equals(team.getAgeGroup())) {
                gradeSpinner.setSelection(i);
                break;
            }
        }
        layout.addView(gradeSpinner);

        // Color picker
        final android.widget.Spinner colorSpinner = new android.widget.Spinner(this);
        String[] colors = {"ירוק", "כחול", "אדום", "צהוב", "סגול", "כתום", "ורוד", "תכלת", "אפור", "שחור", "חום", "תורכיז"};
        String[] colorValues = {"#3DDC84", "#2196F3", "#F44336", "#FFEB3B", "#9C27B0", "#FF9800", "#E91E63", "#00BCD4", "#9E9E9E", "#212121", "#795548", "#00E5FF"};
        android.widget.ArrayAdapter<String> colorAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, colors);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(colorAdapter);
        
        // Set current color
        for (int i = 0; i < colorValues.length; i++) {
            if (colorValues[i].equals(team.getColor())) {
                colorSpinner.setSelection(i);
                break;
            }
        }
        layout.addView(colorSpinner);

        builder.setView(layout);
        builder.setPositiveButton("שמור", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String grade = gradeSpinner.getSelectedItem().toString();
            String color = colorValues[colorSpinner.getSelectedItemPosition()];

            if (name.isEmpty()) {
                Toast.makeText(TeamsActivity.this, "נא להזין שם קבוצה", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update team
            team.setName(name);
            team.setAgeGroup(grade);
            team.setColor(color);
            team.setUpdatedAt(System.currentTimeMillis());

            viewModel.updateTeam(team);
            Toast.makeText(TeamsActivity.this, "הקבוצה עודכנה בהצלחה", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("ביטול", null);
        builder.show();
    }
    
    private void showEditCoachDialog(Team team) {
        List<String> coachNames = new ArrayList<>();
        coachNames.add("ללא מאמן");
        for (User coach : coachList) {
            coachNames.add(coach.getName());
        }
        
        String[] coachArray = coachNames.toArray(new String[0]);
        
        new AlertDialog.Builder(this)
            .setTitle("בחר מאמן עבור " + team.getName())
            .setItems(coachArray, (dialog, which) -> {
                String newCoachId = "";
                String newCoachName = "ללא מאמן";
                
                // Remove old coach's teamId if exists
                if (team.getCoachId() != null && !team.getCoachId().isEmpty()) {
                    usersRef.child(team.getCoachId()).child("teamId").setValue(null);
                }
                
                if (which > 0 && which <= coachList.size()) {
                    User selectedCoach = coachList.get(which - 1);
                    newCoachId = selectedCoach.getUserId();
                    newCoachName = selectedCoach.getName();
                    
                    // Set new coach's teamId
                    usersRef.child(newCoachId).child("teamId").setValue(team.getTeamId());
                }
                
                // Update team
                DatabaseReference teamsRef = FirebaseDatabase.getInstance().getReference("teams");
                teamsRef.child(team.getTeamId()).child("coachId").setValue(newCoachId);
                teamsRef.child(team.getTeamId()).child("coachName").setValue(newCoachName);
                
                Toast.makeText(this, "מאמן עודכן בהצלחה", Toast.LENGTH_SHORT).show();
            })
            .show();
    }
    

    private void confirmDelete(Team team) {
        new AlertDialog.Builder(this)
            .setTitle("מחיקת קבוצה")
            .setMessage("האם אתה בטוח שברצונך למחוק את " + team.getName() + "?")
            .setPositiveButton("מחק", (dialog, which) -> {
                // Remove teamId from all users in this team
                usersRef.orderByChild("teamId").equalTo(team.getTeamId())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                userSnapshot.getRef().child("teamId").setValue(null);
                            }
                            viewModel.deleteTeam(team.getTeamId());
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(TeamsActivity.this, "שגיאה במחיקת קבוצה", Toast.LENGTH_SHORT).show();
                        }
                    });
            })
            .setNegativeButton("ביטול", null)
            .show();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
