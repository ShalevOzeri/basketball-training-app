package com.example.testapp.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.testapp.R;
import com.example.testapp.models.Court;
import com.example.testapp.models.Team;
import com.example.testapp.models.Training;
import com.example.testapp.models.User;
import com.example.testapp.viewmodel.CourtViewModel;
import com.example.testapp.viewmodel.TrainingViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import android.content.SharedPreferences;

public class AllCourtsViewFragment extends Fragment {

    private ScrollView scrollView;
    private LinearLayout courtsContainer;
    private SearchView searchViewCourts;
    private SearchView searchViewTeams;
    private ChipGroup chipGroupCourts;
    private ChipGroup chipGroupDays;
    private ChipGroup chipGroupTeams;
    private LinearLayout filterHeader;
    private androidx.core.widget.NestedScrollView filtersScrollView;
    private ImageView expandCollapseIcon;
    private boolean isFiltersExpanded = false;
    private MaterialButton btnSelectAllCourts;
    private MaterialButton btnSelectAllDays;
    private MaterialButton btnSelectAllTeams;
    private CourtViewModel courtViewModel;
    private TrainingViewModel trainingViewModel;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "AllCourtsViewPreferences";
    private static final String KEY_SELECTED_COURTS = "selected_courts";
    private static final String KEY_SELECTED_DAYS = "selected_days";
    private static final String KEY_SELECTED_TEAMS = "selected_teams";

    private List<Court> courts = new ArrayList<>();
    private List<Training> trainings = new ArrayList<>();
    private List<Team> allTeams = new ArrayList<>();

    private Set<String> selectedCourtIds = new HashSet<>();
    private Set<String> selectedDays = new HashSet<>();
    private Set<String> selectedTeamIds = new HashSet<>();
    private boolean showOnlyThisWeek = true;
    
    private User currentUser;
    private boolean isPlayer = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_courts_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            // Initialize SharedPreferences
            sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
            
            initializeViews(view);
            loadSavedFilters(); // Load saved filters before loading user
            loadCurrentUser();
        } catch (Exception e) {
            android.util.Log.e("AllCourtsViewFragment", "Error in onViewCreated", e);
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "שגיאה בטעינת הדף: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh trainings when returning to this fragment to ensure deleted trainings are removed
        try {
            android.util.Log.d("AllCourtsViewFragment", "onResume: Refreshing trainings list");
            if (trainingViewModel != null && trainings != null) {
                // Clear current trainings and let the observer refresh them
                trainings.clear();
                updateUI();
            }
        } catch (Exception e) {
            android.util.Log.e("AllCourtsViewFragment", "Error in onResume", e);
        }
    }

    private void loadSavedFilters() {
        // Load saved filter selections from SharedPreferences
        String savedCourts = sharedPreferences.getString(KEY_SELECTED_COURTS, "");
        String savedDays = sharedPreferences.getString(KEY_SELECTED_DAYS, "");
        String savedTeams = sharedPreferences.getString(KEY_SELECTED_TEAMS, "");
        
        if (!savedCourts.isEmpty()) {
            selectedCourtIds = new HashSet<>(java.util.Arrays.asList(savedCourts.split(",")));
        }
        if (!savedDays.isEmpty()) {
            selectedDays = new HashSet<>(java.util.Arrays.asList(savedDays.split(",")));
        }
        if (!savedTeams.isEmpty()) {
            selectedTeamIds = new HashSet<>(java.util.Arrays.asList(savedTeams.split(",")));
        }
    }

    private void saveFilters() {
        // Save current filter selections to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SELECTED_COURTS, String.join(",", selectedCourtIds));
        editor.putString(KEY_SELECTED_DAYS, String.join(",", selectedDays));
        editor.putString(KEY_SELECTED_TEAMS, String.join(",", selectedTeamIds));
        editor.apply();
    }

    private void loadCurrentUser() {
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseDatabase.getInstance().getReference("users").child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                if (!isAdded() || getContext() == null) {
                                    return; // Fragment not attached, skip update
                                }
                                
                                currentUser = snapshot.getValue(User.class);
                                if (currentUser != null && "PLAYER".equals(currentUser.getRole())) {
                                    isPlayer = true;
                                    android.util.Log.d("AllCourtsView", "Loaded player. UserId: " + currentUser.getUserId() + ", Teams: " + currentUser.getTeamIds());
                                }
                                setupFilters();
                                setupViewModels();
                            } catch (Exception e) {
                                android.util.Log.e("AllCourtsView", "Error in onDataChange", e);
                                setupFilters();
                                setupViewModels();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            android.util.Log.e("AllCourtsView", "Error loading user", error.toException());
                            setupFilters();
                            setupViewModels();
                        }
                    });
            } else {
                setupFilters();
                setupViewModels();
            }
        } catch (Exception e) {
            android.util.Log.e("AllCourtsViewFragment", "Error in loadCurrentUser", e);
            e.printStackTrace();
            setupFilters();
            setupViewModels();
        }
    }

    private void initializeViews(View view) {
        try {
            scrollView = view.findViewById(R.id.scrollView);
            courtsContainer = view.findViewById(R.id.courtsContainer);
            searchViewCourts = view.findViewById(R.id.searchViewCourts);
            searchViewTeams = view.findViewById(R.id.searchViewTeams);
            chipGroupCourts = view.findViewById(R.id.chipGroupCourts);
            chipGroupDays = view.findViewById(R.id.chipGroupDays);
            chipGroupTeams = view.findViewById(R.id.chipGroupTeams);
            filterHeader = view.findViewById(R.id.filterHeader);
            filtersScrollView = view.findViewById(R.id.filtersScrollView);
            expandCollapseIcon = view.findViewById(R.id.expandCollapseIcon);
            btnSelectAllCourts = view.findViewById(R.id.btnSelectAllCourts);
            btnSelectAllDays = view.findViewById(R.id.btnSelectAllDays);
            btnSelectAllTeams = view.findViewById(R.id.btnSelectAllTeams);

            android.util.Log.d("AllCourtsViewFragment", "All views initialized");

            setupExpandCollapse();
            setupSelectAllButtons();
        } catch (Exception e) {
            android.util.Log.e("AllCourtsViewFragment", "Error initializing views", e);
            e.printStackTrace();
            if (getContext() != null) {
                Toast.makeText(getContext(), "שגיאה בטעינת תצוגות: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupExpandCollapse() {
        filterHeader.setOnClickListener(v -> toggleFilters());
    }

    private void toggleFilters() {
        isFiltersExpanded = !isFiltersExpanded;

        if (isFiltersExpanded) {
            // Expand filters
            filtersScrollView.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams params = filtersScrollView.getLayoutParams();
            params.height = (int) (getResources().getDisplayMetrics().heightPixels * 0.35);
            filtersScrollView.setLayoutParams(params);
            expandCollapseIcon.setRotation(180);
        } else {
            // Collapse filters
            filtersScrollView.setVisibility(View.GONE);
            expandCollapseIcon.setRotation(0);
        }
    }

    private void setupSelectAllButtons() {
        btnSelectAllCourts.setOnClickListener(v -> selectAllCourts());
        btnSelectAllDays.setOnClickListener(v -> selectAllDays());
        btnSelectAllTeams.setOnClickListener(v -> selectAllTeams());
    }

    private void selectAllCourts() {
        // Check whether all visible courts are selected
        boolean allChecked = true;
        for (int i = 0; i < chipGroupCourts.getChildCount(); i++) {
            View child = chipGroupCourts.getChildAt(i);
            if (child instanceof Chip && child.getVisibility() == View.VISIBLE) {
                if (!((Chip) child).isChecked()) {
                    allChecked = false;
                    break;
                }
            }
        }
        
        // If all visible are selected, clear them; otherwise select them all
        for (int i = 0; i < chipGroupCourts.getChildCount(); i++) {
            View child = chipGroupCourts.getChildAt(i);
            if (child instanceof Chip && child.getVisibility() == View.VISIBLE) {
                ((Chip) child).setChecked(!allChecked);
            }
        }
        saveFilters();
    }

    private void selectAllDays() {
        // Check whether all days are selected
        boolean allChecked = true;
        for (int i = 0; i < chipGroupDays.getChildCount(); i++) {
            View child = chipGroupDays.getChildAt(i);
            if (child instanceof Chip) {
                if (!((Chip) child).isChecked()) {
                    allChecked = false;
                    break;
                }
            }
        }
        
        // If all days are selected, clear them; otherwise select them all
        for (int i = 0; i < chipGroupDays.getChildCount(); i++) {
            View child = chipGroupDays.getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setChecked(!allChecked);
            }
        }
        saveFilters();
    }

    private void selectAllTeams() {
        // Check whether all visible teams are selected
        boolean allChecked = true;
        for (int i = 0; i < chipGroupTeams.getChildCount(); i++) {
            View child = chipGroupTeams.getChildAt(i);
            if (child instanceof Chip && child.getVisibility() == View.VISIBLE) {
                if (!((Chip) child).isChecked()) {
                    allChecked = false;
                    break;
                }
            }
        }
        
        // If all visible are selected, clear them; otherwise select them all
        for (int i = 0; i < chipGroupTeams.getChildCount(); i++) {
            View child = chipGroupTeams.getChildAt(i);
            if (child instanceof Chip && child.getVisibility() == View.VISIBLE) {
                ((Chip) child).setChecked(!allChecked);
            }
        }
        saveFilters();
    }

    private void setupFilters() {
        // Setup day filter chips
        setupDayChips();

        // Setup search view for courts
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

        // Setup search view for teams
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
    }

    private void setupDayChips() {
        chipGroupDays.removeAllViews();
        
        // All days in order - display all 7 days including Sunday (week starts Sunday, ends Saturday)
        String[] days = {"ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי", "שבת"};
        String[] dayValues = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        // Add all 7 days
        for (int i = 0; i < 7; i++) {
            final String dayValue = dayValues[i];
            
            Chip chip = new Chip(requireContext());
            chip.setText(days[i]);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            chip.setTag(dayValue);
            
            // If no saved filters, check all by default; otherwise honor saved selections
            boolean shouldBeChecked = selectedDays.isEmpty() || selectedDays.contains(dayValue);
            chip.setChecked(shouldBeChecked);
            if (shouldBeChecked && !selectedDays.contains(dayValue)) {
                selectedDays.add(dayValue);
            }
            
            chip.setOnCheckedChangeListener((buttonView, checked) -> {
                if (checked) {
                    selectedDays.add((String) buttonView.getTag());
                } else {
                    selectedDays.remove(buttonView.getTag());
                }
                saveFilters();
                updateUI();
            });

            chipGroupDays.addView(chip);
        }
    }

    private void setupViewModels() {
        try {
            courtViewModel = new ViewModelProvider(this).get(CourtViewModel.class);
            trainingViewModel = new ViewModelProvider(this).get(TrainingViewModel.class);

            courtViewModel.getCourts().observe(getViewLifecycleOwner(), courtsList -> {
                try {
                    courts = courtsList;
                    setupCourtChips();
                    updateUI();
                } catch (Exception e) {
                    android.util.Log.e("AllCourtsView", "Error in courts observer", e);
                }
            });

            // Use getTrainings() instead of getFilteredTrainings() to show all trainings including past ones
            trainingViewModel.getTrainings().observe(getViewLifecycleOwner(), trainingsList -> {
                try {
                    // Filter out deleted trainings (those without valid trainingId)
                    List<Training> validTrainings = new ArrayList<>();
                    if (trainingsList != null) {
                        for (Training training : trainingsList) {
                            // Only include trainings with valid ID and court info
                            if (training.getTrainingId() != null && !training.getTrainingId().isEmpty() &&
                                training.getCourtId() != null && !training.getCourtId().isEmpty()) {
                                validTrainings.add(training);
                            }
                        }
                    }
                    trainings = validTrainings;
                    updateUI();
                } catch (Exception e) {
                    android.util.Log.e("AllCourtsView", "Error in trainings observer", e);
                }
            });

            loadTeams();
        } catch (Exception e) {
            android.util.Log.e("AllCourtsViewFragment", "Error in setupViewModels", e);
            e.printStackTrace();
        }
    }

    private void setupCourtChips() {
        chipGroupCourts.removeAllViews();
        // Do not clear selectedCourtIds to preserve filter state

        for (Court court : courts) {
            addCourtChip(court);
        }
    }

    private void addCourtChip(Court court) {
        Chip chip = new Chip(requireContext());
        chip.setText(court.getName());
        chip.setCheckable(true);
        chip.setCheckedIconVisible(true);
        
        // Only pre-select if this court was stored in preferences
        boolean shouldBeChecked = selectedCourtIds.contains(court.getCourtId());
        chip.setChecked(shouldBeChecked);
        
        chip.setTag(court);

        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedCourtIds.add(court.getCourtId());
            } else {
                selectedCourtIds.remove(court.getCourtId());
            }
            saveFilters();
            updateUI();
        });

        chipGroupCourts.addView(chip);
    }

    private void filterCourtChips(String query) {
        String lowerQuery = query.toLowerCase();
        for (int i = 0; i < chipGroupCourts.getChildCount(); i++) {
            View child = chipGroupCourts.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                Court court = (Court) chip.getTag();
                if (court != null) {
                    boolean matches = court.getName().toLowerCase().contains(lowerQuery);
                    chip.setVisibility(matches ? View.VISIBLE : View.GONE);
                }
            }
        }
    }

    private void loadTeams() {
        FirebaseDatabase.getInstance().getReference("teams")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!isAdded() || getContext() == null) {
                        return; // Fragment not attached, skip update
                    }
                    
                    allTeams.clear();
                    chipGroupTeams.removeAllViews();
                    // Do not clear selectedTeamIds to preserve filter state

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Team team = snapshot.getValue(Team.class);
                        if (team != null) {
                            allTeams.add(team);
                            
                            // For players, include only their teams
                            if (isPlayer) {
                                if (currentUser.getTeamIds() != null && 
                                    currentUser.getTeamIds().contains(team.getTeamId())) {
                                    addTeamChip(team);
                                }
                            } else {
                                // For non-players, include every team
                                addTeamChip(team);
                            }
                        }
                    }
                    
                    updateUI();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(requireContext(), "שגיאה בטעינת קבוצות", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void addTeamChip(Team team) {
        Chip chip = new Chip(requireContext());
        chip.setText(team.getName());
        chip.setCheckable(true);
        chip.setCheckedIconVisible(true);
        
        // Only pre-select if this team was stored in preferences
        boolean shouldBeChecked = selectedTeamIds.contains(team.getTeamId());
        chip.setChecked(shouldBeChecked);
        
        chip.setTag(team);

        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedTeamIds.add(team.getTeamId());
            } else {
                selectedTeamIds.remove(team.getTeamId());
            }
            saveFilters();
            updateUI();
        });
        
        chipGroupTeams.addView(chip);
    }

    private void filterTeamChips(String query) {
        String lowerQuery = query.toLowerCase();
        for (int i = 0; i < chipGroupTeams.getChildCount(); i++) {
            View child = chipGroupTeams.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                Team team = (Team) chip.getTag();
                if (team != null) {
                    boolean matches = team.getName().toLowerCase().contains(lowerQuery);
                    chip.setVisibility(matches ? View.VISIBLE : View.GONE);
                }
            }
        }
    }

    private void updateUI() {
        courtsContainer.removeAllViews();
        
        if (courts == null || courts.isEmpty()) {
            TextView emptyView = new TextView(requireContext());
            emptyView.setText("אין מגרשים במערכת");
            emptyView.setTextColor(Color.GRAY);
            emptyView.setTextSize(16);
            emptyView.setPadding(32, 32, 32, 32);
            emptyView.setGravity(android.view.Gravity.CENTER);
            courtsContainer.addView(emptyView);
            return;
        }

        // Get current week bounds for filtering (only if showOnlyThisWeek is true)
        // Week starts on Sunday and ends on Saturday (6 days, not including next Sunday)
        Calendar now = Calendar.getInstance();
        Calendar weekStart = (Calendar) now.clone();
        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        weekStart.set(Calendar.HOUR_OF_DAY, 0);
        weekStart.set(Calendar.MINUTE, 0);
        weekStart.set(Calendar.SECOND, 0);
        
        Calendar weekEnd = (Calendar) weekStart.clone();
        weekEnd.add(Calendar.DATE, 6); // Week is Sunday-Saturday (6 days), not including next Sunday
        weekEnd.set(Calendar.HOUR_OF_DAY, 23);
        weekEnd.set(Calendar.MINUTE, 59);
        weekEnd.set(Calendar.SECOND, 59);
        
        long weekStartMillis = weekStart.getTimeInMillis();
        long weekEndMillis = weekEnd.getTimeInMillis();

        Set<String> courtsForSelectedTeams = null;
        if (!selectedTeamIds.isEmpty()) {
            courtsForSelectedTeams = new HashSet<>();
            for (Training training : trainings) {
                if (selectedTeamIds.contains(training.getTeamId())) {
                    courtsForSelectedTeams.add(training.getCourtId());
                }
            }
        }

        for (Court court : courts) {
            // Apply court filter (multi-select)
            if (!selectedCourtIds.isEmpty() && !selectedCourtIds.contains(court.getCourtId())) {
                continue;
            }

            if (courtsForSelectedTeams != null && !courtsForSelectedTeams.contains(court.getCourtId())) {
                continue;
            }

            View courtView = createCourtView(court, weekStartMillis, weekEndMillis);
            courtsContainer.addView(courtView);
        }
    }

    private View createCourtView(Court court, long weekStartMillis, long weekEndMillis) {
        LinearLayout courtLayout = new LinearLayout(requireContext());
        courtLayout.setOrientation(LinearLayout.VERTICAL);
        courtLayout.setPadding(16, 16, 16, 16);

        LinearLayout headerLayout = new LinearLayout(requireContext());
        headerLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView courtName = new TextView(requireContext());
        courtName.setText(court.getName());
        courtName.setTextSize(20);
        courtName.setTypeface(null, android.graphics.Typeface.BOLD);
        courtName.setTextColor(Color.BLACK);
        courtName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        headerLayout.addView(courtName);

        int trainingCount = 0;
        for (Training training : trainings) {
            if (training.getCourtId() != null && training.getCourtId().equals(court.getCourtId())) {
                // Count only trainings in the current week
                long trainingDate = training.getDate();
                if (trainingDate >= weekStartMillis && trainingDate < weekEndMillis) {
                    // Also apply day and team filters if set
                    if (!selectedDays.isEmpty()) {
                        // Compute weekday from the date instead of relying on training.getDayOfWeek()
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(training.getDate());
                        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                        String[] englishDays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                        String dayOfWeekName = englishDays[dayOfWeek - 1];
                        if (!selectedDays.contains(dayOfWeekName)) {
                            continue;
                        }
                    }
                    
                    // For players: Only count trainings from their own teams
                    if (isPlayer) {
                        if (currentUser.getTeamIds() == null || !currentUser.getTeamIds().contains(training.getTeamId())) {
                            continue;
                        }
                        // If teams are filtered, also apply the filter
                        if (!selectedTeamIds.isEmpty() && !selectedTeamIds.contains(training.getTeamId())) {
                            continue;
                        }
                    } else {
                        // For non-players: Apply team filter if set
                        if (!selectedTeamIds.isEmpty() && !selectedTeamIds.contains(training.getTeamId())) {
                            continue;
                        }
                    }
                    
                    trainingCount++;
                }
            }
        }

        TextView infoText = new TextView(requireContext());
        infoText.setText("אימונים: " + trainingCount);
        infoText.setTextSize(14);
        infoText.setTextColor(Color.GRAY);
        infoText.setPadding(8, 0, 0, 0);
        headerLayout.addView(infoText);

        headerLayout.setPadding(0, 0, 0, 16);
        courtLayout.addView(headerLayout);

        View timelineView = createTimelineView(court, weekStartMillis, weekEndMillis);
        courtLayout.addView(timelineView);

        View divider = new View(requireContext());
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2);
        dividerParams.setMargins(0, 24, 0, 24);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(Color.LTGRAY);
        courtLayout.addView(divider);

        return courtLayout;
    }

    private View createTimelineView(Court court, long weekStartMillis, long weekEndMillis) {
        LinearLayout timeline = new LinearLayout(requireContext());
        timeline.setOrientation(LinearLayout.VERTICAL);

        List<Training> courtTrainings = new ArrayList<>();
        for (Training training : trainings) {
            if (training.getCourtId() == null || !training.getCourtId().equals(court.getCourtId())) {
                continue;
            }

            // Apply week filter
            if (showOnlyThisWeek) {
                long trainingDate = training.getDate();
                if (trainingDate < weekStartMillis || trainingDate >= weekEndMillis) {
                    continue;
                }
            }

            // Apply day filter (multi-select)
            if (!selectedDays.isEmpty()) {
                // Compute weekday from the date instead of relying on training.getDayOfWeek()
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(training.getDate());
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                String[] englishDays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                String dayOfWeekName = englishDays[dayOfWeek - 1];
                if (!selectedDays.contains(dayOfWeekName)) {
                    continue;
                }
            }

            // For players: Only show trainings from their own teams
            // Regardless of what team filter they select, they can only see their own teams
            if (isPlayer) {
                if (currentUser.getTeamIds() == null || !currentUser.getTeamIds().contains(training.getTeamId())) {
                    android.util.Log.d("AllCourtsView", "FILTERED OUT - Player not in team: " + training.getTeamId());
                    continue;
                }
                // If teams are filtered, also apply the filter
                if (!selectedTeamIds.isEmpty()) {
                    if (!selectedTeamIds.contains(training.getTeamId())) {
                        android.util.Log.d("AllCourtsView", "FILTERED OUT by team chip - Training teamId: " + training.getTeamId() + " not in selected: " + selectedTeamIds);
                        continue;
                    }
                }
            } else {
                // For non-players: Apply team filter based on chip selection
                if (!selectedTeamIds.isEmpty()) {
                    if (!selectedTeamIds.contains(training.getTeamId())) {
                        android.util.Log.d("AllCourtsView", "FILTERED OUT by chip - Training teamId: " + training.getTeamId() + " not in selected: " + selectedTeamIds);
                        continue;
                    }
                }
            }

            courtTrainings.add(training);
        }

        // Sort trainings by date and time
        Collections.sort(courtTrainings, new Comparator<Training>() {
            @Override
            public int compare(Training t1, Training t2) {
                // First compare by date
                int dateCompare = Long.compare(t1.getDate(), t2.getDate());
                if (dateCompare != 0) {
                    return dateCompare;
                }
                // If same date, compare by start time
                return t1.getStartTime().compareTo(t2.getStartTime());
            }
        });

        if (courtTrainings.isEmpty()) {
            TextView emptyText = new TextView(requireContext());
            emptyText.setText("אין אימונים");
            emptyText.setTextColor(Color.GRAY);
            emptyText.setPadding(16, 16, 16, 16);
            timeline.addView(emptyText);
        } else {
            String lastDate = "";
            for (Training training : courtTrainings) {
                // Get the day of week for this training
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(training.getDate());
                // Compute weekday directly from the date instead of relying on training.getDayOfWeek()
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1=Sunday, 2=Monday, etc.
                String[] englishDays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                String dayOfWeekName = englishDays[dayOfWeek - 1]; // Convert 1-based to 0-based index
                
                String dateStr = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(cal.getTime());
                String currentDateWithDay = dayOfWeekName + " " + dateStr;
                
                // Add day separator if date changed
                if (!lastDate.equals(currentDateWithDay)) {
                    // Add day header
                    View dayHeader = createDayHeaderView(dayOfWeekName);
                    timeline.addView(dayHeader);
                    lastDate = currentDateWithDay;
                }
                
                View trainingBlock = createTrainingBlock(training);
                timeline.addView(trainingBlock);
            }
        }

        return timeline;
    }
    
    private View createDayHeaderView(String dayOfWeekName) {
        TextView dayHeader = new TextView(requireContext());
        
        // Get Hebrew day name
        String[] hebrewDays = {"ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי", "שבת"};
        String[] englishDays = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        
        int dayIndex = 0;
        for (int i = 0; i < englishDays.length; i++) {
            if (englishDays[i].equals(dayOfWeekName)) {
                dayIndex = i;
                break;
            }
        }
        
        dayHeader.setText(hebrewDays[dayIndex]);
        dayHeader.setTextSize(16);
        dayHeader.setTypeface(null, android.graphics.Typeface.BOLD);
        dayHeader.setTextColor(Color.parseColor("#1976D2"));
        dayHeader.setPadding(16, 16, 16, 8);
        
        // Right-to-left layout for Hebrew
        dayHeader.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        dayHeader.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        
        // Add top divider
        LinearLayout headerLayout = new LinearLayout(requireContext());
        headerLayout.setOrientation(LinearLayout.VERTICAL);
        
        View topDivider = new View(requireContext());
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1);
        dividerParams.setMargins(0, 8, 0, 0);
        topDivider.setLayoutParams(dividerParams);
        topDivider.setBackgroundColor(Color.parseColor("#D0D0D0"));
        headerLayout.addView(topDivider);
        
        headerLayout.addView(dayHeader);
        
        return headerLayout;
    }

    private View createTrainingBlock(Training training) {
        LinearLayout blockLayout = new LinearLayout(requireContext());
        blockLayout.setOrientation(LinearLayout.VERTICAL);
        blockLayout.setPadding(12, 12, 12, 12);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 8);
        blockLayout.setLayoutParams(params);

        // Day and date
        TextView dayDateView = new TextView(requireContext());
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE, dd/MM", new Locale("he", "IL"));
        String dayDateText = dayFormat.format(training.getDate());
        dayDateView.setText(dayDateText);
        dayDateView.setTextColor(Color.WHITE);
        dayDateView.setTextSize(11);
        dayDateView.setTypeface(null, android.graphics.Typeface.ITALIC);
        blockLayout.addView(dayDateView);

        // Team name
        TextView teamNameView = new TextView(requireContext());
        teamNameView.setText(training.getTeamName());
        teamNameView.setTextColor(Color.WHITE);
        teamNameView.setTextSize(15);
        teamNameView.setTypeface(null, android.graphics.Typeface.BOLD);
        blockLayout.addView(teamNameView);

        // Time
        TextView timeView = new TextView(requireContext());
        timeView.setText(training.getStartTime() + " - " + training.getEndTime());
        timeView.setTextColor(Color.WHITE);
        timeView.setTextSize(13);
        blockLayout.addView(timeView);

        // Try to use team color from training, otherwise search for team in allTeams list
        String teamColor = training.getTeamColor();
        if (teamColor == null || teamColor.isEmpty()) {
            // Look for the team in our teams list and get its color
            for (Team team : allTeams) {
                if (team.getTeamId().equals(training.getTeamId())) {
                    teamColor = team.getColor();
                    break;
                }
            }
        }

        // Apply color with fallback
        try {
            if (teamColor != null && !teamColor.isEmpty()) {
                blockLayout.setBackgroundColor(Color.parseColor(teamColor));
            } else {
                blockLayout.setBackgroundColor(Color.parseColor("#3DDC84")); // Default green
            }
        } catch (Exception e) {
            blockLayout.setBackgroundColor(Color.parseColor("#3DDC84")); // Default green
        }

        return blockLayout;
    }
}
