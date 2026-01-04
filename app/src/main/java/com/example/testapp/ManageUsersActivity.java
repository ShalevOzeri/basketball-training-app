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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.models.Team;
import com.example.testapp.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ManageUsersActivity extends AppCompatActivity {

    private RecyclerView usersRecyclerView;
    private ProgressBar progressBar;
    private Spinner roleFilterSpinner, teamFilterSpinner;
    private DatabaseReference usersRef;
    private UsersAdapter adapter;
    private List<User> usersList;
    private List<User> filteredUsersList;
    private User currentUser;
    private Map<String, String> teamsMap;

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
        usersList = new ArrayList<>();
        filteredUsersList = new ArrayList<>();
        teamsMap = new HashMap<>();

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UsersAdapter(filteredUsersList, teamsMap, this::showChangeRoleDialog, this::showAssignTeamDialog, this::confirmDeleteUser);
        usersRecyclerView.setAdapter(adapter);
        
        setupFilters();

        checkAdminAccess();
    }
    
    private void setupFilters() {
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
    }
    
    private void loadTeamsData() {
        DatabaseReference teamsRef = FirebaseDatabase.getInstance().getReference("teams");
        teamsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                teamsMap.clear();
                List<String> teamNames = new ArrayList<>();
                List<String> teamIds = new ArrayList<>();
                
                teamNames.add("הכל");
                teamIds.add("");
                
                teamNames.add("ללא קבוצה");
                teamIds.add("NO_TEAM");
                
                for (DataSnapshot teamSnapshot : snapshot.getChildren()) {
                    Team team = teamSnapshot.getValue(Team.class);
                    if (team != null) {
                        teamNames.add(team.getName());
                        teamIds.add(team.getTeamId());
                        teamsMap.put(team.getTeamId(), team.getName());
                    }
                }
                
                ArrayAdapter<String> teamAdapter = new ArrayAdapter<>(ManageUsersActivity.this, 
                    android.R.layout.simple_spinner_item, teamNames);
                teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                teamFilterSpinner.setAdapter(teamAdapter);
                teamFilterSpinner.setTag(teamIds);
                
                teamFilterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                        applyFilters();
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });
                
                loadUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
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
        if (rolePosition > 0) {
            String[] rolesValues = {"PLAYER", "COACH", "COORDINATOR", "ADMIN"};
            selectedRole = rolesValues[rolePosition - 1];
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
                    matchesTeam = (user.getTeamIds() == null || user.getTeamIds().isEmpty()) && (user.getTeamId() == null || user.getTeamId().isEmpty());
                } else {
                    boolean isPlayerInTeam = user.getTeamIds() != null && user.getTeamIds().contains(selectedTeamId);
                    boolean isCoachInTeam = user.getTeamId() != null && user.getTeamId().equals(selectedTeamId);
                    matchesTeam = isPlayerInTeam || isCoachInTeam;
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
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                if (currentUser != null && (currentUser.isAdmin() || currentUser.isCoordinator())) {
                    loadTeamsData();
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageUsersActivity.this, "אין לך הרשאה לגשת למסך זה", Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ManageUsersActivity.this, "שגיאה: " + error.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void loadUsers() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setUserId(userSnapshot.getKey());
                        usersList.add(user);
                    }
                }
                applyFilters();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
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

        List<String> rolesDisplayList = new ArrayList<>(Arrays.asList("שחקן (PLAYER)", "מאמן (COACH)", "רכז (COORDINATOR)", "מנהל (ADMIN)"));
        List<String> rolesValuesList = new ArrayList<>(Arrays.asList("PLAYER", "COACH", "COORDINATOR", "ADMIN"));

        if ("PLAYER".equals(user.getRole())) {
            int coachIndex = rolesValuesList.indexOf("COACH");
            if (coachIndex != -1) {
                rolesDisplayList.remove(coachIndex);
                rolesValuesList.remove(coachIndex);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rolesDisplayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);

        int currentPosition = rolesValuesList.indexOf(user.getRole());
        if (currentPosition != -1) {
            roleSpinner.setSelection(currentPosition);
        }

        builder.setTitle("שינוי הרשאות משתמש");
        builder.setPositiveButton("שמור", (dialog, which) -> {
            int selectedPosition = roleSpinner.getSelectedItemPosition();
            if (selectedPosition >= 0) {
                String newRole = rolesValuesList.get(selectedPosition);
                updateUserRole(user, newRole);
            }
        });
        builder.setNegativeButton("ביטול", null);

        builder.create().show();
    }

    
    private void showAssignTeamDialog(User user) {
        // This method can remain as is for now.
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
        // Unchanged
    }
    
    private void deleteUser(User user) {
        // Unchanged
    }

    private static class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
        private List<User> users;
        private Map<String, String> teamsMap;
        private OnUserClickListener listener;
        private OnAssignTeamClickListener assignTeamListener;
        private OnDeleteUserClickListener deleteUserListener;

        interface OnUserClickListener { void onUserClick(User user); }
        interface OnAssignTeamClickListener { void onAssignTeamClick(User user); }
        interface OnDeleteUserClickListener { void onDeleteUserClick(User user); }

        UsersAdapter(List<User> users, Map<String, String> teamsMap, OnUserClickListener listener, OnAssignTeamClickListener assignTeamListener, OnDeleteUserClickListener deleteUserListener) {
            this.users = users;
            this.teamsMap = teamsMap;
            this.listener = listener;
            this.assignTeamListener = assignTeamListener;
            this.deleteUserListener = deleteUserListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            User user = users.get(position);
            holder.bind(user, teamsMap, listener, assignTeamListener, deleteUserListener);
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

            void bind(User user, Map<String, String> teamsMap, OnUserClickListener listener, OnAssignTeamClickListener assignTeamListener, OnDeleteUserClickListener deleteUserListener) {
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
                
                // --- NEW LOGIC TO DISPLAY TEAM NAMES ---
                StringBuilder teamsText = new StringBuilder();
                if ("PLAYER".equals(user.getRole()) && user.getTeamIds() != null && !user.getTeamIds().isEmpty()) {
                    List<String> teamNames = user.getTeamIds().stream()
                        .map(id -> teamsMap.getOrDefault(id, "קבוצה לא ידועה"))
                        .collect(Collectors.toList());
                    teamsText.append(String.join(", ", teamNames));
                } else if ("COACH".equals(user.getRole()) && user.getTeamId() != null && !user.getTeamId().isEmpty()) {
                    teamsText.append(teamsMap.getOrDefault(user.getTeamId(), "קבוצה לא ידועה"));
                }

                if (teamsText.length() > 0) {
                    userTeam.setText("קבוצה: " + teamsText.toString());
                    userTeam.setVisibility(View.VISIBLE);
                } else {
                    userTeam.setText("קבוצה: אין");
                    userTeam.setVisibility(View.VISIBLE);
                }
                
                // Logic to show/hide the changeRoleButton
                if ("PLAYER".equals(user.getRole())) {
                    changeRoleButton.setVisibility(View.GONE);
                } else {
                    changeRoleButton.setVisibility(View.VISIBLE);
                    changeRoleButton.setOnClickListener(v -> listener.onUserClick(user));
                }

                assignTeamButton.setOnClickListener(v -> assignTeamListener.onAssignTeamClick(user));
                deleteUserButton.setOnClickListener(v -> deleteUserListener.onDeleteUserClick(user));
                
                if ("COACH".equals(user.getRole()) || "PLAYER".equals(user.getRole())) {
                    assignTeamButton.setVisibility(View.VISIBLE);
                } else {
                    assignTeamButton.setVisibility(View.GONE);
                }
            }
        }
    }
}
