package com.example.testapp.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

import com.example.testapp.R;
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

public class EditTrainingFragment extends Fragment {

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
    private Training originalTraining;
    private final List<Team> teams = new ArrayList<>();
    private final Map<String, Team> teamMap = new HashMap<>();
    private final List<Court> courts = new ArrayList<>();
    private final Map<String, Court> courtMap = new HashMap<>();
    private final Calendar selectedDate = Calendar.getInstance();
    private Team selectedTeam;
    private Court selectedCourt;

    private String trainingId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_training, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get trainingId from arguments
        if (getArguments() != null) {
            trainingId = getArguments().getString("trainingId");
        }

        if (trainingId == null || trainingId.isEmpty()) {
            Toast.makeText(requireContext(), "שגיאה: לא נמצא אימון לעריכה", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }

        initViews(view);
        setupToolbar(view);
        setupViewModel();
        setupPickers();
        setupSave();
        
        // Load training first, then teams and courts
        loadTraining();
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        searchViewTeams = view.findViewById(R.id.searchViewTeams);
        searchViewCourts = view.findViewById(R.id.searchViewCourts);
        chipGroupTeams = view.findViewById(R.id.chipGroupTeams);
        chipGroupCourts = view.findViewById(R.id.chipGroupCourts);
        editDate = view.findViewById(R.id.editDate);
        editStartTime = view.findViewById(R.id.editStartTime);
        editEndTime = view.findViewById(R.id.editEndTime);
        editNotes = view.findViewById(R.id.editNotes);
        btnSave = view.findViewById(R.id.btnSave);
    }

    private void setupToolbar(View view) {
        NavController navController = Navigation.findNavController(view);
        
        // Set as action bar
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        
        // Setup navigation
        NavigationUI.setupWithNavController(toolbar, navController);
        
        // Set title
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("עריכת אימון");
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TrainingViewModel.class);
        viewModel.getErrors().observe(getViewLifecycleOwner(), err -> {
            if (err != null) {
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
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

    private void loadTraining() {
        FirebaseDatabase.getInstance().getReference("trainings").child(trainingId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        originalTraining = snapshot.getValue(Training.class);
                        if (originalTraining == null) {
                            Toast.makeText(requireContext(), "שגיאה: לא נמצא אימון", Toast.LENGTH_SHORT).show();
                            if (getView() != null) {
                                Navigation.findNavController(getView()).navigateUp();
                            }
                            return;
                        }
                        
                        selectedDate.setTimeInMillis(originalTraining.getDate());
                        
                        // Now load teams and courts
                        loadTeams();
                        loadCourts();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(requireContext(), "שגיאה בטעינת אימון: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        if (getView() != null) {
                            Navigation.findNavController(getView()).navigateUp();
                        }
                    }
                });
    }

    private void populateFields() {
        if (originalTraining == null) return;
        
        // Update the date field
        Locale hebrewLocale = new Locale("he", "IL");
        SimpleDateFormat dateFmt = new SimpleDateFormat("dd/MM/yyyy", hebrewLocale);
        editDate.setText(dateFmt.format(selectedDate.getTime()));
        
        editStartTime.setText(originalTraining.getStartTime());
        editEndTime.setText(originalTraining.getEndTime());
        editNotes.setText(originalTraining.getNotes());
        
        // Set selected team and court from original training
        for (Team team : teams) {
            if (team.getTeamId().equals(originalTraining.getTeamId())) {
                selectedTeam = team;
                break;
            }
        }
        
        for (Court court : courts) {
            if (court.getCourtId().equals(originalTraining.getCourtId())) {
                selectedCourt = court;
                break;
            }
        }
        
        setupTeamChipGroup();
        setupCourtChipGroup();
    }

    private void setupTeamChipGroup() {
        chipGroupTeams.removeAllViews();
        for (Team team : teams) {
            Chip chip = new Chip(requireContext());
            chip.setText(team.getName());
            chip.setChipBackgroundColorResource(android.R.color.white);
            chip.setCheckable(true);
            
            // Check if this is the selected team
            if (selectedTeam != null && team.getTeamId().equals(selectedTeam.getTeamId())) {
                chip.setChecked(true);
            }
            
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
            Chip chip = new Chip(requireContext());
            chip.setText(court.getName());
            chip.setChipBackgroundColorResource(android.R.color.white);
            chip.setCheckable(true);
            
            // Check if this is the selected court
            if (selectedCourt != null && court.getCourtId().equals(selectedCourt.getCourtId())) {
                chip.setChecked(true);
            }
            
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

        // Allow both tapping to open the dialog and manual typing
        editDate.setOnClickListener(v -> {
            DatePicker datePicker = new DatePicker(requireContext());
            datePicker.init(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH), (view, year, monthOfYear, dayOfMonth) -> {
                // Update selectedDate whenever user changes date in the picker
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, monthOfYear);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            });
            
            // Wrap DatePicker with proper layout parameters so buttons are visible
            LinearLayout container = new LinearLayout(requireContext());
            container.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            LinearLayout.LayoutParams pickerParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    400  // Fixed height for DatePicker
            );
            datePicker.setLayoutParams(pickerParams);
            container.addView(datePicker);
            
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
            builder.setTitle(getString(R.string.date_picker_title))
                    .setView(container)
                    .setPositiveButton(getString(R.string.date_picker_save), (dialog, which) -> {
                        editDate.setText(dateFmt.format(selectedDate.getTime()));
                    })
                    .setNegativeButton(getString(R.string.date_picker_cancel), (dialog, which) -> {
                        // Restore original date if cancelled
                        dialog.cancel();
                    })
                    .show();
        });
        
        // Update the date when typing manually and leaving the field
        editDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String dateText = editDate.getText().toString();
                try {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(dateFmt.parse(dateText));
                    selectedDate.set(Calendar.YEAR, cal.get(Calendar.YEAR));
                    selectedDate.set(Calendar.MONTH, cal.get(Calendar.MONTH));
                    selectedDate.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
                } catch (Exception e) {
                    // If the format is invalid, revert to the saved date
                    editDate.setText(dateFmt.format(selectedDate.getTime()));
                }
            }
        });

        // Allow both tapping to open the dialog and manual typing
        editStartTime.setOnClickListener(v -> showTimePicker(editStartTime));
        editEndTime.setOnClickListener(v -> showTimePicker(editEndTime));
        
        // Normalize the start time when leaving the field
        editStartTime.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String timeText = editStartTime.getText().toString().trim();
                String formatted = formatTimeInput(timeText);
                if (formatted != null) {
                    editStartTime.setText(formatted);
                }
            }
        });
        
        // Normalize the end time when leaving the field
        editEndTime.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String timeText = editEndTime.getText().toString().trim();
                String formatted = formatTimeInput(timeText);
                if (formatted != null) {
                    editEndTime.setText(formatted);
                }
            }
        });
    }
    
    /**
     * Formats a time input into HH:mm.
     * Supports inputs like: "9" -> "09:00", "15" -> "15:00", "9:30" -> "09:30", "930" -> "09:30".
     */
    private String formatTimeInput(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }
        
        // Already in HH:mm format
        if (input.matches("\\d{2}:\\d{2}")) {
            return input;
        }
        
        // Digits only, without a colon
        if (input.matches("\\d+")) {
            int num = Integer.parseInt(input);
            
            // Single number (0-23) treated as hour
            if (num >= 0 && num <= 23) {
                return String.format(Locale.getDefault(), "%02d:00", num);
            }
            
            // Three to four digits (e.g., 930 or 1530)
            if (input.length() == 3 || input.length() == 4) {
                int hours = num / 100;
                int minutes = num % 100;
                if (hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59) {
                    return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
                }
            }
        }
        
        // Pattern H:mm (e.g., 9:30)
        if (input.matches("\\d{1}:\\d{2}")) {
            String[] parts = input.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            if (hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59) {
                return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
            }
        }
        
        // Pattern HH:m (e.g., 09:5)
        if (input.matches("\\d{2}:\\d{1}")) {
            String[] parts = input.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            if (hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59) {
                return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
            }
        }
        
        // Otherwise, leave the input unchanged
        return input;
    }

    private void showTimePicker(EditText target) {
        Calendar now = Calendar.getInstance();
        
        // Build a spinner-style TimePicker via ContextThemeWrapper
        // Force spinner look using Theme.Holo.Light.Dialog
        android.view.ContextThemeWrapper themedContext = new android.view.ContextThemeWrapper(
            requireContext(), android.R.style.Theme_Holo_Light_Dialog);
        final TimePicker timePicker = new TimePicker(themedContext);
        
        timePicker.setIs24HourView(true);
        timePicker.setHour(now.get(Calendar.HOUR_OF_DAY));
        timePicker.setMinute(now.get(Calendar.MINUTE));
        
        // Create dialog
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("בחר שעה")
            .setView(timePicker)
            .setPositiveButton("שמור", (dialog, which) -> {
                // Force focus clear so typed values apply
                timePicker.clearFocus();
                
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                target.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
            })
            .setNegativeButton("ביטול", null)
            .show();
    }

    private void setupSave() {
        btnSave.setOnClickListener(v -> saveTraining());
    }

    private void saveTraining() {
        if (selectedTeam == null) {
            Toast.makeText(requireContext(), "בחר קבוצה", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedCourt == null) {
            Toast.makeText(requireContext(), "בחר מגרש", Toast.LENGTH_SHORT).show();
            return;
        }

        Team team = selectedTeam;
        Court court = selectedCourt;
        String start = editStartTime.getText().toString().trim();
        String end = editEndTime.getText().toString().trim();
        String notes = editNotes.getText().toString().trim();

        if (TextUtils.isEmpty(start) || TextUtils.isEmpty(end)) {
            Toast.makeText(requireContext(), "שעות התחלה/סיום נדרשות", Toast.LENGTH_SHORT).show();
            return;
        }

        int startMinutes = timeToMinutesSafe(start);
        int endMinutes = timeToMinutesSafe(end);
        if (startMinutes < 0 || endMinutes < 0 || startMinutes >= endMinutes) {
            Toast.makeText(requireContext(), "שעת התחלה חייבת להיות לפני שעת סיום", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isWithinCourtHours(court, startMinutes, endMinutes)) {
            return;
        }
        
        // Check if training time is in the past (only for today)
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
        
        // If today, check if the time has already passed
        if (selectedDay.equals(today)) {
            Calendar now = Calendar.getInstance();
            int currentHour = now.get(Calendar.HOUR_OF_DAY);
            int currentMinute = now.get(Calendar.MINUTE);
            int currentTimeInMinutes = currentHour * 60 + currentMinute;
            
            if (startMinutes <= currentTimeInMinutes) {
                Toast.makeText(requireContext(), "לא ניתן להוסיף אימונים בשעות שכבר עברו", Toast.LENGTH_SHORT).show();
                return;
            }
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
        
        // Store date as midnight LOCAL time (not UTC) to match filtering in ScheduleFragment/AllCourtsViewFragment
        Calendar localMidnight = Calendar.getInstance();
        localMidnight.set(selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        localMidnight.set(Calendar.MILLISECOND, 0);
        updatedTraining.setDate(localMidnight.getTimeInMillis());
        
        Locale hebrewLocale = new Locale("he", "IL");
        updatedTraining.setDayOfWeek(new SimpleDateFormat("EEEE", hebrewLocale).format(selectedDate.getTime()));
        updatedTraining.setNotes(notes);
        updatedTraining.setCreatedAt(originalTraining.getCreatedAt());

        // Check for conflicts with other trainings
        checkConflictAndUpdate(updatedTraining);
    }

    private void checkConflictAndUpdate(Training updatedTraining) {
        // Use the version of addTraining that excludes the original training from conflict check
        viewModel.addTraining(updatedTraining, originalTraining.getTrainingId(), new TrainingRepository.OnConflictCheckListener() {
            @Override
            public void onSuccess() {
                // First delete old training, then save new one
                viewModel.deleteTraining(originalTraining.getTrainingId());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(requireContext(), "אימון עודכן", Toast.LENGTH_SHORT).show();
                        if (getView() != null) {
                            Navigation.findNavController(getView()).navigateUp();
                        }
                    });
                }
            }

            @Override
            public void onConflict() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "התנגשות במגרש/זמן", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show());
                }
            }
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

                        // Check if courts are also loaded
                        if (!courts.isEmpty()) {
                            populateFields();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(requireContext(), "שגיאה בטעינת קבוצות", Toast.LENGTH_SHORT).show();
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

                        // Check if teams are also loaded
                        if (!teams.isEmpty()) {
                            populateFields();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(requireContext(), "שגיאה בטעינת מגרשים", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isWithinCourtHours(Court court, int startMinutes, int endMinutes) {
        int dayOfWeek = selectedDate.get(Calendar.DAY_OF_WEEK);
        DaySchedule schedule = court.getScheduleForDay(dayOfWeek);
        if (schedule == null || !schedule.isActive()) {
            Toast.makeText(requireContext(), "המגרש סגור ביום זה", Toast.LENGTH_SHORT).show();
            return false;
        }

        int open = timeToMinutesSafe(schedule.getOpeningHour());
        int close = timeToMinutesSafe(schedule.getClosingHour());
        if (open < 0 || close < 0 || open >= close) {
            Toast.makeText(requireContext(), "הגדרת שעות פעילות לא תקינה למגרש", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (startMinutes < open || endMinutes > close) {
            String msg = String.format(Locale.getDefault(), "שעות חורגות משעות פעילות (%s-%s)", schedule.getOpeningHour(), schedule.getClosingHour());
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
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
}
