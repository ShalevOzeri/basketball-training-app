package com.example.testapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.R;
import com.example.testapp.models.Team;
import com.example.testapp.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ManageUsersFragment extends Fragment {

    private RecyclerView usersRecyclerView;
    private ProgressBar progressBar;
    private SearchView searchViewTeams;
    private ChipGroup chipGroupRoles;
    private ChipGroup chipGroupTeams;
    private MaterialButton btnSelectAllRoles;
    private MaterialButton btnSelectAllTeams;
    private LinearLayout filterHeader;
    private androidx.core.widget.NestedScrollView filtersScrollView;
    private ImageView expandCollapseIcon;
    private boolean isFiltersExpanded = false;
    
    private DatabaseReference usersRef;
    private UsersAdapter adapter;
    private List<User> usersList;
    private List<User> filteredUsersList;
    private User currentUser;
    private Map<String, String> teamsMap;
    private List<Team> teamsList = new ArrayList<>();
    
    private Set<String> selectedRoles = new HashSet<>();
    private Set<String> selectedTeamIds = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        usersRecyclerView = view.findViewById(R.id.usersRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        searchViewTeams = view.findViewById(R.id.searchViewTeams);
        chipGroupRoles = view.findViewById(R.id.chipGroupRoles);
        chipGroupTeams = view.findViewById(R.id.chipGroupTeams);
        btnSelectAllRoles = view.findViewById(R.id.btnSelectAllRoles);
        btnSelectAllTeams = view.findViewById(R.id.btnSelectAllTeams);
        filterHeader = view.findViewById(R.id.filterHeader);
        filtersScrollView = view.findViewById(R.id.filtersScrollView);
        expandCollapseIcon = view.findViewById(R.id.expandCollapseIcon);

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersList = new ArrayList<>();
        filteredUsersList = new ArrayList<>();
        teamsMap = new HashMap<>();

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new UsersAdapter(filteredUsersList, teamsMap, this::showChangeRoleDialog, this::showAssignTeamDialog, this::confirmDeleteUser);
        usersRecyclerView.setAdapter(adapter);

        setupExpandCollapse();
        setupSelectAllButtons();
        setupFilters();
        checkAdminAccess();
    }

    private void setupExpandCollapse() {
        filterHeader.setOnClickListener(v -> toggleFilters());
    }

    private void toggleFilters() {
        isFiltersExpanded = !isFiltersExpanded;

        if (isFiltersExpanded) {
            filtersScrollView.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params = filtersScrollView.getLayoutParams();
            params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.35);
            filtersScrollView.setLayoutParams(params);
            expandCollapseIcon.setRotation(180);
        } else {
            filtersScrollView.setVisibility(View.GONE);
            expandCollapseIcon.setRotation(0);
        }
    }

    private void setupSelectAllButtons() {
        btnSelectAllRoles.setOnClickListener(v -> selectAllRoles());
        btnSelectAllTeams.setOnClickListener(v -> selectAllTeams());
    }

    private void selectAllRoles() {
        boolean allChecked = true;
        for (int i = 0; i < chipGroupRoles.getChildCount(); i++) {
            View child = chipGroupRoles.getChildAt(i);
            if (child instanceof Chip) {
                if (!((Chip) child).isChecked()) {
                    allChecked = false;
                    break;
                }
            }
        }
        
        for (int i = 0; i < chipGroupRoles.getChildCount(); i++) {
            View child = chipGroupRoles.getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setChecked(!allChecked);
            }
        }
    }

    private void selectAllTeams() {
        boolean allChecked = true;
        for (int i = 0; i < chipGroupTeams.getChildCount(); i++) {
            View child = chipGroupTeams.getChildAt(i);
            if (child instanceof Chip && child.getVisibility() == View.VISIBLE) {
                if (!((Chip) child).isChecked()) {
                    allChecked = false;
                    break;
                }
            }
        }
        
        for (int i = 0; i < chipGroupTeams.getChildCount(); i++) {
            View child = chipGroupTeams.getChildAt(i);
            if (child instanceof Chip && child.getVisibility() == View.VISIBLE) {
                ((Chip) child).setChecked(!allChecked);
            }
        }
    }

    private void setupFilters() {
        // Setup role chips
        setupRoleChips();
        
        // Setup search view for teams
        searchViewTeams.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterTeamChips(newText);
                return true;
            }
        });
    }

    private void setupRoleChips() {
        String[] roles = {"שחקן", "מאמן", "רכז", "מנהל"};
        String[] roleValues = {"PLAYER", "COACH", "COORDINATOR", "ADMIN"};

        for (int i = 0; i < roles.length; i++) {
            final String roleValue = roleValues[i];
            Chip chip = new Chip(requireContext());
            chip.setText(roles[i]);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            chip.setChecked(true);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedRoles.add(roleValue);
                } else {
                    selectedRoles.remove(roleValue);
                }
                applyFilters();
            });

            chipGroupRoles.addView(chip);
            selectedRoles.add(roleValue);
        }
    }

    private void filterTeamChips(String query) {
        String lowerQuery = query.toLowerCase();
        for (int i = 0; i < chipGroupTeams.getChildCount(); i++) {
            View child = chipGroupTeams.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                Team team = (Team) chip.getTag();
                if (team != null) {
                    boolean matches = team.getName().toLowerCase().contains(lowerQuery);
                    chip.setVisibility(matches ? View.VISIBLE : View.GONE);
                }
            }
        }
    }

    private void loadTeamsData() {
        DatabaseReference teamsRef = FirebaseDatabase.getInstance().getReference("teams");
        teamsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                teamsMap.clear();
                teamsList.clear();
                chipGroupTeams.removeAllViews();
                selectedTeamIds.clear();

                // Add "No Team" chip
                Chip noTeamChip = new Chip(requireContext());
                noTeamChip.setText("ללא קבוצה");
                noTeamChip.setCheckable(true);
                noTeamChip.setCheckedIconVisible(true);
                noTeamChip.setChecked(true);
                noTeamChip.setTag(null); // Special marker for no team
                noTeamChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedTeamIds.add("NO_TEAM");
                    } else {
                        selectedTeamIds.remove("NO_TEAM");
                    }
                    applyFilters();
                });
                chipGroupTeams.addView(noTeamChip);
                selectedTeamIds.add("NO_TEAM");

                for (DataSnapshot teamSnapshot : snapshot.getChildren()) {
                    Team team = teamSnapshot.getValue(Team.class);
                    if (team != null) {
                        teamsList.add(team);
                        teamsMap.put(team.getTeamId(), team.getName());
                        addTeamChip(team);
                        selectedTeamIds.add(team.getTeamId());
                    }
                }

                loadUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "שגיאה בטעינת קבוצות", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTeamChip(Team team) {
        Chip chip = new Chip(requireContext());
        chip.setText(team.getName());
        chip.setCheckable(true);
        chip.setCheckedIconVisible(true);
        chip.setChecked(true);
        chip.setTag(team);

        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedTeamIds.add(team.getTeamId());
            } else {
                selectedTeamIds.remove(team.getTeamId());
            }
            applyFilters();
        });

        chipGroupTeams.addView(chip);
    }

    private void applyFilters() {
        if (usersList == null || usersList.isEmpty()) {
            return;
        }

        filteredUsersList.clear();

        for (User user : usersList) {
            // Role filter
            boolean matchesRole = selectedRoles.isEmpty() || selectedRoles.contains(user.getRole());
            
            // Team filter
            boolean matchesTeam = false;
            if (selectedTeamIds.isEmpty()) {
                matchesTeam = true;
            } else {
                // Check "No Team"
                if (selectedTeamIds.contains("NO_TEAM")) {
                    boolean hasNoTeam = (user.getTeamIds() == null || user.getTeamIds().isEmpty()) && 
                                       (user.getTeamId() == null || user.getTeamId().isEmpty());
                    if (hasNoTeam) {
                        matchesTeam = true;
                    }
                }
                
                // Check specific teams
                if (!matchesTeam) {
                    for (String teamId : selectedTeamIds) {
                        if (!"NO_TEAM".equals(teamId)) {
                            boolean isPlayerInTeam = user.getTeamIds() != null && user.getTeamIds().contains(teamId);
                            boolean isCoachInTeam = user.getTeamId() != null && user.getTeamId().equals(teamId);
                            if (isPlayerInTeam || isCoachInTeam) {
                                matchesTeam = true;
                                break;
                            }
                        }
                    }
                }
            }

            if (matchesRole && matchesTeam) {
                filteredUsersList.add(user);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void checkAdminAccess() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUser = snapshot.getValue(User.class);
                    if (currentUser != null && "ADMIN".equals(currentUser.getRole())) {
                        loadTeamsData();
                    } else {
                        if (isAdded() && getContext() != null) {
                            Toast.makeText(requireContext(), "גישה נדחתה - נדרשות הרשאות מנהל", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(requireContext(), "שגיאה בטעינת נתונים", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        usersList.add(user);
                    }
                }
                applyFilters();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                if (isAdded() && getContext() != null) {
                    Toast.makeText(requireContext(), "שגיאה בטעינת משתמשים", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showChangeRoleDialog(User user) {
        String[] roles = {"ADMIN", "COORDINATOR", "COACH"};
        String[] roleNames = {"מנהל", "רכז", "מאמן"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("שינוי תפקיד עבור " + user.getName());
        builder.setItems(roleNames, (dialog, which) -> {
            String newRole = roles[which];
            updateUserRole(user, newRole);
        });
        builder.setNegativeButton("ביטול", null);
        builder.show();
    }

    private void showAssignTeamDialog(User user) {
        if (teamsList.isEmpty()) {
            Toast.makeText(requireContext(), "אין קבוצות זמינות", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] teamNames = new String[teamsList.size()];
        for (int i = 0; i < teamsList.size(); i++) {
            teamNames[i] = teamsList.get(i).getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("שיוך קבוצה עבור " + user.getName());

        if ("PLAYER".equals(user.getRole())) {
            boolean[] checkedTeams = new boolean[teamsList.size()];
            List<String> currentTeamIds = user.getTeamIds() != null ? user.getTeamIds() : new ArrayList<>();
            
            for (int i = 0; i < teamsList.size(); i++) {
                checkedTeams[i] = currentTeamIds.contains(teamsList.get(i).getTeamId());
            }

            builder.setMultiChoiceItems(teamNames, checkedTeams, (dialog, which, isChecked) -> {
                checkedTeams[which] = isChecked;
            });

            builder.setPositiveButton("שמור", (dialog, which) -> {
                List<String> selectedTeamIds = new ArrayList<>();
                for (int i = 0; i < checkedTeams.length; i++) {
                    if (checkedTeams[i]) {
                        selectedTeamIds.add(teamsList.get(i).getTeamId());
                    }
                }
                usersRef.child(user.getUserId()).child("teamIds").setValue(selectedTeamIds)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "הקבוצות עודכנו בהצלחה", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "שגיאה בעדכון קבוצות", Toast.LENGTH_SHORT).show();
                    });
            });
        } else {
            builder.setItems(teamNames, (dialog, which) -> {
                Team selectedTeam = teamsList.get(which);
                usersRef.child(user.getUserId()).child("teamId").setValue(selectedTeam.getTeamId())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "הקבוצה עודכנה בהצלחה", Toast.LENGTH_SHORT).show();
                        loadUsers();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "שגיאה בעדכון קבוצה", Toast.LENGTH_SHORT).show();
                    });
            });
        }

        builder.setNegativeButton("ביטול", null);
        builder.show();
    }

    private void updateUserRole(User user, String newRole) {
        usersRef.child(user.getUserId()).child("role").setValue(newRole)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(requireContext(), "התפקיד עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                loadUsers();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "שגיאה בעדכון תפקיד", Toast.LENGTH_SHORT).show();
            });
    }

    private void confirmDeleteUser(User user) {
        new AlertDialog.Builder(requireContext())
            .setTitle("מחיקת משתמש")
            .setMessage("האם אתה בטוח שברצונך למחוק את " + user.getName() + "?")
            .setPositiveButton("מחק", (dialog, which) -> deleteUser(user))
            .setNegativeButton("ביטול", null)
            .show();
    }

    private void deleteUser(User user) {
        usersRef.child(user.getUserId()).removeValue()
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(requireContext(), "המשתמש נמחק בהצלחה", Toast.LENGTH_SHORT).show();
                loadUsers();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "שגיאה במחיקת משתמש", Toast.LENGTH_SHORT).show();
            });
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

        UsersAdapter(List<User> users, Map<String, String> teamsMap, OnUserClickListener listener, 
                    OnAssignTeamClickListener assignTeamListener, OnDeleteUserClickListener deleteUserListener) {
            this.users = users;
            this.teamsMap = teamsMap;
            this.listener = listener;
            this.assignTeamListener = assignTeamListener;
            this.deleteUserListener = deleteUserListener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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

            void bind(User user, Map<String, String> teamsMap, OnUserClickListener listener, 
                     OnAssignTeamClickListener assignTeamListener, OnDeleteUserClickListener deleteUserListener) {
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
                
                // הצגת שמות הקבוצות
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
                
                // הצגת/הסתרת כפתור שינוי תפקיד
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
