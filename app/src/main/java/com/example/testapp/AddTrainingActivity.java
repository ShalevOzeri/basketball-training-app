package com.example.testapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;

import com.example.testapp.models.DaySchedule;
import com.example.testapp.models.Court;
import com.example.testapp.models.Team;
import com.example.testapp.models.Training;
import com.example.testapp.repository.TrainingRepository;
import com.example.testapp.viewmodel.TrainingViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddTrainingActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private SearchView searchViewTeams;
    private SearchView searchViewCourts;
    private ChipGroup chipGroupTeams;
    private ChipGroup chipGroupCourts;
    private EditText editDate;
    private EditText editStartTime;
    private EditText editEndTime;
    private EditText editNotes;
    private MaterialButton btnSave;

    private TrainingViewModel viewModel;
    private final List<Team> teams = new ArrayList<>();
    private final Map<String, Team> teamMap = new HashMap<>();
    private final List<Court> courts = new ArrayList<>();
    private final Map<String, Court> courtMap = new HashMap<>();
    private final Calendar selectedDate = Calendar.getInstance();
    private Team selectedTeam;
    private Court selectedCourt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_training);

        initViews();
        setupToolbar();
        setupViewModel();
        setupPickers();
        setupSave();
        loadTeams();
        loadCourts();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        searchViewTeams = findViewById(R.id.searchViewTeams);
        searchViewCourts = findViewById(R.id.searchViewCourts);
        chipGroupTeams = findViewById(R.id.chipGroupTeams);
        chipGroupCourts = findViewById(R.id.chipGroupCourts);
        editDate = findViewById(R.id.editDate);
        editStartTime = findViewById(R.id.editStartTime);
        editEndTime = findViewById(R.id.editEndTime);
        editNotes = findViewById(R.id.editNotes);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("אימון חדש");
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

        // Setup search listeners for teams
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

        // Setup search listeners for courts
        searchViewCourts.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterCourtChips(newText);
                return true;
            }
        });
    }

    private void setupTeamChipGroup() {
        chipGroupTeams.removeAllViews();
        for (Team team : teams) {
            Chip chip = new Chip(this);
            chip.setText(team.getName());
            chip.setChipBackgroundColorResource(android.R.color.white);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedTeam = team;
                }
            });
            chipGroupTeams.addView(chip);
        }
    }

    private void setupCourtChipGroup() {
        chipGroupCourts.removeAllViews();
        for (Court court : courts) {
            Chip chip = new Chip(this);
            chip.setText(court.getName());
            chip.setChipBackgroundColorResource(android.R.color.white);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCourt = court;
                }
            });
            chipGroupCourts.addView(chip);
        }
    }

    private void filterTeamChips(String query) {
        String lowerQuery = query.toLowerCase();
        for (int i = 0; i < chipGroupTeams.getChildCount(); i++) {
            View child = chipGroupTeams.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                String teamName = chip.getText().toString();
                chip.setVisibility(teamName.toLowerCase().contains(lowerQuery) ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void filterCourtChips(String query) {
        String lowerQuery = query.toLowerCase();
        for (int i = 0; i < chipGroupCourts.getChildCount(); i++) {
            View child = chipGroupCourts.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                String courtName = chip.getText().toString();
                chip.setVisibility(courtName.toLowerCase().contains(lowerQuery) ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void setupPickers() {
        Locale hebrewLocale = new Locale("he", "IL");
        SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy", hebrewLocale);
        editDate.setText(dateFmt.format(selectedDate.getTime()));

        editDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(this, android.R.style.Theme_Material_Light_Dialog_Alert, (DatePicker view, int year, int month, int dayOfMonth) -> {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                editDate.setText(dateFmt.format(selectedDate.getTime()));
            }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
            // Prevent selecting past dates
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialog.show();
        });

        editStartTime.setOnClickListener(v -> showTimePicker(editStartTime));
        editEndTime.setOnClickListener(v -> showTimePicker(editEndTime));
    }

    private void showTimePicker(EditText target) {
        Calendar now = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(this, TimePickerDialog.THEME_HOLO_LIGHT, (TimePicker view, int hourOfDay, int minute) -> {
            target.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute));
        }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private void setupSave() {
        btnSave.setOnClickListener(v -> {
            if (selectedTeam == null) {
                Toast.makeText(this, "בחר קבוצה", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedCourt == null) {
                Toast.makeText(this, "בחר מגרש", Toast.LENGTH_SHORT).show();
                return;
            }

            Team team = selectedTeam;
            Court court = selectedCourt;
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
                return; // Toast already shown
            }

            // Check if date is in the past
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            
            Calendar selectedDay = (Calendar) selectedDate.clone();
            selectedDay.set(Calendar.HOUR_OF_DAY, 0);
            selectedDay.set(Calendar.MINUTE, 0);
            selectedDay.set(Calendar.SECOND, 0);
            selectedDay.set(Calendar.MILLISECOND, 0);
            
            if (selectedDay.before(today)) {
                Toast.makeText(this, "לא ניתן להוסיף אימונים לתאריכים שעברו", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // If today, check if the time has already passed
            if (selectedDay.equals(today)) {
                Calendar now = Calendar.getInstance();
                int currentHour = now.get(Calendar.HOUR_OF_DAY);
                int currentMinute = now.get(Calendar.MINUTE);
                int currentTimeInMinutes = currentHour * 60 + currentMinute;
                
                if (startMinutes <= currentTimeInMinutes) {
                    Toast.makeText(this, "לא ניתן להוסיף אימונים בשעות שכבר עברו", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Training training = new Training();
            training.setTeamId(team.getTeamId());
            training.setTeamName(team.getName());
            training.setTeamColor(team.getColor());
            training.setCourtId(court.getCourtId());
            training.setCourtName(court.getName());
            training.setCourtType(court.getCourtType());
            training.setStartTime(start);
            training.setEndTime(end);
            
            // Store date as midnight LOCAL time (not UTC) to match filtering in ScheduleFragment/AllCourtsViewFragment
            Calendar localMidnight = Calendar.getInstance();
            localMidnight.set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            localMidnight.set(Calendar.MILLISECOND, 0);
            training.setDate(localMidnight.getTimeInMillis());
            
            Locale hebrewLocale = new Locale("he", "IL");
            training.setDayOfWeek(new SimpleDateFormat("EEEE", hebrewLocale).format(selectedDate.getTime()));
            training.setNotes(notes);
            training.setCreatedAt(System.currentTimeMillis());

            viewModel.addTraining(training, new TrainingRepository.OnConflictCheckListener() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(AddTrainingActivity.this, "אימון נוסף", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onConflict() {
                    runOnUiThread(() -> Toast.makeText(AddTrainingActivity.this, "התנגשות במגרש/זמן", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> Toast.makeText(AddTrainingActivity.this, error, Toast.LENGTH_SHORT).show());
                }
            });
        });
    }

    private void loadTeams() {
        FirebaseDatabase.getInstance().getReference("teams")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        teams.clear();
                        teamMap.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Team team = child.getValue(Team.class);
                            if (team != null) {
                                teams.add(team);
                                teamMap.put(team.getTeamId(), team);
                            }
                        }
                        setupTeamChipGroup();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(AddTrainingActivity.this, "שגיאה בטעינת קבוצות", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCourts() {
        FirebaseDatabase.getInstance().getReference("courts")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        courts.clear();
                        courtMap.clear();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Court court = child.getValue(Court.class);
                            if (court != null) {
                                courts.add(court);
                                courtMap.put(court.getCourtId(), court);
                            }
                        }
                        setupCourtChipGroup();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(AddTrainingActivity.this, "שגיאה בטעינת מגרשים", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isWithinCourtHours(Court court, int startMinutes, int endMinutes) {
        int dayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK); // Sunday=1
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
