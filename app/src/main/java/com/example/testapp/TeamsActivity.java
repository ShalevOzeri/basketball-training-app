package com.example.testapp;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.adapters.TeamAdapter;
import com.example.testapp.models.Team;
import com.example.testapp.viewmodel.TeamViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TeamsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TeamAdapter adapter;
    private TeamViewModel viewModel;
    private FloatingActionButton fab;
    private MaterialToolbar toolbar;

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
        adapter = new TeamAdapter(team -> showTeamOptionsDialog(team));
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TeamViewModel.class);
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

        // Age group
        final android.widget.Spinner ageGroupSpinner = new android.widget.Spinner(this);
        String[] ageGroups = {"U10", "U12", "U14", "U16", "U18", "U20", "Senior"};
        android.widget.ArrayAdapter<String> ageAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, ageGroups);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ageGroupSpinner.setAdapter(ageAdapter);
        layout.addView(ageGroupSpinner);

        // Level
        final android.widget.Spinner levelSpinner = new android.widget.Spinner(this);
        String[] levels = {"Beginner", "Intermediate", "Advanced"};
        android.widget.ArrayAdapter<String> levelAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, levels);
        levelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(levelAdapter);
        layout.addView(levelSpinner);

        // Coach name
        final android.widget.EditText coachInput = new android.widget.EditText(this);
        coachInput.setHint("שם המאמן");
        layout.addView(coachInput);

        builder.setView(layout);

        builder.setPositiveButton("הוסף", (dialog, which) -> {
            String name = nameInput.getText().toString().trim();
            String ageGroup = ageGroupSpinner.getSelectedItem().toString();
            String level = levelSpinner.getSelectedItem().toString();
            String coach = coachInput.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "יש להזין שם קבוצה", Toast.LENGTH_SHORT).show();
                return;
            }

            if (coach.isEmpty()) {
                coach = "ללא מאמן";
            }

            // Generate random color
            String color = com.example.testapp.utils.ColorUtils.getRandomColor();

            com.example.testapp.models.Team team = new com.example.testapp.models.Team(
                null, name, ageGroup, level, "", coach, color
            );

            viewModel.addTeam(team);
            Toast.makeText(this, "קבוצה נוספה: " + name, Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("ביטול", null);
        builder.show();
    }

    private void showTeamOptionsDialog(Team team) {
        String[] options = {"Edit", "Delete"};
        new AlertDialog.Builder(this)
            .setTitle(team.getName())
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    // Edit
                    Toast.makeText(this, "Edit: " + team.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    // Delete
                    confirmDelete(team);
                }
            })
            .show();
    }

    private void confirmDelete(Team team) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Team")
            .setMessage("Are you sure you want to delete " + team.getName() + "?")
            .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteTeam(team.getTeamId()))
            .setNegativeButton("Cancel", null)
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
