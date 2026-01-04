package com.example.testapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.testapp.models.Court;
import com.example.testapp.models.Team;
import com.example.testapp.models.Training;
import com.example.testapp.viewmodel.CourtViewModel;
import com.example.testapp.viewmodel.TrainingViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllCourtsViewActivity extends AppCompatActivity {

    private ScrollView scrollView;
    private LinearLayout courtsContainer;
    private MaterialToolbar toolbar;
    private Spinner spinnerCourtFilter;
    private Spinner spinnerTeamFilter;
    private CourtViewModel courtViewModel;
    private TrainingViewModel trainingViewModel;

    private ArrayAdapter<String> courtFilterAdapter;
    private ArrayAdapter<String> teamFilterAdapter;

    private List<Court> courts = new ArrayList<>();
    private List<Training> trainings = new ArrayList<>();
    private List<Team> allTeams = new ArrayList<>();

    private String selectedCourtId = null;
    private String selectedTeamId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_courts_view);

        initializeViews();
        setupToolbar();
        setupFilters();
        setupViewModels();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        scrollView = findViewById(R.id.scrollView);
        courtsContainer = findViewById(R.id.courtsContainer);
        spinnerCourtFilter = findViewById(R.id.spinnerCourtFilter);
        spinnerTeamFilter = findViewById(R.id.spinnerTeamFilter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("תצוגת כל המגרשים");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupFilters() {
        // Setup court filter spinner
        List<String> courtNames = new ArrayList<>();
        courtNames.add("כל המגרשים");
        courtFilterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courtNames);
        courtFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourtFilter.setAdapter(courtFilterAdapter);

        spinnerCourtFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedCourtId = null;
                } else {
                    String courtName = (String) parent.getItemAtPosition(position);
                    for (Court court : courts) {
                        if (court.getName().equals(courtName)) {
                            selectedCourtId = court.getCourtId();
                            break;
                        }
                    }
                }
                updateUI();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedCourtId = null;
                updateUI();
            }
        });

        // Setup team filter spinner
        List<String> teamNames = new ArrayList<>();
        teamNames.add("כל הקבוצות");
        teamFilterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teamNames);
        teamFilterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeamFilter.setAdapter(teamFilterAdapter);

        spinnerTeamFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedTeamId = null;
                } else {
                    selectedTeamId = allTeams.get(position - 1).getTeamId();
                }
                updateUI();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                selectedTeamId = null;
                updateUI();
            }
        });
    }


    private void setupViewModels() {
        courtViewModel = new ViewModelProvider(this).get(CourtViewModel.class);
        trainingViewModel = new ViewModelProvider(this).get(TrainingViewModel.class);

        courtViewModel.getCourts().observe(this, courtsList -> {
            courts = courtsList;
            loadCourtFilterOptions();
            updateUI();
        });

        trainingViewModel.getTrainings().observe(this, trainingsList -> {
            trainings = trainingsList;
            updateUI();
        });

        loadTeams();
    }

    private void loadCourtFilterOptions() {
        List<String> courtNames = new ArrayList<>();
        courtNames.add("כל המגרשים");

        for (Court court : courts) {
            if (court.getName() != null && !courtNames.contains(court.getName())) {
                courtNames.add(court.getName());
            }
        }

        courtFilterAdapter.clear();
        courtFilterAdapter.addAll(courtNames);
        courtFilterAdapter.notifyDataSetChanged();
    }

    private void loadTeams() {
        FirebaseDatabase.getInstance().getReference("teams")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        allTeams.clear();
                        List<String> teamNames = new ArrayList<>();
                        teamNames.add("כל הקבוצות");

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Team team = snapshot.getValue(Team.class);
                            if (team != null) {
                                allTeams.add(team);
                                teamNames.add(team.getName());
                            }
                        }

                        teamFilterAdapter.clear();
                        teamFilterAdapter.addAll(teamNames);
                        teamFilterAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AllCourtsViewActivity.this, "שגיאה בטעינת קבוצות", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI() {
        if (courts.isEmpty()) return;

        courtsContainer.removeAllViews();

        Set<String> courtsForSelectedTeam = null;
        if (selectedTeamId != null) {
            courtsForSelectedTeam = new HashSet<>();
            for (Training training : trainings) {
                if (selectedTeamId.equals(training.getTeamId())) {
                    courtsForSelectedTeam.add(training.getCourtId());
                }
            }
        }

        for (Court court : courts) {
            if (selectedCourtId != null && !selectedCourtId.equals(court.getCourtId())) {
                continue;
            }

            if (courtsForSelectedTeam != null && !courtsForSelectedTeam.contains(court.getCourtId())) {
                continue;
            }

            View courtView = createCourtView(court);
            courtsContainer.addView(courtView);
        }
    }

    private View createCourtView(Court court) {
        LinearLayout courtLayout = new LinearLayout(this);
        courtLayout.setOrientation(LinearLayout.VERTICAL);
        courtLayout.setPadding(16, 16, 16, 16);

        LinearLayout headerLayout = new LinearLayout(this);
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView courtName = new TextView(this);
        courtName.setText(court.getName());
        courtName.setTextSize(20);
        courtName.setTypeface(null, android.graphics.Typeface.BOLD);
        courtName.setTextColor(Color.BLACK);
        courtName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        headerLayout.addView(courtName);

        int trainingCount = 0;
        for (Training training : trainings) {
            if (training.getCourtId().equals(court.getCourtId())) {
                trainingCount++;
            }
        }

        TextView infoText = new TextView(this);
        infoText.setText("אימונים: " + trainingCount);
        infoText.setTextSize(14);
        infoText.setTextColor(Color.GRAY);
        infoText.setPadding(8, 0, 0, 0);
        headerLayout.addView(infoText);

        headerLayout.setPadding(0, 0, 0, 16);
        courtLayout.addView(headerLayout);

        View timelineView = createTimelineView(court);
        courtLayout.addView(timelineView);

        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2);
        dividerParams.setMargins(0, 24, 0, 24);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(Color.LTGRAY);
        courtLayout.addView(divider);

        return courtLayout;
    }

    private View createTimelineView(Court court) {
        LinearLayout timeline = new LinearLayout(this);
        timeline.setOrientation(LinearLayout.VERTICAL);
        timeline.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        timeline.setBackgroundColor(Color.parseColor("#F5F5F5"));
        timeline.setPadding(8, 8, 8, 8);

        List<Training> courtTrainings = new ArrayList<>();
        for (Training training : trainings) {
            if (training.getCourtId().equals(court.getCourtId())) {
                courtTrainings.add(training);
            }
        }

        if (courtTrainings.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("אין אימונים");
            emptyText.setTextColor(Color.GRAY);
            emptyText.setPadding(16, 16, 16, 16);
            emptyText.setTextSize(14);
            timeline.addView(emptyText);
        } else {
            for (Training training : courtTrainings) {
                View trainingBlock = createTrainingBlock(training);
                timeline.addView(trainingBlock);
            }
        }

        return timeline;
    }

    private View createTrainingBlock(Training training) {
        LinearLayout blockLayout = new LinearLayout(this);
        blockLayout.setOrientation(LinearLayout.VERTICAL);
        blockLayout.setPadding(8, 8, 8, 8);
        blockLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        blockLayout.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams blockParams = (LinearLayout.LayoutParams) blockLayout.getLayoutParams();
        blockParams.setMargins(0, 4, 0, 4);
        blockLayout.setLayoutParams(blockParams);

        TextView teamNameView = new TextView(this);
        teamNameView.setText(training.getTeamName());
        teamNameView.setTextColor(Color.BLACK);
        teamNameView.setTextSize(14);
        teamNameView.setTypeface(null, android.graphics.Typeface.BOLD);
        blockLayout.addView(teamNameView);

        TextView timeView = new TextView(this);
        timeView.setText(training.getStartTime() + " - " + training.getEndTime());
        timeView.setTextColor(Color.GRAY);
        timeView.setTextSize(12);
        timeView.setPadding(0, 4, 0, 0);
        blockLayout.addView(timeView);

        try {
            blockLayout.setBackgroundColor(Color.parseColor(training.getTeamColor()));
            teamNameView.setTextColor(Color.WHITE);
            timeView.setTextColor(Color.parseColor("#CCCCCC"));
        } catch (Exception e) {
            blockLayout.setBackgroundColor(Color.parseColor("#3DDC84"));
            teamNameView.setTextColor(Color.WHITE);
            timeView.setTextColor(Color.parseColor("#CCCCCC"));
        }

        return blockLayout;
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
