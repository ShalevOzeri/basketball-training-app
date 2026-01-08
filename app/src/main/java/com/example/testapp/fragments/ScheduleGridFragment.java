package com.example.testapp.fragments;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TableLayout;
import android.widget.ScrollView;
import android.widget.HorizontalScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.R;
import com.example.testapp.adapters.TeamSelectionAdapter;
import com.example.testapp.adapters.Schedule2DAdapter;
import com.example.testapp.models.Court;
import com.example.testapp.models.DaySchedule;
import com.example.testapp.models.Team;
import com.example.testapp.models.TimeSlot;
import com.example.testapp.models.Training;
import com.example.testapp.repository.TrainingRepository;
import com.example.testapp.viewmodel.CourtViewModel;
import com.example.testapp.viewmodel.TeamViewModel;
import com.example.testapp.viewmodel.TrainingViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

/**
 * Fragment for displaying weekly basketball court schedule grid.
 * Shows one selected court across 7 days with 30-minute time slots.
 */
public class ScheduleGridFragment extends Fragment {

    // Constants
    private static final int TIME_SLOT_DURATION = 30; // minutes
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd", Locale.getDefault());
    private static final SimpleDateFormat DAY_FORMAT = new SimpleDateFormat("EEE", Locale.getDefault());

    // Views
    private TextView tvSelectedCourt;
    private ChipGroup chipGroupCourts;
    private MaterialButton btnPreviousWeek;
    private MaterialButton btnNextWeek;
    private MaterialButton btnToggleDayView;
    private TextView tvWeekDisplay;
    private TextView tvSelectedDay;
    private TableLayout scheduleTable;
    private ScrollView verticalScrollView;
    private HorizontalScrollView horizontalScrollView;
    private TextView tvEmptyState;

    // Data
    private Court selectedCourt;
    private Calendar weekStartDate;
    private boolean isDayView = false;
    private int selectedDayIndex = 0; // 0 = Sunday
    private List<Court> courts = new ArrayList<>();
    private List<Team> teams = new ArrayList<>();
    private List<Training> trainings = new ArrayList<>();
    private com.example.testapp.models.User currentUser;
    private boolean isReadOnlyMode = false;
    private boolean userLoaded = false; // Track if user was loaded to prevent showing all trainings before filtering

    // Adapter
    private Schedule2DAdapter adapter2D;

    // ViewModels
    private CourtViewModel courtViewModel;
    private TeamViewModel teamViewModel;
    private TrainingViewModel trainingViewModel;

    // Repository
    private TrainingRepository trainingRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule_grid_2d, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Initialize views
            tvSelectedCourt = view.findViewById(R.id.tvSelectedCourt);
            chipGroupCourts = view.findViewById(R.id.chipGroupCourts);
            btnPreviousWeek = view.findViewById(R.id.btnPreviousWeek);
            btnNextWeek = view.findViewById(R.id.btnNextWeek);
            btnToggleDayView = view.findViewById(R.id.btnToggleDayView);
            tvWeekDisplay = view.findViewById(R.id.tvWeekDisplay);
            tvSelectedDay = view.findViewById(R.id.tvSelectedDay);
            scheduleTable = view.findViewById(R.id.scheduleTable);
            verticalScrollView = view.findViewById(R.id.verticalScrollView);
            horizontalScrollView = view.findViewById(R.id.horizontalScrollView);
            tvEmptyState = view.findViewById(R.id.tvEmptyState);

            android.util.Log.d("ScheduleGridFragment", "All views initialized successfully");

            // Initialize repository
            trainingRepository = new TrainingRepository();

            // Set week to current week
            weekStartDate = Calendar.getInstance();
            weekStartDate = getWeekStart(weekStartDate);
            selectedDayIndex = weekStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY; // default to Sunday of that week

            // Setup 2D Adapter
            adapter2D = new Schedule2DAdapter(getContext(), scheduleTable, weekStartDate);
            
            // Set slot click listener - show team selection dialog
            adapter2D.setSlotClickListener(timeSlot -> {
                showTeamSelectionDialog(timeSlot);
            });
            
            // Set occupied slot listener for editing/deleting trainings
            adapter2D.setOccupiedSlotListener(timeSlot -> {
                if (timeSlot.getTraining() != null) {
                    Training completeTraining = findTrainingInList(timeSlot.getTraining());
                    if (completeTraining != null) {
                        showEditTrainingDialog(completeTraining, timeSlot.getDate());
                    } else {
                        showEditTrainingDialog(timeSlot.getTraining(), timeSlot.getDate());
                    }
                }
            });

            // Setup ViewModels and UI
            loadCurrentUser();
            setupViewModels();
            setupUI();
            updateWeekDisplay();
            updateSelectedDayLabel();
        } catch (Exception e) {
            android.util.Log.e("ScheduleGridFragment", "Error in onViewCreated", e);
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "שגיאה בטעינת הדף: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload current user to ensure proper filtering when returning to this screen
        android.util.Log.d("ScheduleGridFragment", "onResume - reloading current user");
        userLoaded = false; // Reset flag to prevent showing unfiltered trainings
        loadCurrentUser();
    }

    /**
     * Load current user and determine permissions
     */
    private void loadCurrentUser() {
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ?
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        
        if (userId == null) {
            isReadOnlyMode = true;
            return;
        }

        com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                        currentUser = snapshot.getValue(com.example.testapp.models.User.class);
                        if (currentUser != null) {
                            android.util.Log.d("ScheduleGridFragment", "User loaded: " + currentUser.getName() + ", isPlayer: " + currentUser.isPlayer() + ", teams: " + (currentUser.getTeamIds() != null ? currentUser.getTeamIds().size() : 0));
                            // Only ADMIN and COORDINATOR can edit
                            isReadOnlyMode = !canEditSchedule();
                            userLoaded = true; // Mark user as loaded
                            updateUIForPermissions();
                            // Refresh grid to apply proper filtering based on user role
                            refreshScheduleGrid();
                        } else {
                            isReadOnlyMode = true;
                            userLoaded = true; // Mark as loaded even if null
                        }
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) {
                        android.util.Log.e("ScheduleGrid", "Failed to load user: " + error.getMessage());
                        isReadOnlyMode = true;
                    }
                });
    }

    /**
     * Check if current user can edit schedule
     */
    private boolean canEditSchedule() {
        return currentUser != null && (currentUser.isAdmin() || currentUser.isCoordinator());
    }

    /**
     * Update UI based on user permissions
     */
    private void updateUIForPermissions() {
        if (getView() == null) return;
        
        // Hide edit functionality for read-only users
        if (isReadOnlyMode) {
            // Remove click listeners for adding trainings
            adapter2D.setSlotClickListener(null);
            
            // Show read-only toast when clicking occupied slots
            adapter2D.setOccupiedSlotListener(timeSlot -> {
                if (timeSlot != null && timeSlot.getTraining() != null) {
                    Training training = timeSlot.getTraining();
                    String message = "אימון: " + training.getTeamName() + "\n" +
                                    "שעה: " + training.getStartTime() + " - " + training.getEndTime() + "\n\n" +
                                    "צפייה בלבד - אין הרשאה לעריכה";
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Setup ViewModels and observe LiveData.
     */
    private void setupViewModels() {
        try {
            courtViewModel = new ViewModelProvider(this).get(CourtViewModel.class);
            teamViewModel = new ViewModelProvider(this).get(TeamViewModel.class);
            trainingViewModel = new ViewModelProvider(this).get(TrainingViewModel.class);

            // Observe courts
            courtViewModel.getCourts().observe(getViewLifecycleOwner(), courtList -> {
                try {
                    if (courtList != null) {
                        courts = courtList;
                        renderCourtChips();
                    }
                } catch (Exception e) {
                    android.util.Log.e("ScheduleGrid", "Error in courts observer", e);
                }
            });

            // Observe teams
            teamViewModel.getTeams().observe(getViewLifecycleOwner(), teamList -> {
                try {
                    if (teamList != null) {
                        teams = teamList;
                        android.util.Log.d("ScheduleGrid", "Teams loaded: " + teams.size());
                    }
                } catch (Exception e) {
                    android.util.Log.e("ScheduleGrid", "Error in teams observer", e);
                }
            });

            // Observe trainings
            trainingViewModel.getTrainings().observe(getViewLifecycleOwner(), trainingList -> {
                try {
                    if (trainingList != null) {
                        trainings = trainingList;
                        android.util.Log.d("ScheduleGridFragment", "Trainings loaded: " + trainings.size() + " total");
                        for (Training t : trainings) {
                            android.util.Log.d("ScheduleGridFragment", "  - " + t.getTeamName() + " at " + t.getStartTime() + " on court " + t.getCourtId());
                        }
                        // Only refresh grid if user was already loaded to prevent showing unfiltered trainings
                        if (userLoaded) {
                            refreshScheduleGrid();
                        } else {
                            android.util.Log.d("ScheduleGridFragment", "User not loaded yet, skipping refresh");
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.e("ScheduleGrid", "Error in trainings observer", e);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("ScheduleGridFragment", "Error in setupViewModels", e);
            e.printStackTrace();
        }
    }

    /**
     * Setup UI click listeners.
     */
    private void setupUI() {
        btnPreviousWeek.setOnClickListener(v -> {
            if (isDayView) {
                shiftDay(-1);
            } else {
                weekStartDate.add(Calendar.WEEK_OF_YEAR, -1);
                updateWeekDisplay();
                refreshScheduleGrid();
            }
        });

        btnNextWeek.setOnClickListener(v -> {
            if (isDayView) {
                shiftDay(1);
            } else {
                weekStartDate.add(Calendar.WEEK_OF_YEAR, 1);
                updateWeekDisplay();
                refreshScheduleGrid();
            }
        });

        btnToggleDayView.setOnClickListener(v -> {
            isDayView = !isDayView;
            btnToggleDayView.setText(isDayView ? "תצוגת שבוע" : "תצוגת יום");
            
            // Update navigation button labels based on view mode
            if (isDayView) {
                btnPreviousWeek.setText("← יום אחורה");
                btnNextWeek.setText("יום קדימה →");
                
                // Default to today if within current week, otherwise Sunday
                Calendar today = Calendar.getInstance();
                Calendar weekEnd = (Calendar) weekStartDate.clone();
                weekEnd.add(Calendar.DAY_OF_YEAR, 6);
                if (!today.before(weekStartDate) && !today.after(weekEnd)) {
                    selectedDayIndex = today.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
                } else {
                    selectedDayIndex = 0;
                }
            } else {
                btnPreviousWeek.setText("← שבוע קודם");
                btnNextWeek.setText("שבוע הבא →");
            }
            
            updateSelectedDayLabel();
            refreshScheduleGrid();
        });
    }

    /**
     * Shift the viewed day by delta (+1 next, -1 previous). If target day exits current week, move weekStartDate accordingly.
     */
    private void shiftDay(int deltaDays) {
        Calendar target = (Calendar) weekStartDate.clone();
        target.add(Calendar.DAY_OF_YEAR, selectedDayIndex + deltaDays);

        weekStartDate = getWeekStart(target);
        selectedDayIndex = target.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
        if (selectedDayIndex < 0) selectedDayIndex = 0;
        updateWeekDisplay();
        updateSelectedDayLabel();
        refreshScheduleGrid();
    }

    /**
     * Get the Sunday (week start) for a given date.
     */
    private Calendar getWeekStart(Calendar date) {
        Calendar weekStart = (Calendar) date.clone();
        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        weekStart.set(Calendar.HOUR_OF_DAY, 0);
        weekStart.set(Calendar.MINUTE, 0);
        weekStart.set(Calendar.SECOND, 0);
        weekStart.set(Calendar.MILLISECOND, 0);
        return weekStart;
    }

    /**
     * Update the week range display.
     */
    private void updateWeekDisplay() {
        Calendar weekEnd = (Calendar) weekStartDate.clone();
        weekEnd.add(Calendar.DAY_OF_YEAR, 6);

        String weekRange = DATE_FORMAT.format(weekStartDate.getTime()) +
                " - " + DATE_FORMAT.format(weekEnd.getTime());
        tvWeekDisplay.setText(weekRange);
    }

    private void updateSelectedDayLabel() {
        String[] days = {"ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי", "שבת"};
        if (tvSelectedDay != null && selectedDayIndex >= 0 && selectedDayIndex < days.length) {
            tvSelectedDay.setText("יום: " + days[selectedDayIndex]);
        }
        tvSelectedDay.setVisibility(isDayView ? View.VISIBLE : View.GONE);
    }

    /**
     * Refresh the schedule grid with current week and court data.
     */
    private void refreshScheduleGrid() {
        try {
            if (selectedCourt == null) {
                tvEmptyState.setVisibility(View.VISIBLE);
                scheduleTable.setVisibility(View.GONE);
            }

            tvEmptyState.setVisibility(View.GONE);
            scheduleTable.setVisibility(View.VISIBLE);

            // Guard against null selectedCourt
            if (selectedCourt == null) {
                android.util.Log.w("ScheduleGrid", "refreshScheduleGrid called but selectedCourt is null");
                return;
            }

            // Filter trainings for player: show only their teams
            List<Training> visibleTrainings = new ArrayList<>(trainings);
            if (currentUser != null && currentUser.isPlayer()) {
                List<String> allowedTeams = currentUser.getTeamIds();
                visibleTrainings.removeIf(t -> t.getTeamId() == null || !allowedTeams.contains(t.getTeamId()));
            }

            android.util.Log.d("ScheduleGrid", "refreshScheduleGrid started");
            android.util.Log.d("ScheduleGrid", "Selected court: " + selectedCourt.getCourtId() + ", Trainings count: " + visibleTrainings.size());
            
            // Log all trainings for debugging
            for (Training t : visibleTrainings) {
                if (t.getCourtId() != null && t.getCourtId().equals(selectedCourt.getCourtId())) {
                    android.util.Log.d("ScheduleGrid", "Court training: " + t.getTeamName() + " at " + t.getStartTime() + "-" + t.getEndTime() + " on " + new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date(t.getDate())));
                }
            }

            // Pre-filter trainings for selected court and bucket them per day to reduce repeated scans
            Map<Integer, List<Training>> trainingsByDayIndex = new HashMap<>();
            for (Training t : visibleTrainings) {
                if (t.getCourtId() == null || !t.getCourtId().equals(selectedCourt.getCourtId())) {
                    continue;
                }
                for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
                    Calendar day = (Calendar) weekStartDate.clone();
                    day.add(Calendar.DAY_OF_YEAR, dayIndex);
                    if (isSameDay(day.getTimeInMillis(), t.getDate())) {
                        trainingsByDayIndex.computeIfAbsent(dayIndex, k -> new ArrayList<>()).add(t);
                        break;
                    }
                }
            }

            // Calculate min/max operating hours across all visible days
            int globalMinStart = Integer.MAX_VALUE;
            int globalMaxEnd = Integer.MIN_VALUE;
            
            for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
                if (isDayView && dayIndex != selectedDayIndex) {
                    continue;
                }
                Calendar day = (Calendar) weekStartDate.clone();
                day.add(Calendar.DAY_OF_YEAR, dayIndex);
                int dayOfWeek = day.get(Calendar.DAY_OF_WEEK);
                DaySchedule daySchedule = selectedCourt.getScheduleForDay(dayOfWeek);
                
                if (daySchedule != null && daySchedule.isActive()) {
                    int dayStart = timeToMinutes(daySchedule.getOpeningHour());
                    int dayEnd = timeToMinutes(daySchedule.getClosingHour());
                    if (dayStart >= 0) globalMinStart = Math.min(globalMinStart, dayStart);
                    if (dayEnd >= 0) globalMaxEnd = Math.max(globalMaxEnd, dayEnd);
                }
            }
            
            // Fallback if no valid hours found
            if (globalMinStart == Integer.MAX_VALUE) globalMinStart = 480; // 08:00
            if (globalMaxEnd == Integer.MIN_VALUE) globalMaxEnd = 1320;   // 22:00

            // Generate all time slots for the week
            List<TimeSlot> allTimeSlots = new ArrayList<>();
            Map<String, Training> trainingBySlot = new HashMap<>();

            // Generate days (all week or single day)
            for (int dayIndex = 0; dayIndex < 7; dayIndex++) {
                if (isDayView && dayIndex != selectedDayIndex) {
                    continue;
                }
                Calendar day = (Calendar) weekStartDate.clone();
                day.add(Calendar.DAY_OF_YEAR, dayIndex);
                int dayOfWeek = day.get(Calendar.DAY_OF_WEEK);
                DaySchedule courtDaySchedule = selectedCourt.getScheduleForDay(dayOfWeek);
                List<Training> dayTrainings = trainingsByDayIndex.getOrDefault(dayIndex, new ArrayList<>());

                android.util.Log.d("ScheduleGridFragment", "Day " + dayIndex + ": dayOfWeek=" + dayOfWeek + ", daySchedule=" + (courtDaySchedule != null ? "exists" : "null") + ", trainings=" + dayTrainings.size());

                // Show slots if: court is active OR there are trainings to display
                if ((courtDaySchedule != null && courtDaySchedule.isActive()) || !dayTrainings.isEmpty()) {
                    android.util.Log.d("ScheduleGridFragment", "Generating slots for day " + dayIndex);
                    List<TimeSlot> daySlotsraw = generateTimeSlotsForDay(selectedCourt, courtDaySchedule, day, dayTrainings, globalMinStart, globalMaxEnd);
                    android.util.Log.d("ScheduleGridFragment", "Generated " + daySlotsraw.size() + " slots for day " + dayIndex);
                    allTimeSlots.addAll(daySlotsraw);

                    // Map trainings to their overlapping 30-minute slots without scanning all trainings repeatedly
                    for (Training training : dayTrainings) {
                        int trainingStartMin = timeToMinutes(training.getStartTime());
                        int trainingEndMin = timeToMinutes(training.getEndTime());
                        if (trainingStartMin < 0 || trainingEndMin < 0) continue;

                        int startRounded = (trainingStartMin / TIME_SLOT_DURATION) * TIME_SLOT_DURATION;
                        int endRounded = ((trainingEndMin + TIME_SLOT_DURATION - 1) / TIME_SLOT_DURATION) * TIME_SLOT_DURATION;
                        for (int m = startRounded; m < endRounded; m += TIME_SLOT_DURATION) {
                            String slotKey = minutesToTime(m) + "_" + dayIndex;
                            trainingBySlot.put(slotKey, training);
                        }
                    }
                }
            }

            android.util.Log.d("ScheduleGrid", "Total time slots: " + allTimeSlots.size() + ", Populated slots: " + trainingBySlot.size());

            // Recreate adapter with new week start date for proper day column headers
            adapter2D = new Schedule2DAdapter(getContext(), scheduleTable, (Calendar) weekStartDate.clone());

            // Re-attach occupied slot listener (new adapter instance loses previous listener)
            if (isReadOnlyMode) {
                adapter2D.setOccupiedSlotListener(timeSlot -> {
                    if (timeSlot != null && timeSlot.getTraining() != null) {
                        Training training = timeSlot.getTraining();
                        String message = "אימון: " + training.getTeamName() + "\n" + training.getStartTime() + "-" + training.getEndTime();
                        Toast.makeText(getContext(), message + "\n(צפייה בלבד)", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                adapter2D.setOccupiedSlotListener(timeSlot -> {
                    if (timeSlot != null && timeSlot.getTraining() != null) {
                        Training completeTraining = findTrainingInList(timeSlot.getTraining());
                        if (completeTraining != null) {
                            showEditTrainingDialog(completeTraining, timeSlot.getDate());
                        } else {
                            showEditTrainingDialog(timeSlot.getTraining(), timeSlot.getDate());
                        }
                    }
                });
            }

            // Set visible day indices depending on view mode
            List<Integer> visibleDayIndices = new ArrayList<>();
            if (isDayView) {
                visibleDayIndices.add(selectedDayIndex);
            } else {
                for (int i = 0; i < 7; i++) {
                    visibleDayIndices.add(i);
                }
            }
            adapter2D.setVisibleDayIndices(visibleDayIndices);
            
            // Set court info for TimeSlot creation
            if (selectedCourt != null) {
                adapter2D.setCourtInfo(selectedCourt.getCourtId(), selectedCourt.getName());
            }
            
            // Set click listeners based on permissions
            if (!isReadOnlyMode) {
                adapter2D.setSlotClickListener(timeSlot -> showTeamSelectionDialog(timeSlot));
            } else {
                adapter2D.setSlotClickListener(null);
            }
            // Note: occupiedSlotListener is set once in onViewCreated/loadCurrentUser - don't override it here

            android.util.Log.d("ScheduleGrid", "Before updateData: allTimeSlots=" + (allTimeSlots != null ? allTimeSlots.size() : "null") + ", trainingBySlot=" + (trainingBySlot != null ? trainingBySlot.size() : "null"));
            adapter2D.updateData(allTimeSlots, trainingBySlot);
            android.util.Log.d("ScheduleGrid", "After updateData");
            updateWeekDisplay();
        } catch (Exception e) {
            android.util.Log.e("ScheduleGridFragment", "Error in refreshScheduleGrid: " + e.getMessage(), e);
            e.printStackTrace();
            Toast.makeText(getContext(), "שגיאה בטעינת לוח אימונים: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Generate 30-minute time slots for one day.
     */
    private List<TimeSlot> generateTimeSlotsForDay(Court court, DaySchedule daySchedule, Calendar day, List<Training> dayTrainings, int globalMinStart, int globalMaxEnd) {
        List<TimeSlot> timeSlots = new ArrayList<>();

        boolean dayActive = daySchedule != null && daySchedule.isActive();

        // If the court is closed but there are trainings, still show slots for them
        if (!dayActive && dayTrainings.isEmpty()) {
            return timeSlots; // Nothing to show
        }

        // Use global min/max hours across all visible days
        int scheduleStart = globalMinStart;
        int scheduleEnd = globalMaxEnd;

        // Generate 30-minute slots across the computed window (including the end time)
        for (int minutes = scheduleStart; minutes <= scheduleEnd - TIME_SLOT_DURATION; minutes += TIME_SLOT_DURATION) {
            String startTime = minutesToTime(minutes);
            String endTime = minutesToTime(minutes + TIME_SLOT_DURATION);

            TimeSlot slot = new TimeSlot(court.getCourtId(), court.getName(),
                    startTime, endTime, day.getTimeInMillis());

            timeSlots.add(slot);
        }

        // Mark occupied slots based on trainings (covers trainings outside original schedule window)
        markOccupiedSlots(timeSlots, court.getCourtId(), day.getTimeInMillis(), dayTrainings);

        return timeSlots;
    }

    /**
     * Mark which time slots are occupied by trainings.
     */
    private void markOccupiedSlots(List<TimeSlot> timeSlots, String courtId, long targetDate, List<Training> dayTrainings) {
        Calendar targetCal = Calendar.getInstance();
        targetCal.setTimeInMillis(targetDate);
        targetCal.set(Calendar.HOUR_OF_DAY, 0);
        targetCal.set(Calendar.MINUTE, 0);
        targetCal.set(Calendar.SECOND, 0);
        targetCal.set(Calendar.MILLISECOND, 0);
        long startOfDay = targetCal.getTimeInMillis();

        targetCal.set(Calendar.HOUR_OF_DAY, 23);
        targetCal.set(Calendar.MINUTE, 59);
        targetCal.set(Calendar.SECOND, 59);
        targetCal.set(Calendar.MILLISECOND, 999);
        long endOfDay = targetCal.getTimeInMillis();

        // Find trainings for this court/day
        for (Training training : dayTrainings) {
            if (training.getCourtId() == null || !training.getCourtId().equals(courtId)) {
                continue;
            }

            int trainingStart = timeToMinutes(training.getStartTime());
            int trainingEnd = timeToMinutes(training.getEndTime());

            // Mark all slots that overlap with this training
            for (TimeSlot slot : timeSlots) {
                int slotStart = timeToMinutes(slot.getStartTime());
                int slotEnd = timeToMinutes(slot.getEndTime());

                if (slotStart < trainingEnd && slotEnd > trainingStart) {
                    slot.setTraining(training);
                }
            }
        }
    }

    /**
     * Get team name by ID.
     */
    private String getTeamNameById(String teamId) {
        for (Team team : teams) {
            if (team.getTeamId() != null && team.getTeamId().equals(teamId)) {
                return team.getName();
            }
        }
        return "Unknown Team";
    }

    /**
     * Render court selection chips instead of dialog/button.
     */
    private void renderCourtChips() {
        if (chipGroupCourts == null || getContext() == null) return;

        chipGroupCourts.removeAllViews();

        for (Court court : courts) {
            Chip chip = new Chip(getContext());
            chip.setText(court.getName());
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setEnsureMinTouchTargetSize(true);
            chip.setTag(court);

            if (selectedCourt != null && selectedCourt.getCourtId().equals(court.getCourtId())) {
                chip.setChecked(true);
            }

            chipGroupCourts.addView(chip);
        }

        chipGroupCourts.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds == null || checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            View chipView = group.findViewById(checkedId);
            if (chipView instanceof Chip) {
                Court court = (Court) chipView.getTag();
                if (court != null) {
                    selectedCourt = court;
                    if (tvSelectedCourt != null) {
                        tvSelectedCourt.setText(court.getName());
                    }
                    refreshScheduleGrid();
                }
            }
        });

        // Ensure a selection exists
        if (selectedCourt == null && chipGroupCourts.getChildCount() > 0) {
            Chip firstChip = (Chip) chipGroupCourts.getChildAt(0);
            firstChip.setChecked(true);
        } else if (selectedCourt != null) {
            for (int i = 0; i < chipGroupCourts.getChildCount(); i++) {
                View child = chipGroupCourts.getChildAt(i);
                if (child instanceof Chip) {
                    Court court = (Court) child.getTag();
                    if (court != null && court.getCourtId().equals(selectedCourt.getCourtId())) {
                        ((Chip) child).setChecked(true);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Show court selection dialog.
     */
    private void showCourtSelectionDialog() {
        if (courts.isEmpty()) {
            Toast.makeText(getContext(), "No courts available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] courtNames = new String[courts.size()];
        for (int i = 0; i < courts.size(); i++) {
            courtNames[i] = courts.get(i).getName();
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Court")
                .setItems(courtNames, (dialog, which) -> {
                    selectedCourt = courts.get(which);
                    tvSelectedCourt.setText(selectedCourt.getName());
                    refreshScheduleGrid();
                })
                .show();
    }

    /**
     * Show team selection dialog with custom time selection.
     */
    private void showTeamSelectionDialog(TimeSlot timeSlot) {
        android.util.Log.d("ScheduleGrid", "showTeamSelectionDialog called");
        
        if (getContext() == null) {
            android.util.Log.e("ScheduleGrid", "Context is null!");
            return;
        }
        
        if (timeSlot == null) {
            android.util.Log.e("ScheduleGrid", "TimeSlot is null!");
            Toast.makeText(getContext(), "שגיאה: משבצת זמן לא תקינה", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if the date is in the past
        long slotDate = timeSlot.getDate();
        android.util.Log.d("ScheduleGrid", "TimeSlot date: " + slotDate + ", current time: " + System.currentTimeMillis());
        
        if (isPastDate(slotDate)) {
            Toast.makeText(getContext(), "❌ לא ניתן להוסיף אימון בתאריך שעבר", Toast.LENGTH_SHORT).show();
            android.util.Log.w("ScheduleGrid", "Attempt to schedule training in past: " + slotDate);
            return;
        }
        
        // Check if the time slot is within court operating hours
        if (selectedCourt != null && timeSlot.getStartTime() != null) {
            Calendar slotCal = Calendar.getInstance();
            slotCal.setTimeInMillis(slotDate);
            int dayOfWeek = slotCal.get(Calendar.DAY_OF_WEEK);
            
            int slotStartMinutes = timeToMinutesSafe(timeSlot.getStartTime());
            // If endTime is null, calculate it as startTime + 30 minutes (slot duration)
            int slotEndMinutes;
            if (timeSlot.getEndTime() != null) {
                slotEndMinutes = timeToMinutesSafe(timeSlot.getEndTime());
            } else {
                slotEndMinutes = slotStartMinutes + TIME_SLOT_DURATION; // 30 minutes
            }
            
            android.util.Log.d("ScheduleGrid", "Checking court hours - dayOfWeek: " + dayOfWeek + 
                ", slotTime: " + timeSlot.getStartTime() + "-" + (timeSlot.getEndTime() != null ? timeSlot.getEndTime() : "calculated") + 
                ", minutes: " + slotStartMinutes + "-" + slotEndMinutes);
            
            if (slotStartMinutes >= 0 && slotEndMinutes >= 0) {
                if (!isWithinCourtHours(selectedCourt, dayOfWeek, slotStartMinutes, slotEndMinutes)) {
                    // Error message is already shown by isWithinCourtHours method
                    android.util.Log.w("ScheduleGrid", "Time slot outside court operating hours - blocking dialog");
                    return;
                }
            }
        } else {
            android.util.Log.w("ScheduleGrid", "Skipping court hours check - selectedCourt: " + (selectedCourt != null) + 
                ", startTime: " + (timeSlot.getStartTime() != null));
        }
        
        android.util.Log.d("ScheduleGrid", "TimeSlot: " + timeSlot.getStartTime() + " - " + timeSlot.getEndTime());
        android.util.Log.d("ScheduleGrid", "Teams count: " + teams.size());
        
        if (teams.isEmpty()) {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("אין קבוצות")
                .setMessage("לא קיימות קבוצות במערכת. יש להוסיף קבוצות לפני תזמון אימונים.")
                .setPositiveButton("הבנתי", null)
                .show();
            return;
        }

        try {
            // Inflate custom dialog layout
            android.util.Log.d("ScheduleGrid", "Inflating dialog layout...");
            View dialogView = LayoutInflater.from(getContext())
                    .inflate(R.layout.dialog_schedule_training, null);
            android.util.Log.d("ScheduleGrid", "Dialog layout inflated successfully");

            android.util.Log.d("ScheduleGrid", "Finding dialog views...");
            TextView tvStartTime = dialogView.findViewById(R.id.tvStartTime);
            TextView tvEndTime = dialogView.findViewById(R.id.tvEndTime);
            MaterialButton btnSelectStartTime = dialogView.findViewById(R.id.btnSelectStartTime);
            MaterialButton btnSelectEndTime = dialogView.findViewById(R.id.btnSelectEndTime);
            RecyclerView recyclerTeams = dialogView.findViewById(R.id.recyclerTeams);
            android.util.Log.d("ScheduleGrid", "Dialog views found: start=" + (tvStartTime != null) + ", end=" + (tvEndTime != null) + 
                    ", btn1=" + (btnSelectStartTime != null) + ", btn2=" + (btnSelectEndTime != null) + ", recycler=" + (recyclerTeams != null));

            // Initialize times - start with clicked slot time
            final String[] selectedStartTime = {timeSlot.getStartTime()};
            final String[] selectedEndTime = {getDefaultEndTime(timeSlot.getStartTime())};

            android.util.Log.d("ScheduleGrid", "Setting initial times: " + selectedStartTime[0] + " -> " + selectedEndTime[0]);
            tvStartTime.setText(selectedStartTime[0]);
            tvEndTime.setText(selectedEndTime[0]);

        // Start time picker
        btnSelectStartTime.setOnClickListener(v -> {
            showCustomTimePicker(selectedStartTime[0], time -> {
                selectedStartTime[0] = time;
                tvStartTime.setText(time);
                // Auto-update end time
                selectedEndTime[0] = getDefaultEndTime(time);
                tvEndTime.setText(selectedEndTime[0]);
            });
        });

        // End time picker
        btnSelectEndTime.setOnClickListener(v -> {
            showCustomTimePicker(selectedEndTime[0], time -> {
                selectedEndTime[0] = time;
                tvEndTime.setText(time);
            });
        });

        // Setup teams RecyclerView
        android.util.Log.d("ScheduleGrid", "Setting up teams RecyclerView...");
        recyclerTeams.setLayoutManager(new LinearLayoutManager(getContext()));
        final Team[] selectedTeamHolder = new Team[1];
        
        // Create a copy of teams list for filtering
        List<Team> filteredTeams = new ArrayList<>(teams);
        TeamSelectionAdapter teamAdapter = new TeamSelectionAdapter(filteredTeams, team -> {
            android.util.Log.d("ScheduleGrid", "Team selected: " + team.getName());
            selectedTeamHolder[0] = team;
        });
        recyclerTeams.setAdapter(teamAdapter);
        
        // Setup search functionality
        EditText etSearchTeam = dialogView.findViewById(R.id.etSearchTeam);
        etSearchTeam.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().trim().toLowerCase();
                
                // Filter teams based on search text
                filteredTeams.clear();
                if (searchText.isEmpty()) {
                    filteredTeams.addAll(teams);
                } else {
                    for (Team team : teams) {
                        if (team.getName().toLowerCase().contains(searchText) ||
                            team.getAgeGroup().toLowerCase().contains(searchText)) {
                            filteredTeams.add(team);
                        }
                    }
                }
                teamAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
        
        android.util.Log.d("ScheduleGrid", "RecyclerView setup complete");

        // Show dialog
        android.util.Log.d("ScheduleGrid", "Creating and showing AlertDialog...");
        new AlertDialog.Builder(requireContext())
                .setTitle("תזמון אימון")
                .setView(dialogView)
                .setPositiveButton("שמור", (dialog, which) -> {
                    android.util.Log.d("ScheduleGrid", "Save button clicked");
                    // Validate team selection
                    if (selectedTeamHolder[0] == null) {
                        Toast.makeText(getContext(), "יש לבחור קבוצה", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Validate time range
                    if (!isValidTimeRange(selectedStartTime[0], selectedEndTime[0])) {
                        Toast.makeText(getContext(), "שעת סיום חייבת להיות אחרי שעת התחלה", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Schedule training with custom times
                    scheduleTrainingWithCustomTimes(timeSlot, selectedTeamHolder[0], selectedStartTime[0], selectedEndTime[0]);
                })
                .setNegativeButton("ביטול", null)
                .show();
            android.util.Log.d("ScheduleGrid", "AlertDialog shown");
        } catch (Exception e) {
            android.util.Log.e("ScheduleGrid", "Error showing dialog: " + e.getMessage(), e);
            e.printStackTrace();
            Toast.makeText(getContext(), "שגיאה בפתיחת דיאלוג: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show Android TimePickerDialog.
     */
    private void showCustomTimePicker(String currentTime, TimePickerCallback callback) {
        String[] timeParts = currentTime.split(":");
        int hour = Integer.parseInt(timeParts[0]);
        int minute = Integer.parseInt(timeParts[1]);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minuteOfDay) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfDay);
                    callback.onTimePicked(time);
                },
                hour,
                minute,
                true // 24-hour format
        );
        timePickerDialog.show();
    }

    /**
     * Get default end time (start + 90 minutes).
     */
    private String getDefaultEndTime(String startTime) {
        int startMinutes = timeToMinutes(startTime);
        int endMinutes = startMinutes + 90; // Default 90 minutes duration
        return minutesToTime(endMinutes);
    }

    /**
     * Validate that end time is after start time.
     */
    private boolean isValidTimeRange(String startTime, String endTime) {
        int startMinutes = timeToMinutes(startTime);
        int endMinutes = timeToMinutes(endTime);
        return endMinutes > startMinutes;
    }

    /**
     * Schedule training with custom start and end times.
     */
    private void scheduleTrainingWithCustomTimes(TimeSlot baseTimeSlot, Team selectedTeam,
                                                  String startTime, String endTime) {
        android.util.Log.d("ScheduleGrid", "scheduleTrainingWithCustomTimes called");
        
        // Validate inputs
        if (baseTimeSlot == null) {
            android.util.Log.e("ScheduleGrid", "baseTimeSlot is null");
            Toast.makeText(getContext(), "שגיאה: משבצת זמן לא תקינה", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if the date is in the past
        long trainingDate = baseTimeSlot.getDate();
        if (isPastDate(trainingDate)) {
            Toast.makeText(getContext(), "לא ניתן להוסיף אימון בתאריך שעבר", Toast.LENGTH_SHORT).show();
            android.util.Log.w("ScheduleGrid", "Attempt to add training in the past: " + trainingDate);
            return;
        }
        
        if (selectedTeam == null) {
            android.util.Log.e("ScheduleGrid", "selectedTeam is null");
            Toast.makeText(getContext(), "שגיאה: קבוצה לא נבחרה", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (trainingRepository == null) {
            android.util.Log.e("ScheduleGrid", "trainingRepository is null");
            Toast.makeText(getContext(), "שגיאה: Repository לא אותחל", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId == null) {
            android.util.Log.e("ScheduleGrid", "userId is null");
            Toast.makeText(getContext(), "שגיאה: משתמש לא מאומת", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Validate that the training time is within court operating hours
            if (selectedCourt != null) {
                Calendar trainingCal = Calendar.getInstance();
                trainingCal.setTimeInMillis(baseTimeSlot.getDate());
                int dayOfWeek = trainingCal.get(Calendar.DAY_OF_WEEK);
                
                int startMinutes = timeToMinutes(startTime);
                int endMinutes = timeToMinutes(endTime);
                
                if (!isWithinCourtHours(selectedCourt, dayOfWeek, startMinutes, endMinutes)) {
                    // Error message is already shown by isWithinCourtHours method
                    return;
                }
            }
            
            Training training = new Training();
            training.setTeamId(selectedTeam.getTeamId());
            training.setTeamName(selectedTeam.getName());
            training.setTeamColor(selectedTeam.getColor()); // Add team color
            training.setCourtId(baseTimeSlot.getCourtId());
            training.setCourtName(baseTimeSlot.getCourtName());
            training.setDate(baseTimeSlot.getDate());
            training.setStartTime(startTime);
            training.setEndTime(endTime);
            training.setCreatedBy(userId);
            training.setCreatedAt(System.currentTimeMillis());

            android.util.Log.d("ScheduleGrid", "Calling trainingRepository.addTraining");

            trainingRepository.addTraining(training, new TrainingRepository.OnTrainingAddedListener() {
                @Override
                public void onTrainingAdded(String trainingId) {
                    android.util.Log.d("ScheduleGrid", "onTrainingAdded: " + trainingId);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "אימון נוסף בהצלחה", Toast.LENGTH_SHORT).show();
                            refreshScheduleGrid();
                        });
                    }
                }

                @Override
                public void onError(String error) {
                    android.util.Log.e("ScheduleGrid", "onError: " + error);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "שגיאה: " + error, Toast.LENGTH_LONG).show();
                        });
                    }
                }
            });
        } catch (Exception e) {
            android.util.Log.e("ScheduleGrid", "Exception in scheduleTrainingWithCustomTimes", e);
            Toast.makeText(getContext(), "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Find court by ID.
     */
    private Court findCourtById(String courtId) {
        for (Court court : courts) {
            if (court.getCourtId() != null && court.getCourtId().equals(courtId)) {
                return court;
            }
        }
        return null;
    }

    /**
     * Convert time string (HH:mm) to minutes since midnight.
     */
    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    /**
     * Convert minutes since midnight to time string (HH:mm).
     */
    private String minutesToTime(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", hours, mins);
    }

    /**
     * Show dialog to edit or delete existing training
     */
    private void showEditTrainingDialog(Training training, long slotDate) {
        new AlertDialog.Builder(requireContext())
                .setTitle("עריכת אימון")
                .setMessage("קבוצה: " + training.getTeamName() + "\n" +
                           "שעות: " + training.getStartTime() + " - " + training.getEndTime())
                .setPositiveButton("שכפל לשבועות קדימה", (dialog, which) -> {
                    duplicateToFutureWeeks(training, slotDate);
                })
                .setNeutralButton("מחק", (dialog, which) -> {
                    deleteTraining(training);
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    /**
     * Delete training from Firebase
     */
    private void deleteTraining(Training training) {
        if (trainingRepository == null) {
            Toast.makeText(getContext(), "שגיאה: Repository לא אותחל", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            android.util.Log.d("ScheduleGrid", "Deleting training: " + training.getTeamName() + " (ID: " + training.getTrainingId() + ")");
            trainingRepository.deleteTraining(training.getTrainingId());
            Toast.makeText(getContext(), "אימון נמחק בהצלחה", Toast.LENGTH_SHORT).show();
            
            // Wait a moment for Firebase to update, then refresh the UI
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                android.util.Log.d("ScheduleGrid", "Refreshing schedule after deletion");
                refreshScheduleGrid();
            }, 500);
        } catch (Exception e) {
            android.util.Log.e("ScheduleGrid", "Error deleting training", e);
            Toast.makeText(getContext(), "שגיאה במחיקת אימון: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Find a training in the trainings list by ID
     */
    private Training findTrainingInList(Training trainingToFind) {
        if (trainingToFind == null || trainingToFind.getTrainingId() == null) {
            return null;
        }
        
        for (Training training : trainings) {
            if (trainingToFind.getTrainingId().equals(training.getTrainingId())) {
                return training;
            }
        }
        
        return null;
    }

    /**
     * Check if a date is in the past (before today)
     */
    private boolean isPastDate(long dateTimestamp) {
        java.util.Calendar today = java.util.Calendar.getInstance();
        // Set to beginning of today
        today.set(java.util.Calendar.HOUR_OF_DAY, 0);
        today.set(java.util.Calendar.MINUTE, 0);
        today.set(java.util.Calendar.SECOND, 0);
        today.set(java.util.Calendar.MILLISECOND, 0);
        
        java.util.Calendar trainingDate = java.util.Calendar.getInstance();
        trainingDate.setTimeInMillis(dateTimestamp);
        // Set to beginning of training date
        trainingDate.set(java.util.Calendar.HOUR_OF_DAY, 0);
        trainingDate.set(java.util.Calendar.MINUTE, 0);
        trainingDate.set(java.util.Calendar.SECOND, 0);
        trainingDate.set(java.util.Calendar.MILLISECOND, 0);
        
        return trainingDate.before(today);
    }

    /**
     * Duplicate training to future weeks if available
     */
    private void duplicateToFutureWeeks(final Training training, final long slotDate) {
        // Show dialog to select number of weeks to duplicate
        final EditText input = new EditText(requireContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setHint("מספר שבועות");
        
        new AlertDialog.Builder(requireContext())
                .setTitle("שכפל לשבועות הבאות")
                .setMessage("כמה שבועות קדימה?")
                .setView(input)
                .setPositiveButton("בחר", (dialog, which) -> {
                    String numWeeksStr = input.getText().toString();
                    if (!numWeeksStr.isEmpty()) {
                        int numWeeks = Integer.parseInt(numWeeksStr);
                        if (numWeeks > 0) {
                            scheduleTrainingToFutureWeeks(training, numWeeks, slotDate);
                        }
                    }
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    /**
     * Schedule training to future weeks with availability checking
     */
    private void scheduleTrainingToFutureWeeks(Training training, int numWeeks, long slotDate) {
        if (trainingRepository == null) {
            Toast.makeText(getContext(), "שגיאה: Repository לא אותחל", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("ScheduleGrid", "Starting duplicate: " + training.getTeamName() + " for " + numWeeks + " weeks");
        
        Toast.makeText(getContext(), "מוסיף אימונים לשבועות קדימה...", Toast.LENGTH_SHORT).show();

        final java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        
        // Start from the slot's actual date, not from weekStartDate
        // This way if training is on Thursday, we duplicate from Thursday of each future week
        final java.util.Calendar trainingCalendar = java.util.Calendar.getInstance();
        trainingCalendar.setTimeInMillis(slotDate);
        
        android.util.Log.d("ScheduleGrid", "Training date: " + sdf.format(trainingCalendar.getTime()) + 
                          " (day of week: " + trainingCalendar.get(java.util.Calendar.DAY_OF_WEEK) + ")");
        
        final List<String> skippedReasons = new ArrayList<>();
        final int[] successCount = {0};
        final int[] currentWeek = {0};

        // Process each week sequentially - start from week 0 (will be incremented to 1 in first call)
        processWeekDuplicate(training, trainingCalendar, numWeeks, currentWeek, sdf, skippedReasons, successCount);
    }

    /**
     * Recursively process each week for duplication
     */
    private void processWeekDuplicate(final Training training, final java.util.Calendar calendar, final int totalWeeks, 
                                      final int[] currentWeek, final java.text.SimpleDateFormat sdf, 
                                      final List<String> skippedReasons, final int[] successCount) {
        // Increment week counter at the start
        currentWeek[0]++;
        
        if (currentWeek[0] > totalWeeks) {
            // All weeks processed - show result
            showDuplicationResult(skippedReasons, successCount[0], totalWeeks);
            return;
        }

        calendar.add(java.util.Calendar.DATE, 7);
        String futureDate = sdf.format(calendar.getTime());
        long futureTimestamp = calendar.getTimeInMillis();
        
        // Get day of week (1 = Sunday, 2 = Monday, ..., 7 = Saturday)
        int dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK);
        
        android.util.Log.d("ScheduleGrid", "Processing week " + currentWeek[0] + ": " + futureDate + 
                          " (day of week: " + dayOfWeek + ")");
        
        // Check if the court is operating on this day
        Court court = selectedCourt;
        if (court == null) {
            skippedReasons.add(futureDate + " (מגרש לא נבחר)");
            android.util.Log.w("ScheduleGrid", "selectedCourt is null");
            processWeekDuplicate(training, calendar, totalWeeks, currentWeek, sdf, skippedReasons, successCount);
            return;
        }

        DaySchedule daySchedule = court.getScheduleForDay(dayOfWeek);
        
        android.util.Log.d("ScheduleGrid", "Week " + currentWeek[0] + " (" + futureDate + "): dayOfWeek=" + dayOfWeek + 
                          ", isActive=" + (daySchedule != null ? daySchedule.isActive() : "null"));
        
        if (daySchedule == null || !daySchedule.isActive()) {
            // Court not operating on this day
            skippedReasons.add(futureDate + " (סגור)");
            android.util.Log.d("ScheduleGrid", "Court closed on " + futureDate);
            processWeekDuplicate(training, calendar, totalWeeks, currentWeek, sdf, skippedReasons, successCount);
            return;
        }

        // Create new training for this week
        Training newTraining = new Training();
        newTraining.setTeamId(training.getTeamId());
        newTraining.setTeamName(training.getTeamName());
        newTraining.setTeamColor(training.getTeamColor());
        // Always use selectedCourt's ID and name to ensure it's not null
        newTraining.setCourtId(selectedCourt.getCourtId());
        newTraining.setCourtName(selectedCourt.getName());
        newTraining.setCourtType(training.getCourtType());
        newTraining.setStartTime(training.getStartTime());
        newTraining.setEndTime(training.getEndTime());
        newTraining.setDate(futureTimestamp);
        
        android.util.Log.d("ScheduleGrid", "Adding training for " + futureDate + ": " + newTraining.getTeamName() + 
                          " (" + newTraining.getStartTime() + "-" + newTraining.getEndTime() + ")" +
                          ", courtId=" + newTraining.getCourtId());
        
        // Add training with callback
        trainingRepository.addTraining(newTraining, new TrainingRepository.OnConflictCheckListener() {
            @Override
            public void onSuccess() {
                successCount[0]++;
                android.util.Log.d("ScheduleGrid", "Training added successfully for " + futureDate);
                // Continue to next week
                processWeekDuplicate(training, calendar, totalWeeks, currentWeek, sdf, skippedReasons, successCount);
            }

            @Override
            public void onConflict() {
                skippedReasons.add(futureDate + " (תפוס)");
                android.util.Log.d("ScheduleGrid", "Conflict detected for " + futureDate);
                // Continue to next week
                processWeekDuplicate(training, calendar, totalWeeks, currentWeek, sdf, skippedReasons, successCount);
            }

            @Override
            public void onFailure(String error) {
                skippedReasons.add(futureDate + " (שגיאה)");
                android.util.Log.e("ScheduleGrid", "Error adding training for " + futureDate + ": " + error);
                // Continue to next week
                processWeekDuplicate(training, calendar, totalWeeks, currentWeek, sdf, skippedReasons, successCount);
            }
        });
    }

    /**
     * Show the result of duplication
     */
    private void showDuplicationResult(List<String> skippedReasons, int successCount, int totalWeeks) {
        android.util.Log.d("ScheduleGrid", "Duplication complete: success=" + successCount + ", skipped=" + skippedReasons.size());
        
        if (skippedReasons.isEmpty()) {
            Toast.makeText(getContext(), "✅ אימונים נוספו בהצלחה לכל " + totalWeeks + " השבועות", Toast.LENGTH_SHORT).show();
        } else {
            StringBuilder message = new StringBuilder("⚠️ הוסף " + successCount + " אימונים\n\nשבועות שלא זוכרו:\n");
            for (String reason : skippedReasons) {
                message.append("• ").append(reason).append("\n");
            }
            
            new AlertDialog.Builder(requireContext())
                    .setTitle("תוצאות השכפול")
                    .setMessage(message.toString().trim())
                    .setPositiveButton("אישור", (dialog, which) -> {})
                    .show();
        }
        
        // Refresh the schedule grid
        refreshScheduleGrid();
    }

    /**
     * Callback interface for time picker.
     */
    private interface TimePickerCallback {
        void onTimePicked(String time);
    }

    /**
     * Check if two times are the same (HH:mm format).
     */
    private boolean isSameTime(String time1, String time2) {
        if (time1 == null || time2 == null) return false;
        return time1.equals(time2);
    }

    /**
     * Check if two dates are on the same day.
     */
    private boolean isSameDay(long timestamp1, long timestamp2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTimeInMillis(timestamp1);
        cal2.setTimeInMillis(timestamp2);
        
        boolean same = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        
        if (!same) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
            android.util.Log.d("ScheduleGrid", "Day mismatch: " + sdf.format(new java.util.Date(timestamp1)) + 
                " vs " + sdf.format(new java.util.Date(timestamp2)) + 
                " (DOY: " + cal1.get(Calendar.DAY_OF_YEAR) + " vs " + cal2.get(Calendar.DAY_OF_YEAR) + ")");
        }
        
        return same;
    }

    /**
     * Check if training time is within court operating hours.
     * Shows error message and returns false if outside operating hours.
     */
    private boolean isWithinCourtHours(Court court, int dayOfWeek, int startMinutes, int endMinutes) {
        DaySchedule schedule = court.getScheduleForDay(dayOfWeek);
        android.util.Log.d("ScheduleGrid", "isWithinCourtHours - dayOfWeek: " + dayOfWeek + 
            ", schedule: " + (schedule != null ? schedule.toString() : "null") + 
            ", isActive: " + (schedule != null ? schedule.isActive() : "N/A"));
        
        if (schedule == null || !schedule.isActive()) {
            Toast.makeText(getContext(), "המגרש סגור ביום זה", Toast.LENGTH_SHORT).show();
            return false;
        }

        int open = timeToMinutesSafe(schedule.getOpeningHour());
        int close = timeToMinutesSafe(schedule.getClosingHour());
        android.util.Log.d("ScheduleGrid", "Court hours - open: " + open + " (" + schedule.getOpeningHour() + 
            "), close: " + close + " (" + schedule.getClosingHour() + 
            "), slot: " + startMinutes + "-" + endMinutes);
        
        if (open < 0 || close < 0 || open >= close) {
            Toast.makeText(getContext(), "הגדרת שעות פעילות לא תקינה למגרש", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (startMinutes < open || endMinutes > close) {
            String msg = String.format(Locale.getDefault(), "שעות חורגות משעות פעילות (%s-%s)", schedule.getOpeningHour(), schedule.getClosingHour());
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            android.util.Log.w("ScheduleGrid", "Time outside hours: slot " + startMinutes + "-" + endMinutes + 
                " vs court " + open + "-" + close);
            return false;
        }
        android.util.Log.d("ScheduleGrid", "Time slot is within court hours - OK");
        return true;
    }

    /**
     * Convert time string (HH:mm) to minutes since midnight with error handling.
     * Returns -1 if the time format is invalid.
     */
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
