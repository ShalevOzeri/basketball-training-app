package com.example.testapp;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testapp.models.Court;
import com.example.testapp.models.DaySchedule;
import com.example.testapp.repository.CourtRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddEditCourtActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private EditText courtNameInput, courtLocationInput;
    private Button saveButton;
    
    private CourtRepository courtRepository;
    
    // Day layouts and controls
    private Map<Integer, DayControls> dayControlsMap;
    private String courtId; // null for new court
    
    private static class DayControls {
        TextView dayName;
        SwitchMaterial activeSwitch;
        LinearLayout timeLayout;
        EditText openingHour;
        EditText closingHour;
        
        DayControls(TextView dayName, SwitchMaterial activeSwitch,
                   LinearLayout timeLayout, EditText openingHour, 
                   EditText closingHour) {
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
        setContentView(R.layout.activity_add_edit_court);
        
        courtRepository = new CourtRepository();
        
        // Check if editing existing court
        courtId = getIntent().getStringExtra("COURT_ID");
        
        initializeViews();
        setupToolbar();
        
        if (courtId != null) {
            loadCourt(courtId);
        }
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        courtNameInput = findViewById(R.id.courtNameInput);
        courtLocationInput = findViewById(R.id.courtLocationInput);
        saveButton = findViewById(R.id.saveButton);
        
        dayControlsMap = new HashMap<>();
        
        // Initialize controls for each day
        initializeDayControls(1, R.id.sundayLayout, "ראשון");
        initializeDayControls(2, R.id.mondayLayout, "שני");
        initializeDayControls(3, R.id.tuesdayLayout, "שלישי");
        initializeDayControls(4, R.id.wednesdayLayout, "רביעי");
        initializeDayControls(5, R.id.thursdayLayout, "חמישי");
        initializeDayControls(6, R.id.fridayLayout, "שישי");
        initializeDayControls(7, R.id.saturdayLayout, "שבת");
        
        saveButton.setOnClickListener(v -> saveCourt());
    }
    
    private void initializeDayControls(int dayOfWeek, int layoutId, String dayName) {
        View dayLayout = findViewById(layoutId);
        TextView dayNameText = dayLayout.findViewById(R.id.dayNameText);
        SwitchMaterial activeSwitch = dayLayout.findViewById(R.id.dayActiveSwitch);
        LinearLayout timeLayout = dayLayout.findViewById(R.id.timeSelectionLayout);
        EditText openingHour = dayLayout.findViewById(R.id.openingHourEdit);
        EditText closingHour = dayLayout.findViewById(R.id.closingHourEdit);
        
        dayNameText.setText(dayName);
        
        // Setup switch listener
        activeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            timeLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });
        
        // Setup time pickers
        openingHour.setOnClickListener(v -> showTimePicker(openingHour, true));
        closingHour.setOnClickListener(v -> showTimePicker(closingHour, false));
        
        DayControls controls = new DayControls(dayNameText, activeSwitch, 
                                               timeLayout, openingHour, closingHour);
        dayControlsMap.put(dayOfWeek, controls);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(courtId == null ? "הוספת מגרש" : "עריכת מגרש");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void loadCourt(String courtId) {
        courtRepository.getCourt(courtId, new CourtRepository.OnCourtLoadedListener() {
            @Override
            public void onCourtLoaded(Court court) {
                courtNameInput.setText(court.getName());
                courtLocationInput.setText(court.getLocation());
                populateSchedule(court);
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(AddEditCourtActivity.this, "שגיאה: " + error, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void populateSchedule(Court court) {
        for (int day = 1; day <= 7; day++) {
            DayControls controls = dayControlsMap.get(day);
            if (controls != null) {
                DaySchedule schedule = court.getScheduleForDay(day);
                if (schedule != null) {
                    controls.activeSwitch.setChecked(schedule.isActive());
                    controls.timeLayout.setVisibility(schedule.isActive() ? View.VISIBLE : View.GONE);
                    controls.openingHour.setText(schedule.getOpeningHour());
                    controls.closingHour.setText(schedule.getClosingHour());
                }
            }
        }
    }
    
    private void saveCourt() {
        String name = courtNameInput.getText() != null ? courtNameInput.getText().toString().trim() : "";
        String location = courtLocationInput.getText() != null ? courtLocationInput.getText().toString().trim() : "";
        
        if (name.isEmpty()) {
            Toast.makeText(this, "יש להזין שם מגרש", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (location.isEmpty()) {
            location = "ללא מיקום";
        }
        
        // Create court
        Court court;
        if (courtId != null) {
            // Editing existing court
            court = new Court();
            court.setCourtId(courtId);
        } else {
            // Creating new court
            court = new Court();
        }
        
        court.setName(name);
        court.setLocation(location);
        court.setAvailable(true);
        
        // Set weekly schedule
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
        
        // Set legacy fields for backward compatibility
        DayControls firstActiveDay = findFirstActiveDay();
        if (firstActiveDay != null) {
            court.setOpeningHour(firstActiveDay.openingHour.getText().toString());
            court.setClosingHour(firstActiveDay.closingHour.getText().toString());
        } else {
            court.setOpeningHour("08:00");
            court.setClosingHour("22:00");
        }
        court.setActiveDays(buildActiveDaysString());
        court.setCreatedAt(System.currentTimeMillis());
        
        // Save to repository
        if (courtId != null) {
            courtRepository.updateCourt(court, new CourtRepository.OnCourtUpdatedListener() {
                @Override
                public void onCourtUpdated() {
                    Toast.makeText(AddEditCourtActivity.this, "המגרש עודכן בהצלחה", Toast.LENGTH_SHORT).show();
                    finish();
                }
                
                @Override
                public void onError(String error) {
                    Toast.makeText(AddEditCourtActivity.this, "שגיאה בעדכון: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            courtRepository.addCourt(court);
            Toast.makeText(this, "מגרש נוסף: " + name, Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private DayControls findFirstActiveDay() {
        for (int day = 1; day <= 7; day++) {
            DayControls controls = dayControlsMap.get(day);
            if (controls != null && controls.activeSwitch.isChecked()) {
                return controls;
            }
        }
        return null;
    }
    
    private String buildActiveDaysString() {
        StringBuilder result = new StringBuilder();
        for (int day = 1; day <= 7; day++) {
            DayControls controls = dayControlsMap.get(day);
            if (controls != null && controls.activeSwitch.isChecked()) {
                if (result.length() > 0) {
                    result.append(",");
                }
                result.append(day);
            }
        }
        return result.length() > 0 ? result.toString() : "1,2,3,4,5,6,7";
    }
    
    private void showTimePicker(EditText editText, boolean isOpeningHour) {
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
