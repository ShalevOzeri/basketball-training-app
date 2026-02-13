package com.example.testapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.R;
import com.example.testapp.TeamPlayersActivity;
import com.example.testapp.adapters.TeamAdapter;
import com.example.testapp.models.Team;
import com.example.testapp.models.User;
import com.example.testapp.viewmodel.TeamViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TeamsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TeamAdapter adapter;
    private TeamViewModel viewModel;
    private FloatingActionButton fab;
    private DatabaseReference usersRef;
    private DatabaseReference settingsRef;
    private List<User> coachList = new ArrayList<>();
    private String currentUserId;
    private String currentUserRole;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teams, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupViewModel();
        setupFab();
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.teamsRecyclerView);
        fab = view.findViewById(R.id.fab);
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        settingsRef = FirebaseDatabase.getInstance().getReference("settings");
        loadCoaches();
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TeamAdapter(new TeamAdapter.OnTeamInteractionListener() {
            @Override
            public void onTeamClick(Team team) {
                showTeamPlayersActivity(team);
            }

            @Override
            public void onEditClick(Team team) {
                showTeamOptionsDialog(team);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TeamViewModel.class);
        
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        usersRef.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    currentUserRole = user.getRole();
                    
                    adapter.setUserRole(currentUserRole);
                    
                    if ("COACH".equals(currentUserRole)) {
                        fab.setVisibility(View.GONE);
                        viewModel.filterByCoach(currentUserId);
                    } else {
                        fab.setVisibility(View.VISIBLE);
                        viewModel.clearFilter();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "שגיאה בטעינת תפקיד", Toast.LENGTH_SHORT).show();
            }
        });
        
        viewModel.getTeams().observe(getViewLifecycleOwner(), teams -> {
            adapter.setTeams(teams);
        });

        viewModel.getErrors().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFab() {
        fab.setOnClickListener(v -> showAddTeamDialog());
    }
    
    private void loadCoaches() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "שגיאה בטעינת מאמנים ורכזים", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddTeamDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("הוספת קבוצה חדשה");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText nameInput = new EditText(requireContext());
        nameInput.setHint("שם הקבוצה");
        layout.addView(nameInput);

        final Spinner gradeSpinner = new Spinner(requireContext());
        String[] grades = {"ו", "ז", "ח", "ט", "י", "יא", "יב", "בוגרים/בוגרות"};
        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(
            requireContext(), android.R.layout.simple_spinner_item, grades);
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gradeSpinner.setAdapter(gradeAdapter);
        layout.addView(gradeSpinner);

        final Spinner colorSpinner = new Spinner(requireContext());
        String[] colors = {"ירוק", "כחול", "אדום", "צהוב", "סגול", "כתום", "ורוד", "תכלת", "אפור", "שחור", "חום", "תורכיז"};
        String[] colorValues = {"#3DDC84", "#2196F3", "#F44336", "#FFEB3B", "#9C27B0", "#FF9800", "#E91E63", "#00BCD4", "#9E9E9E", "#212121", "#795548", "#00E5FF"};
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(
            requireContext(), android.R.layout.simple_spinner_item, colors);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(colorAdapter);
        layout.addView(colorSpinner);

        final Spinner coachSpinner = new Spinner(requireContext());
        List<String> coachNames = new ArrayList<>();
        coachNames.add("ללא מאמן");
        for (User coach : coachList) {
            coachNames.add(coach.getName());
        }
        ArrayAdapter<String> coachAdapter = new ArrayAdapter<>(
            requireContext(), android.R.layout.simple_spinner_item, coachNames);
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
                // Coach assignment is stored in teams.coachId only
            }

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "יש להזין שם קבוצה", Toast.LENGTH_SHORT).show();
                return;
            }

            Team team = new Team(null, name, grade, "", coachId, coachName, color);
            viewModel.addTeam(team);
            Toast.makeText(requireContext(), "קבוצה נוספה: " + name, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("ביטול", null);
        builder.show();
    }

    private void showTeamPlayersActivity(Team team) {
        Intent intent = new Intent(requireActivity(), TeamPlayersActivity.class);
        intent.putExtra("teamId", team.getTeamId());
        intent.putExtra("teamName", team.getName());
        intent.putExtra("team", team);
        startActivity(intent);
    }

    private void showTeamOptionsDialog(Team team) {
        if ("COACH".equals(currentUserRole)) {
            Toast.makeText(requireContext(), "אין הרשאה לערוך קבוצה", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] options = {"ערוך פרטי קבוצה", "ערוך מאמן", "מחק קבוצה"};
        new AlertDialog.Builder(requireContext())
            .setTitle(team.getName())
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    showEditTeamDialog(team);
                } else if (which == 1) {
                    showEditCoachDialog(team);
                } else {
                    confirmDelete(team);
                }
            })
            .show();
    }
    
    private void showEditTeamDialog(Team team) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("עריכת " + team.getName());

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText nameInput = new EditText(requireContext());
        nameInput.setHint("שם הקבוצה");
        nameInput.setText(team.getName());
        layout.addView(nameInput);

        final Spinner gradeSpinner = new Spinner(requireContext());
        String[] grades = {"ו", "ז", "ח", "ט", "י", "יא", "יב", "בוגרים/בוגרות"};
        ArrayAdapter<String> gradeAdapter = new ArrayAdapter<>(
            requireContext(), android.R.layout.simple_spinner_item, grades);
        gradeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gradeSpinner.setAdapter(gradeAdapter);
        
        for (int i = 0; i < grades.length; i++) {
            if (grades[i].equals(team.getAgeGroup())) {
                gradeSpinner.setSelection(i);
                break;
            }
        }
        layout.addView(gradeSpinner);

        final Spinner colorSpinner = new Spinner(requireContext());
        String[] colors = {"ירוק", "כחול", "אדום", "צהוב", "סגול", "כתום", "ורוד", "תכלת", "אפור", "שחור", "חום", "תורכיז"};
        String[] colorValues = {"#3DDC84", "#2196F3", "#F44336", "#FFEB3B", "#9C27B0", "#FF9800", "#E91E63", "#00BCD4", "#9E9E9E", "#212121", "#795548", "#00E5FF"};
        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(
            requireContext(), android.R.layout.simple_spinner_item, colors);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(colorAdapter);
        
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
                Toast.makeText(requireContext(), "נא להזין שם קבוצה", Toast.LENGTH_SHORT).show();
                return;
            }

            team.setName(name);
            team.setAgeGroup(grade);
            team.setColor(color);
            team.setUpdatedAt(System.currentTimeMillis());

            viewModel.updateTeam(team);
            Toast.makeText(requireContext(), "הקבוצה עודכנה בהצלחה", Toast.LENGTH_SHORT).show();
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
        
        new AlertDialog.Builder(requireContext())
            .setTitle("בחר מאמן עבור " + team.getName())
            .setItems(coachArray, (dialog, which) -> {
                String newCoachId = "";
                String newCoachName = "ללא מאמן";
                
                // Coach assignment is stored in teams.coachId only (not in users.teamId)
                
                if (which > 0 && which <= coachList.size()) {
                    User selectedCoach = coachList.get(which - 1);
                    newCoachId = selectedCoach.getUserId();
                    newCoachName = selectedCoach.getName();
                }
                
                DatabaseReference teamsRef = FirebaseDatabase.getInstance().getReference("teams");
                teamsRef.child(team.getTeamId()).child("coachId").setValue(newCoachId);
                teamsRef.child(team.getTeamId()).child("coachName").setValue(newCoachName);
                
                Toast.makeText(requireContext(), "מאמן עודכן בהצלחה", Toast.LENGTH_SHORT).show();
            })
            .show();
    }

    private void confirmDelete(Team team) {
        new AlertDialog.Builder(requireContext())
            .setTitle("מחיקת קבוצה")
            .setMessage("האם אתה בטוח שברצונך למחוק את " + team.getName() + "?")
            .setPositiveButton("מחק", (dialog, which) -> {
                // Delete the team directly - no need to clean users.teamId for coaches
                viewModel.deleteTeam(team.getTeamId());
                Toast.makeText(requireContext(), "הקבוצה נמחקה", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("ביטול", null)
            .show();
    }
}
