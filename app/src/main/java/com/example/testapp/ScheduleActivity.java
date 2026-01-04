package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.adapters.TrainingAdapter;
import com.example.testapp.models.Team;
import com.example.testapp.models.Training;
import com.example.testapp.models.User;
import com.example.testapp.repository.TrainingRepository;
import com.example.testapp.repository.UserRepository;
import com.example.testapp.viewmodel.TrainingViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity {

    private static final String TAG = "ScheduleActivity";

    private RecyclerView recyclerView;
    private TrainingAdapter adapter;
    private TrainingViewModel viewModel;
    private FloatingActionButton fab;
    private MaterialToolbar toolbar;
    private Chip chipThisWeek;
    private Spinner spinnerTeamFilter;
    private Spinner spinnerDayFilter;
    private Spinner spinnerLocationFilter;

    private List<Team> teamsList = new ArrayList<>();
    private ArrayAdapter<String> teamAdapter;
    private ArrayAdapter<String> dayAdapter;
    private ArrayAdapter<String> locationAdapter;

    private UserRepository userRepository;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        setupFilters();
        setupFab();
        loadTeams();
        checkUserPermissions();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.scheduleRecyclerView);
        fab = findViewById(R.id.fab);
        chipThisWeek = findViewById(R.id.chipThisWeek);
        spinnerTeamFilter = findViewById(R.id.spinnerTeamFilter);
        spinnerDayFilter = findViewById(R.id.spinnerDayFilter);
        spinnerLocationFilter = findViewById(R.id.spinnerLocationFilter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("לוח אימונים");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        adapter = new TrainingAdapter(
            training -> {
                Toast.makeText(this, training.getTeamName() + " - " + training.getStartTime(), Toast.LENGTH_SHORT).show();
            },
            training -> {
                Intent intent = new Intent(this, EditTrainingActivity.class);
                intent.putExtra("TRAINING", training);
                startActivity(intent);
            },
            training -> {
                showDeleteConfirmDialog(training);
            },
            training -> {
                showDuplicateDialog(training);
            }
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupFilters() {
        // Setup team filter spinner
        List<String> teamNames = new ArrayList<>();
        teamNames.add("כל הקבוצות");
        teamAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teamNames);
        teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeamFilter.setAdapter(teamAdapter);

        spinnerTeamFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    viewModel.setTeamFilter(null);
                } else {
                    Team selectedTeam = teamsList.get(position - 1);
                    viewModel.setTeamFilter(selectedTeam.getTeamId());
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                viewModel.setTeamFilter(null);
            }
        });

        // Setup day filter spinner
        List<String> days = new ArrayList<>();
        days.add("כל הימים");
        days.add("ראשון");
        days.add("שני");
        days.add("שלישי");
        days.add("רביעי");
        days.add("חמישי");
        days.add("שישי");
        days.add("שבת");

        dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDayFilter.setAdapter(dayAdapter);

        spinnerDayFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    viewModel.setDayFilter(null);
                } else {
                    String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                    viewModel.setDayFilter(dayNames[position - 1]);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                viewModel.setDayFilter(null);
            }
        });

        // Setup location filter spinner (will be populated dynamically by court names)
        List<String> locations = new ArrayList<>();
        locations.add("כל המגרשים");
        locationAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locations);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocationFilter.setAdapter(locationAdapter);

        spinnerLocationFilter.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    viewModel.setLocationFilter(null);
                } else {
                    String location = (String) parent.getItemAtPosition(position);
                    viewModel.setLocationFilter(location);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                viewModel.setLocationFilter(null);
            }
        });

        // Setup this week chip
        chipThisWeek.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setShowOnlyThisWeek(isChecked);
        });

        // Load locations from all trainings
        loadLocations();
    }

    private void loadTeams() {
        FirebaseDatabase.getInstance().getReference("teams")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    teamsList.clear();
                    List<String> teamNames = new ArrayList<>();
                    teamNames.add("כל הקבוצות");

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Team team = snapshot.getValue(Team.class);
                        if (team != null) {
                            teamsList.add(team);
                            teamNames.add(team.getName());
                        }
                    }

                    teamAdapter.clear();
                    teamAdapter.addAll(teamNames);
                    teamAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ScheduleActivity.this, "שגיאה בטעינת קבוצות", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TrainingViewModel.class);

        // Observe filtered trainings
        viewModel.getFilteredTrainings().observe(this, trainings -> {
            if (trainings != null) {
                adapter.setTrainings(trainings);
            }
        });

        // Observe all trainings and trigger filter update
        viewModel.getTrainings().observe(this, trainings -> {
            if (trainings != null) {
                viewModel.setShowOnlyThisWeek(chipThisWeek.isChecked());
            }
        });

        viewModel.getErrors().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFab() {
        fab.setOnClickListener(v -> {
            startActivity(new Intent(this, AddTrainingActivity.class));
        });
    }

    private void checkUserPermissions() {
        userRepository = new UserRepository();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            FirebaseDatabase.getInstance().getReference("users").child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        currentUser = snapshot.getValue(User.class);
                        if (currentUser != null) {
                            boolean canEdit = currentUser.isAdmin() || currentUser.isCoordinator();

                            if (!canEdit) {
                                fab.setVisibility(View.GONE);
                                adapter = new TrainingAdapter(
                                    training -> {
                                        Toast.makeText(ScheduleActivity.this, training.getTeamName() + " - " + training.getStartTime(), Toast.LENGTH_SHORT).show();
                                    },
                                    null, // No edit
                                    null, // No delete
                                    null  // No duplicate
                                );
                                recyclerView.setAdapter(adapter);

                                if (viewModel.getFilteredTrainings().getValue() != null) {
                                    adapter.setTrainings(viewModel.getFilteredTrainings().getValue());
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ScheduleActivity.this, "Failed to load user permissions.", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    private void showDeleteConfirmDialog(Training training) {
        new AlertDialog.Builder(this)
            .setTitle("מחיקת אימון")
            .setMessage("האם בטוח שברצונך למחוק אימון זה?\n" + training.getTeamName() + " - " + training.getStartTime() + " עד " + training.getEndTime())
            .setPositiveButton("מחוק", (dialog, which) -> {
                viewModel.deleteTraining(training.getTrainingId());
                Toast.makeText(ScheduleActivity.this, "אימון נמחק", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("ביטול", null)
            .show();
    }

    private void showDuplicateDialog(Training training) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("שכפל אימון");

        // Create a layout for the dialog
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 20);

        // Add a TextView for instruction
        TextView message = new TextView(this);
        message.setText("הזן את מספר השבועות לשכפול:");
        message.setTextSize(16f);
        message.setPadding(0, 0, 0, 20);
        layout.addView(message);

        // Add an EditText for the number of weeks
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(input);

        builder.setView(layout);

        // Set up the buttons
        builder.setPositiveButton("שכפל", (dialog, which) -> {
            String weeksStr = input.getText().toString();
            if (weeksStr.isEmpty()) {
                Toast.makeText(ScheduleActivity.this, "יש להזין מספר שבועות", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int weeks = Integer.parseInt(weeksStr);
                if (weeks > 0 && weeks <= 52) { // Limit to 1 year
                    duplicateTraining(training, weeks);
                } else {
                    Toast.makeText(ScheduleActivity.this, "נא להזין מספר בין 1 ל-52", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(ScheduleActivity.this, "מספר לא תקין", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("ביטול", null);

        builder.show();
    }

    private void duplicateTraining(Training originalTraining, int weeks) {
        Calendar dupDate = Calendar.getInstance();
        dupDate.setTimeInMillis(originalTraining.getDate());

        for (int i = 1; i <= weeks; i++) {
            dupDate.add(Calendar.WEEK_OF_YEAR, 1);

            Training duplicate = new Training();
            duplicate.setTeamId(originalTraining.getTeamId());
            duplicate.setTeamName(originalTraining.getTeamName());
            duplicate.setTeamColor(originalTraining.getTeamColor());
            duplicate.setCourtId(originalTraining.getCourtId());
            duplicate.setCourtName(originalTraining.getCourtName());
            duplicate.setCourtType(originalTraining.getCourtType());
            duplicate.setStartTime(originalTraining.getStartTime());
            duplicate.setEndTime(originalTraining.getEndTime());
            duplicate.setDate(dupDate.getTimeInMillis());
            duplicate.setDayOfWeek(new SimpleDateFormat("EEEE", Locale.getDefault()).format(dupDate.getTime()));
            duplicate.setNotes(originalTraining.getNotes());
            duplicate.setCreatedAt(System.currentTimeMillis());

            viewModel.addTraining(duplicate, new TrainingRepository.OnConflictCheckListener() {
                @Override
                public void onSuccess() {
                    // Silent success
                }

                @Override
                public void onConflict() {
                    // Skip if conflict
                }

                @Override
                public void onFailure(String error) {
                    // Log but continue
                }
            });
        }

        Toast.makeText(this, "אימון שוכפל " + weeks + " שבועות קדימה", Toast.LENGTH_SHORT).show();
    }

    private void loadLocations() {
        FirebaseDatabase.getInstance().getReference("courts")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    List<String> courtNames = new ArrayList<>();
                    courtNames.add("כל המגרשים");

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        com.example.testapp.models.Court court = snapshot.getValue(com.example.testapp.models.Court.class);
                        if (court != null && court.getName() != null) {
                            if (!courtNames.contains(court.getName())) {
                                courtNames.add(court.getName());
                            }
                        }
                    }

                    locationAdapter.clear();
                    locationAdapter.addAll(courtNames);
                    locationAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(ScheduleActivity.this, "שגיאה בטעינת מגרשים", Toast.LENGTH_SHORT).show();
                }
            });
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
