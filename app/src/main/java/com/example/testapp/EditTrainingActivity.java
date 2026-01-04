package com.example.testapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.testapp.models.DaySchedule;
import com.example.testapp.models.Court;
import com.example.testapp.models.Team;
import com.example.testapp.models.Training;
import com.example.testapp.repository.TrainingRepository;
import com.example.testapp.viewmodel.TrainingViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditTrainingActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private Spinner spinnerTeam;
    private Spinner spinnerCourt;
    private EditText editDate;
    private EditText editStartTime;
    private EditText editEndTime;
    private EditText editNotes;
    private MaterialButton btnSave;

    private TrainingViewModel viewModel;
    private Training originalTraining;
    private final List<Team> teams = new ArrayList<>();
    private ArrayAdapter<String> teamAdapter;
    private final List<Court> courts = new ArrayList<>();
    private ArrayAdapter<String> courtAdapter;
    private final Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_training);

        originalTraining = (Training) getIntent().getSerializableExtra("TRAINING");
        if (originalTraining == null) {
            Toast.makeText(this, "שגיאה: לא נמצא אימון לעריכה", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        selectedDate.setTimeInMillis(originalTraining.getDate());

        initViews();
        setupToolbar();
        setupViewModel();
        setupTeamSpinner();
        setupCourtSpinner();
        setupPickers();
        setupSave();
        loadTeams();
        loadCourts();
        populateFields();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        spinnerTeam = findViewById(R.id.spinnerTeam);
        spinnerCourt = findViewById(R.id.spinnerCourt);
        editDate = findViewById(R.id.editDate);
        editStartTime = findViewById(R.id.editStartTime);
        editEndTime = findViewById(R.id.editEndTime);
        editNotes = findViewById(R.id.editNotes);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("עריכת אימון");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TrainingViewModel.class);
        viewModel.getErrors().observe(this, err -> {
            if (err != null) {
                Toast.makeText(this, err, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTeamSpinner() {
        teamAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        teamAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTeam.setAdapter(teamAdapter);
    }

    private void setupCourtSpinner() {
        courtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        courtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourt.setAdapter(courtAdapter);
    }

    private void setupPickers() {
        SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        editDate.setText(dateFmt.format(selectedDate.getTime()));

        editDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(this, (DatePicker view, int year, int month, int dayOfMonth) -> {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                editDate.setText(dateFmt.format(selectedDate.getTime()));
            }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
            dialog.show();
        });

        editStartTime.setOnClickListener(v -> showTimePicker(editStartTime));
        editEndTime.setOnClickListener(v -> showTimePicker(editEndTime));
    }

    private void showTimePicker(EditText target) {
        Calendar now = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, (TimePicker view, int hourOfDay, int minute) -> {
            target.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private void populateFields() {
        editStartTime.setText(originalTraining.getStartTime());
        editEndTime.setText(originalTraining.getEndTime());
        editNotes.setText(originalTraining.getNotes());
    }

    private void setupSave() {
        btnSave.setOnClickListener(v -> saveTraining());
    }

    private void saveTraining() {
        int teamPos = spinnerTeam.getSelectedItemPosition();
        if (teamPos < 0 || teams.isEmpty()) {
            Toast.makeText(this, "בחר קבוצה", Toast.LENGTH_SHORT).show();
            return;
        }

        int courtPos = spinnerCourt.getSelectedItemPosition();
        if (courtPos < 0 || courts.isEmpty()) {
            Toast.makeText(this, "בחר מגרש", Toast.LENGTH_SHORT).show();
            return;
        }

        Team team = teams.get(teamPos);
        Court court = courts.get(courtPos);
        String start = editStartTime.getText().toString().trim();
        String end = editEndTime.getText().toString().trim();
        String notes = editNotes.getText().toString().trim();

        if (TextUtils.isEmpty(start) || TextUtils.isEmpty(end)) {
            Toast.makeText(this, "שעות התחלה/סיום נדרשות", Toast.LENGTH_SHORT).show();
            return;
        }

        int startMinutes = timeToMinutesSafe(start);
        int endMinutes = timeToMinutesSafe(end);
        if (startMinutes < 0 || endMinutes < 0 || startMinutes >= endMinutes) {
            Toast.makeText(this, "שעת התחלה חייבת להיות לפני שעת סיום", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isWithinCourtHours(court, startMinutes, endMinutes)) {
            return;
        }

        // Create updated training
        Training updatedTraining = new Training();
        updatedTraining.setTrainingId(originalTraining.getTrainingId());
        updatedTraining.setTeamId(team.getTeamId());
        updatedTraining.setTeamName(team.getName());
        updatedTraining.setTeamColor(team.getColor());
        updatedTraining.setCourtId(court.getCourtId());
        updatedTraining.setCourtName(court.getName());
        updatedTraining.setCourtType(court.getCourtType());
        updatedTraining.setStartTime(start);
        updatedTraining.setEndTime(end);
        updatedTraining.setDate(selectedDate.getTimeInMillis());
        updatedTraining.setDayOfWeek(new SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDate.getTime()));
        updatedTraining.setNotes(notes);
        updatedTraining.setCreatedAt(originalTraining.getCreatedAt());

        // Check for conflicts with other trainings
        checkConflictAndUpdate(updatedTraining);
    }

    private void checkConflictAndUpdate(Training updatedTraining) {
        viewModel.addTraining(updatedTraining, new TrainingRepository.OnConflictCheckListener() {
            @Override
            public void onSuccess() {
                // First delete old training, then save new one
                viewModel.deleteTraining(originalTraining.getTrainingId());
                Toast.makeText(EditTrainingActivity.this, "אימון עודכן", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onConflict() {
                Toast.makeText(EditTrainingActivity.this, "התנגשות במגרש/זמן", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(EditTrainingActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTeams() {
        FirebaseDatabase.getInstance().getReference("teams")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        teams.clear();
                        List<String> names = new ArrayList<>();
                        int selectedPos = 0;
                        int pos = 0;

                        for (DataSnapshot child : snapshot.getChildren()) {
                            Team team = child.getValue(Team.class);
                            if (team != null) {
                                teams.add(team);
                                names.add(team.getName());
                                if (team.getTeamId().equals(originalTraining.getTeamId())) {
                                    selectedPos = pos;
                                }
                                pos++;
                            }
                        }

                        teamAdapter.clear();
                        teamAdapter.addAll(names);
                        teamAdapter.notifyDataSetChanged();
                        spinnerTeam.setSelection(selectedPos);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(EditTrainingActivity.this, "שגיאה בטעינת קבוצות", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCourts() {
        FirebaseDatabase.getInstance().getReference("courts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        courts.clear();
                        List<String> names = new ArrayList<>();
                        int selectedPos = 0;
                        int pos = 0;

                        for (DataSnapshot child : snapshot.getChildren()) {
                            Court court = child.getValue(Court.class);
                            if (court != null) {
                                courts.add(court);
                                names.add(court.getName());
                                if (court.getCourtId().equals(originalTraining.getCourtId())) {
                                    selectedPos = pos;
                                }
                                pos++;
                            }
                        }

                        courtAdapter.clear();
                        courtAdapter.addAll(names);
                        courtAdapter.notifyDataSetChanged();
                        spinnerCourt.setSelection(selectedPos);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(EditTrainingActivity.this, "שגיאה בטעינת מגרשים", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isWithinCourtHours(Court court, int startMinutes, int endMinutes) {
        int dayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK);
        DaySchedule schedule = court.getScheduleForDay(dayOfWeek);
        if (schedule == null || !schedule.isActive()) {
            Toast.makeText(this, "המגרש סגור ביום זה", Toast.LENGTH_SHORT).show();
            return false;
        }

        int open = timeToMinutesSafe(schedule.getOpeningHour());
        int close = timeToMinutesSafe(schedule.getClosingHour());
        if (open < 0 || close < 0 || open >= close) {
            Toast.makeText(this, "הגדרת שעות פעילות לא תקינה למגרש", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (startMinutes < open || endMinutes > close) {
            String msg = String.format(Locale.getDefault(), "שעות חורגות משעות פעילות (%s-%s)", schedule.getOpeningHour(), schedule.getClosingHour());
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private int timeToMinutesSafe(String time) {
        try {
            String[] parts = time.split(":");
            if (parts.length != 2) return -1;
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            if (h < 0 || h > 23 || m < 0 || m > 59) return -1;
            return h * 60 + m;
        } catch (Exception e) {
            return -1;
        }
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
