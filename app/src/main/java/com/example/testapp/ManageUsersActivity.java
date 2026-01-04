package com.example.testapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.models.User;
import com.example.testapp.repository.UserRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    private RecyclerView usersRecyclerView;
    private ProgressBar progressBar;
    private Spinner roleFilterSpinner, teamFilterSpinner;
    private DatabaseReference usersRef;
    private UserRepository userRepository;
    private UsersAdapter adapter;
    private List<User> usersList;
    private List<User> filteredUsersList;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        roleFilterSpinner = findViewById(R.id.roleFilterSpinner);
        teamFilterSpinner = findViewById(R.id.teamFilterSpinner);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        userRepository = new UserRepository();
        usersList = new ArrayList<>();
        filteredUsersList = new ArrayList<>();

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsersAdapter(filteredUsersList, this::showChangeRoleDialog, this::showAssignTeamDialog, this::confirmDeleteUser);
        usersRecyclerView.setAdapter(adapter);
        
        setupFilters();

        // Check if current user is admin
        checkAdminAccess();
    }
    
    private void setupFilters() {
        // Setup role filter
        String[] roles = {"הכל", "שחקן", "מאמן", "רכז", "מנהל"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleFilterSpinner.setAdapter(roleAdapter);
        
        roleFilterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        
        // Setup team filter - will be populated after loading teams
        loadTeamsForFilter();
    }
    
    private void loadTeamsForFilter() {
        DatabaseReference teamsRef = FirebaseDatabase.getInstance().getReference("teams");
        teamsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> teamNames = new ArrayList<>();
                List<String> teamIds = new ArrayList<>();
                
                teamNames.add("הכל");
                teamIds.add("");
                
                teamNames.add("ללא קבוצה");
                teamIds.add("NO_TEAM");
                
                for (DataSnapshot teamSnapshot : snapshot.getChildren()) {
                    com.example.testapp.models.Team team = teamSnapshot.getValue(com.example.testapp.models.Team.class);
                    if (team != null) {
                        teamNames.add(team.getName());
                        teamIds.add(team.getTeamId());
                    }
                }
                
                ArrayAdapter<String> teamAdapter = new ArrayAdapter<>(ManageUsersActivity.this, 
                    android.R.layout.simple_spinner_item, teamNames);
                teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                teamFilterSpinner.setAdapter(teamAdapter);
                teamFilterSpinner.setTag(teamIds); // Store team IDs
                
                teamFilterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                        applyFilters();
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ManageUsersActivity.this, "שגיאה בטעינת קבוצות", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void applyFilters() {
        if (usersList == null || usersList.isEmpty()) {
            return;
        }
        
        filteredUsersList.clear();
        
        int rolePosition = roleFilterSpinner.getSelectedItemPosition();
        String selectedRole = null;
        switch (rolePosition) {
            case 1: selectedRole = "PLAYER"; break;
            case 2: selectedRole = "COACH"; break;
            case 3: selectedRole = "COORDINATOR"; break;
            case 4: selectedRole = "ADMIN"; break;
        }
        
        int teamPosition = teamFilterSpinner.getSelectedItemPosition();
        String selectedTeamId = null;
        if (teamFilterSpinner.getTag() != null && teamPosition > 0) {
            List<String> teamIds = (List<String>) teamFilterSpinner.getTag();
            if (teamPosition < teamIds.size()) {
                selectedTeamId = teamIds.get(teamPosition);
            }
        }
        
        for (User user : usersList) {
            boolean matchesRole = (selectedRole == null) || selectedRole.equals(user.getRole());
            boolean matchesTeam = true;
            
            if (selectedTeamId != null) {
                if ("NO_TEAM".equals(selectedTeamId)) {
                    matchesTeam = (user.getTeamId() == null || user.getTeamId().isEmpty());
                } else {
                    matchesTeam = selectedTeamId.equals(user.getTeamId());
                }
            }
            
            if (matchesRole && matchesTeam) {
                filteredUsersList.add(user);
            }
        }
        
        adapter.notifyDataSetChanged();
    }

    private void checkAdminAccess() {
        progressBar.setVisibility(View.VISIBLE);
        
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "נדרש להתחבר למערכת", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                if (currentUser != null && currentUser.isAdmin()) {
                    loadUsers();
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageUsersActivity.this, "אין לך הרשאה לגשת למסך זה", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, "שגיאה: " + error.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        usersList.add(user);
                    }
                }
                applyFilters(); // Apply filters after loading users
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, "שגיאה בטעינת משתמשים: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showChangeRoleDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_role, null);
        builder.setView(dialogView);
        
        TextView userNameText = dialogView.findViewById(R.id.userNameText);
        TextView userEmailText = dialogView.findViewById(R.id.userEmailText);
        Spinner roleSpinner = dialogView.findViewById(R.id.roleSpinner);
        
        userNameText.setText(user.getName());
        userEmailText.setText(user.getEmail());
        
        String[] rolesDisplay = {"שחקן (PLAYER)", "מאמן (COACH)", "רכז (COORDINATOR)", "מנהל (ADMIN)"};
        String[] rolesValues = {"PLAYER", "COACH", "COORDINATOR", "ADMIN"};
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rolesDisplay);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);
        
        // Set current role
        int position = 0;
        switch (user.getRole()) {
            case "PLAYER": position = 0; break;
            case "COACH": position = 1; break;
            case "COORDINATOR": position = 2; break;
            case "ADMIN": position = 3; break;
            default: position = 1; break;
        }
        roleSpinner.setSelection(position);
        
        builder.setTitle("שינוי הרשאות משתמש");
        builder.setPositiveButton("שמור", (dialog, which) -> {
            String newRole = rolesValues[roleSpinner.getSelectedItemPosition()];
            updateUserRole(user, newRole);
        });
        builder.setNegativeButton("ביטול", null);
        
        builder.create().show();
    }
    
    private void showAssignTeamDialog(User user) {
        progressBar.setVisibility(View.VISIBLE);
        
        // Load teams from database
        DatabaseReference teamsRef = FirebaseDatabase.getInstance().getReference("teams");
        teamsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                
                List<String> teamNames = new ArrayList<>();
                List<String> teamIds = new ArrayList<>();
                
                teamNames.add("אין קבוצה");
                teamIds.add("");
                
                for (DataSnapshot teamSnapshot : snapshot.getChildren()) {
                    com.example.testapp.models.Team team = teamSnapshot.getValue(com.example.testapp.models.Team.class);
                    if (team != null) {
                        teamNames.add(team.getName() + " (" + team.getAgeGroup() + ")");
                        teamIds.add(team.getTeamId());
                    }
                }
                
                if (teamNames.size() == 1) {
                    Toast.makeText(ManageUsersActivity.this, "אין קבוצות במערכת", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                String[] teamArray = teamNames.toArray(new String[0]);
                
                new AlertDialog.Builder(ManageUsersActivity.this)
                    .setTitle("בחר קבוצה עבור " + user.getName())
                    .setItems(teamArray, (dialog, which) -> {
                        String selectedTeamId = teamIds.get(which);
                        
                        // Update user's teamId
                        usersRef.child(user.getUserId()).child("teamId").setValue(selectedTeamId.isEmpty() ? null : selectedTeamId)
                            .addOnSuccessListener(aVoid -> {
                                if (selectedTeamId.isEmpty()) {
                                    Toast.makeText(ManageUsersActivity.this, user.getName() + " הוסר מהקבוצה", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ManageUsersActivity.this, user.getName() + " חובר לקבוצה " + teamArray[which], Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ManageUsersActivity.this, "שגיאה בעדכון קבוצה", Toast.LENGTH_SHORT).show();
                            });
                    })
                    .show();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, "שגיאה בטעינת קבוצות", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUserRole(User user, String newRole) {
        progressBar.setVisibility(View.VISIBLE);
        usersRef.child(user.getUserId()).child("role").setValue(newRole)
            .addOnSuccessListener(aVoid -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, "הרשאות עודכנו בהצלחה", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, "שגיאה בעדכון: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }
    
    private void confirmDeleteUser(User user) {
        new AlertDialog.Builder(this)
            .setTitle("מחיקת משתמש")
            .setMessage("האם אתה בטוח שברצונך למחוק את " + user.getName() + "?\n\nפעולה זו תמחק את המשתמש לצמיתות מהמערכת.")
            .setPositiveButton("מחק", (dialog, which) -> deleteUser(user))
            .setNegativeButton("ביטול", null)
            .show();
    }
    
    private void deleteUser(User user) {
        progressBar.setVisibility(View.VISIBLE);
        
        // Delete user from database
        usersRef.child(user.getUserId()).removeValue()
            .addOnSuccessListener(aVoid -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, 
                    user.getName() + " נמחק מהמערכת", Toast.LENGTH_SHORT).show();
                
                // Note: Firebase Auth user deletion requires re-authentication
                // For full deletion, the user would need to delete their own account
                // or use Admin SDK on server side
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, 
                    "שגיאה במחיקת משתמש: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });
    }

    // Adapter for users list
    private static class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
        private List<User> users;
        private OnUserClickListener listener;
        private OnAssignTeamClickListener assignTeamListener;
        private OnDeleteUserClickListener deleteUserListener;

        interface OnUserClickListener {
            void onUserClick(User user);
        }
        
        interface OnAssignTeamClickListener {
            void onAssignTeamClick(User user);
        }
        
        interface OnDeleteUserClickListener {
            void onDeleteUserClick(User user);
        }

        UsersAdapter(List<User> users, OnUserClickListener listener, OnAssignTeamClickListener assignTeamListener, OnDeleteUserClickListener deleteUserListener) {
            this.users = users;
            this.listener = listener;
            this.assignTeamListener = assignTeamListener;
            this.deleteUserListener = deleteUserListener;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            User user = users.get(position);
            holder.bind(user, listener, assignTeamListener, deleteUserListener);
        }

        @Override
        public int getItemCount() {
            return users.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView userName, userEmail, userRole, userTeam;
            Button changeRoleButton, assignTeamButton, deleteUserButton;

            ViewHolder(View itemView) {
                super(itemView);
                userName = itemView.findViewById(R.id.userName);
                userEmail = itemView.findViewById(R.id.userEmail);
                userRole = itemView.findViewById(R.id.userRole);
                userTeam = itemView.findViewById(R.id.userTeam);
                changeRoleButton = itemView.findViewById(R.id.changeRoleButton);
                assignTeamButton = itemView.findViewById(R.id.assignTeamButton);
                deleteUserButton = itemView.findViewById(R.id.deleteUserButton);
            }

            void bind(User user, OnUserClickListener listener, OnAssignTeamClickListener assignTeamListener, OnDeleteUserClickListener deleteUserListener) {
                userName.setText(user.getName());
                userEmail.setText(user.getEmail());
                
                String roleText = "";
                switch (user.getRole()) {
                    case "ADMIN": roleText = "מנהל"; break;
                    case "COORDINATOR": roleText = "רכז"; break;
                    case "COACH": roleText = "מאמן"; break;
                    case "PLAYER": roleText = "שחקן"; break;
                }
                userRole.setText(roleText);
                
                // Show team info
                if (user.getTeamId() != null && !user.getTeamId().isEmpty()) {
                    userTeam.setText("קבוצה: " + user.getTeamId());
                } else {
                    userTeam.setText("קבוצה: אין");
                }
                
                changeRoleButton.setOnClickListener(v -> listener.onUserClick(user));
                assignTeamButton.setOnClickListener(v -> assignTeamListener.onAssignTeamClick(user));
                deleteUserButton.setOnClickListener(v -> deleteUserListener.onDeleteUserClick(user));
                
                // Show assign team button only for COACH and PLAYER
                if ("COACH".equals(user.getRole()) || "PLAYER".equals(user.getRole())) {
                    assignTeamButton.setVisibility(View.VISIBLE);
                } else {
                    assignTeamButton.setVisibility(View.GONE);
                }
            }
        }
    }
}
