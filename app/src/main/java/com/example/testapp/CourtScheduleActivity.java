package com.example.testapp;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testapp.models.Court;
import com.example.testapp.models.DaySchedule;
import com.example.testapp.repository.CourtRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CourtScheduleActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView courtNameText;
    private MaterialButton saveButton;
    
    private Court court;
    private CourtRepository courtRepository;
    
    // Day layouts and controls
    private Map<Integer, DayControls> dayControlsMap;
    
    private static class DayControls {
        View layout;
        TextView dayName;
        SwitchMaterial activeSwitch;
        LinearLayout timeLayout;
        TextInputEditText openingHour;
        TextInputEditText closingHour;
        
        DayControls(View layout, TextView dayName, SwitchMaterial activeSwitch,
                   LinearLayout timeLayout, TextInputEditText openingHour, 
                   TextInputEditText closingHour) {
            this.layout = layout;
            this.dayName = dayName;
            this.activeSwitch = activeSwitch;
            this.timeLayout = timeLayout;
            this.openingHour = openingHour;
            this.closingHour = closingHour;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_court_schedule);
        
        courtRepository = new CourtRepository();
        
        // Get court from intent
        String courtId = getIntent().getStringExtra("COURT_ID");
        if (courtId == null) {
            Toast.makeText(this, "שגיאה: לא נמצא מגרש", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        initializeViews();
        setupToolbar();
        loadCourt(courtId);
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        courtNameText = findViewById(R.id.courtNameText);
        saveButton = findViewById(R.id.saveScheduleButton);
        
        dayControlsMap = new HashMap<>();
        
        // Initialize controls for each day
        initializeDayControls(1, R.id.sundayLayout, "ראשון");
        initializeDayControls(2, R.id.mondayLayout, "שני");
        initializeDayControls(3, R.id.tuesdayLayout, "שלישי");
        initializeDayControls(4, R.id.wednesdayLayout, "רביעי");
        initializeDayControls(5, R.id.thursdayLayout, "חמישי");
        initializeDayControls(6, R.id.fridayLayout, "שישי");
        initializeDayControls(7, R.id.saturdayLayout, "שבת");
        
        saveButton.setOnClickListener(v -> saveSchedule());
    }
    
    private void initializeDayControls(int dayOfWeek, int layoutId, String dayName) {
        View dayLayout = findViewById(layoutId);
        TextView dayNameText = dayLayout.findViewById(R.id.dayNameText);
        SwitchMaterial activeSwitch = dayLayout.findViewById(R.id.dayActiveSwitch);
        LinearLayout timeLayout = dayLayout.findViewById(R.id.timeSelectionLayout);
        TextInputEditText openingHour = dayLayout.findViewById(R.id.openingHourEdit);
        TextInputEditText closingHour = dayLayout.findViewById(R.id.closingHourEdit);
        
        dayNameText.setText(dayName);
        
        // Setup switch listener
        activeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            timeLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        
        // Setup time pickers
        openingHour.setOnClickListener(v -> showTimePicker(openingHour, true));
        closingHour.setOnClickListener(v -> showTimePicker(closingHour, false));
        
        DayControls controls = new DayControls(dayLayout, dayNameText, activeSwitch, 
                                               timeLayout, openingHour, closingHour);
        dayControlsMap.put(dayOfWeek, controls);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("לוח זמנים שבועי");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void loadCourt(String courtId) {
        courtRepository.getCourt(courtId, new CourtRepository.OnCourtLoadedListener() {
            @Override
            public void onCourtLoaded(Court loadedCourt) {
                court = loadedCourt;
                courtNameText.setText(court.getName());
                populateSchedule();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(CourtScheduleActivity.this, "שגיאה: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void populateSchedule() {
        for (int day = 1; day <= 7; day++) {
            DayControls controls = dayControlsMap.get(day);
            if (controls != null) {
                DaySchedule schedule = court.getScheduleForDay(day);
                if (schedule != null) {
                    controls.activeSwitch.setChecked(schedule.isActive());
                    controls.timeLayout.setVisibility(schedule.isActive() ? View.VISIBLE : View.GONE);
                    controls.openingHour.setText(schedule.getOpeningHour());
                    controls.closingHour.setText(schedule.getClosingHour());
                } else {
                    // Default values
                    controls.activeSwitch.setChecked(true);
                    controls.openingHour.setText("08:00");
                    controls.closingHour.setText("22:00");
                }
            }
        }
    }
    
    private void saveSchedule() {
        // Update court's weekly schedule
        for (int day = 1; day <= 7; day++) {
            DayControls controls = dayControlsMap.get(day);
            if (controls != null) {
                boolean isActive = controls.activeSwitch.isChecked();
                String opening = controls.openingHour.getText() != null ? 
                                controls.openingHour.getText().toString() : "08:00";
                String closing = controls.closingHour.getText() != null ? 
                                controls.closingHour.getText().toString() : "22:00";
                
                DaySchedule schedule = new DaySchedule(isActive, opening, closing);
                court.setScheduleForDay(day, schedule);
            }
        }
        
        // Save to repository
        courtRepository.updateCourt(court, new CourtRepository.OnCourtUpdatedListener() {
            @Override
            public void onCourtUpdated() {
                Toast.makeText(CourtScheduleActivity.this, "לוח הזמנים נשמר בהצלחה", Toast.LENGTH_SHORT).show();
                finish();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(CourtScheduleActivity.this, "שגיאה בשמירה: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showTimePicker(TextInputEditText editText, boolean isOpeningHour) {
        // Parse current time or use default
        String currentTime = editText.getText() != null ? editText.getText().toString() : "08:00";
        String[] parts = currentTime.split(":");
        int hour = 8;
        int minute = 0;
        
        try {
            if (parts.length >= 2) {
                hour = Integer.parseInt(parts[0]);
                minute = Integer.parseInt(parts[1]);
            }
        } catch (NumberFormatException e) {
            // Use defaults
        }
        
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, selectedHour, selectedMinute) -> {
                String time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                editText.setText(time);
            },
            hour,
            minute,
            true // 24-hour format
        );
        
        timePickerDialog.show();
    }
}
