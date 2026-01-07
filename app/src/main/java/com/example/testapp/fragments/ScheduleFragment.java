package com.example.testapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.AddTrainingActivity;
import com.example.testapp.EditTrainingActivity;
import com.example.testapp.R;
import com.example.testapp.adapters.TrainingAdapter;
import com.example.testapp.models.Court;
import com.example.testapp.models.Team;
import com.example.testapp.models.Training;
import com.example.testapp.models.User;
import com.example.testapp.repository.TrainingRepository;
import com.example.testapp.repository.UserRepository;
import com.example.testapp.viewmodel.CourtViewModel;
import com.example.testapp.viewmodel.TrainingViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import android.content.SharedPreferences;

public class ScheduleFragment extends Fragment {

    private RecyclerView recyclerView;
    private TrainingAdapter adapter;
    private TrainingViewModel viewModel;
    private CourtViewModel courtViewModel;
    private FloatingActionButton fab;
    private Chip chipThisWeek;
    private Chip chipHidePast;
    private SearchView searchViewTeams;
    private SearchView searchViewCourts;
    private ChipGroup chipGroupTeams;
    private ChipGroup chipGroupCourts;
    private ChipGroup chipGroupDays;
    private ChipGroup chipGroupMonths;
    private ChipGroup chipGroupSort;
    private LinearLayout filterHeader;
    private androidx.core.widget.NestedScrollView filtersScrollView;
    private ImageView expandCollapseIcon;
    private boolean isFiltersExpanded = false;
    private MaterialButton btnSelectAllTeams;
    private MaterialButton btnSelectAllCourts;
    private MaterialButton btnSelectAllMonths;
    private MaterialButton btnSelectAllDays;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "SchedulePreferences";
    private static final String KEY_SORT_TYPE = "sort_type";
    private static final String KEY_SELECTED_TEAMS = "selected_teams";
    private static final String KEY_SELECTED_COURTS = "selected_courts";
    private static final String KEY_SELECTED_DAYS = "selected_days";
    private static final String KEY_SELECTED_MONTHS = "selected_months";

    private List<Team> teamsList = new ArrayList<>();
    private List<Court> courtsList = new ArrayList<>();

    private Set<String> selectedTeamIds = new HashSet<>();
    private Set<String> selectedCourtIds = new HashSet<>();
    private Set<String> selectedDays = new HashSet<>();
    private Set<Integer> selectedMonths = new HashSet<>();
    
    // Sort type enum
    private enum SortType {
        BY_TEAM, BY_TIME
    }
    private SortType currentSortType = SortType.BY_TIME;

    private UserRepository userRepository;
    private User currentUser;
    private boolean isPlayer = false;
    private boolean userLoaded = false;
    private boolean viewCreated = false; // Track if view was created in this lifecycle

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewCreated = true; // Mark that view was created
        // Reset userLoaded on each view creation to force proper filtering
        userLoaded = false;

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        loadSavedFilters();
        
        initializeViews(view);
        setupRecyclerView();
        setupFab();
        checkUserPermissions(); // Load user first
        setupViewModel(); // Setup ViewModel - will wait for userLoaded before showing data
    }

    @Override
    public void onResume() {
        super.onResume();
        // Only reload if returning to fragment (not after initial creation)
        if (!viewCreated) {
            android.util.Log.d("ScheduleFragment", "onResume - returning to fragment, reloading");
            // Clear adapter and reset userLoaded when resuming to prevent showing cached unfiltered data
            if (adapter != null) {
                adapter.setTrainings(new ArrayList<>());
            }
            userLoaded = false;
            // Reload user permissions to ensure proper filtering
            checkUserPermissions();
        } else {
            android.util.Log.d("ScheduleFragment", "onResume - first time after onViewCreated, skipping reload");
            viewCreated = false; // Reset for next time
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Clear adapter when leaving to prevent stale data
        if (adapter != null) {
            adapter.setTrainings(new ArrayList<>());
        }
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.scheduleRecyclerView);
        fab = view.findViewById(R.id.fab);
        chipThisWeek = view.findViewById(R.id.chipThisWeek);
        chipHidePast = view.findViewById(R.id.chipHidePast);
        searchViewTeams = view.findViewById(R.id.searchViewTeams);
        searchViewCourts = view.findViewById(R.id.searchViewCourts);
        chipGroupTeams = view.findViewById(R.id.chipGroupTeams);
        chipGroupCourts = view.findViewById(R.id.chipGroupCourts);
        chipGroupDays = view.findViewById(R.id.chipGroupDays);
        chipGroupMonths = view.findViewById(R.id.chipGroupMonths);
        chipGroupSort = view.findViewById(R.id.chipGroupSort);
        filterHeader = view.findViewById(R.id.filterHeader);
        filtersScrollView = view.findViewById(R.id.filtersScrollView);
        expandCollapseIcon = view.findViewById(R.id.expandCollapseIcon);
        btnSelectAllTeams = view.findViewById(R.id.btnSelectAllTeams);
        btnSelectAllCourts = view.findViewById(R.id.btnSelectAllCourts);
        btnSelectAllMonths = view.findViewById(R.id.btnSelectAllMonths);
        btnSelectAllDays = view.findViewById(R.id.btnSelectAllDays);

        setupExpandCollapse();
        setupSelectAllButtons();
        setupSortOptions();
    }

    private void setupSortOptions() {
        // Load saved sort preference
        loadSortPreference();
        
        chipGroupSort.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipSortByTeam) {
                currentSortType = SortType.BY_TEAM;
            } else if (checkedId == R.id.chipSortByTime) {
                currentSortType = SortType.BY_TIME;
            }
            saveSortPreference();
            applyFiltersAndSort();
        });
    }
    
    private void loadSortPreference() {
        String savedSort = sharedPreferences.getString(KEY_SORT_TYPE, "BY_TIME");
        try {
            currentSortType = SortType.valueOf(savedSort);
        } catch (IllegalArgumentException e) {
            currentSortType = SortType.BY_TIME; // Default fallback
        }
        
        // Update UI to reflect loaded preference
        switch (currentSortType) {
            case BY_TEAM:
                chipGroupSort.check(R.id.chipSortByTeam);
                break;
            case BY_TIME:
                chipGroupSort.check(R.id.chipSortByTime);
                break;
        }
    }
    
    private void saveSortPreference() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SORT_TYPE, currentSortType.name());
        editor.apply();
    }

    private void loadSavedFilters() {
        // Load saved filter selections from SharedPreferences
        String savedTeams = sharedPreferences.getString(KEY_SELECTED_TEAMS, "");
        String savedCourts = sharedPreferences.getString(KEY_SELECTED_COURTS, "");
        String savedDays = sharedPreferences.getString(KEY_SELECTED_DAYS, "");
        String savedMonths = sharedPreferences.getString(KEY_SELECTED_MONTHS, "");
        
        if (!savedTeams.isEmpty()) {
            selectedTeamIds = new HashSet<>(java.util.Arrays.asList(savedTeams.split(",")));
        }
        if (!savedCourts.isEmpty()) {
            selectedCourtIds = new HashSet<>(java.util.Arrays.asList(savedCourts.split(",")));
        }
        if (!savedDays.isEmpty()) {
            selectedDays = new HashSet<>(java.util.Arrays.asList(savedDays.split(",")));
        }
        if (!savedMonths.isEmpty()) {
            String[] monthStrings = savedMonths.split(",");
            for (String monthStr : monthStrings) {
                try {
                    selectedMonths.add(Integer.parseInt(monthStr));
                } catch (NumberFormatException e) {
                    // Skip invalid entries
                }
            }
        }
    }

    private void saveFilters() {
        // Save current filter selections to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SELECTED_TEAMS, String.join(",", selectedTeamIds));
        editor.putString(KEY_SELECTED_COURTS, String.join(",", selectedCourtIds));
        editor.putString(KEY_SELECTED_DAYS, String.join(",", selectedDays));
        
        // Convert Integer set to comma-separated string
        List<String> monthStrings = new ArrayList<>();
        for (Integer month : selectedMonths) {
            monthStrings.add(String.valueOf(month));
        }
        editor.putString(KEY_SELECTED_MONTHS, String.join(",", monthStrings));
        editor.apply();
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
        btnSelectAllTeams.setOnClickListener(v -> selectAllTeams());
        btnSelectAllCourts.setOnClickListener(v -> selectAllCourts());
        btnSelectAllMonths.setOnClickListener(v -> selectAllMonths());
        btnSelectAllDays.setOnClickListener(v -> selectAllDays());
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

    private void selectAllMonths() {
        // Check whether all months are selected
        boolean allChecked = true;
        for (int i = 0; i < chipGroupMonths.getChildCount(); i++) {
            View child = chipGroupMonths.getChildAt(i);
            if (child instanceof Chip) {
                if (!((Chip) child).isChecked()) {
                    allChecked = false;
                    break;
                }
            }
        }
        
        // If all months are selected, clear them; otherwise select them all
        for (int i = 0; i < chipGroupMonths.getChildCount(); i++) {
            View child = chipGroupMonths.getChildAt(i);
            if (child instanceof Chip) {
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

    private void setupRecyclerView() {
        adapter = new TrainingAdapter(
            training -> {
                Toast.makeText(requireContext(), training.getTeamName() + " - " + training.getStartTime(), Toast.LENGTH_SHORT).show();
            },
            training -> {
                Intent intent = new Intent(requireActivity(), EditTrainingActivity.class);
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
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupFilters() {
        // Setup SearchView for teams
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

        // Setup SearchView for courts
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

        // Setup this week chip
        chipThisWeek.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setShowOnlyThisWeek(isChecked);
        });

        // Setup hide past trainings chip (checked by default)
        chipHidePast.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setHidePastTrainings(isChecked);
        });
    }

    private void setupDayChips() {
        chipGroupDays.removeAllViews();
        // Do not clear selectedDays because they were loaded from SharedPreferences
        
        // All days in order
        String[] days = {"ראשון", "שני", "שלישי", "רביעי", "חמישי", "שישי", "שבת"};
        String[] dayValues = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};

        // Add all days
        for (int i = 0; i < 7; i++) {
            final String dayValue = dayValues[i];
            
            Chip chip = new Chip(requireContext());
            chip.setText(days[i]);
            chip.setCheckable(true);
            chip.setTag(dayValue);
            
            // Check if the day was selected; if nothing is saved, select all
            boolean shouldBeChecked = selectedDays.isEmpty() || selectedDays.contains(dayValue);
            chip.setChecked(shouldBeChecked);
            if (shouldBeChecked && !selectedDays.contains(dayValue)) {
                selectedDays.add(dayValue);
            }

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedDays.add((String) buttonView.getTag());
                } else {
                    selectedDays.remove(buttonView.getTag());
                }
                saveFilters();
                updateDayFilter();
            });

            chipGroupDays.addView(chip);
        }
    }

    private void setupMonthChips() {
        String[] months = {"ינואר", "פברואר", "מרץ", "אפריל", "מאי", "יוני", 
                  "יולי", "אוגוסט", "ספטמבר", "אוקטובר", "נובמבר", "דצמבר"};

        for (int i = 0; i < months.length; i++) {
            String month = months[i];
            int monthValue = i + 1; // Calendar.JANUARY = 0

            Chip chip = new Chip(requireContext());
            chip.setText(month);
            chip.setCheckable(true);
            chip.setTag(monthValue);
            
            // Mark if the month was previously selected
            chip.setChecked(selectedMonths.contains(monthValue));

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedMonths.add((Integer) buttonView.getTag());
                } else {
                    selectedMonths.remove(buttonView.getTag());
                }
                saveFilters();
                updateMonthFilter();
            });

            chipGroupMonths.addView(chip);
        }
    }

    private void loadTeamsAndCourts() {
        // Load teams
        FirebaseDatabase.getInstance().getReference("teams")
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!isAdded() || getContext() == null) {
                        return; // Fragment not attached, skip update
                    }
                    
                    teamsList.clear();
                    chipGroupTeams.removeAllViews();
                    // Do not clear selectedTeamIds because they were loaded from SharedPreferences

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Team team = snapshot.getValue(Team.class);
                        if (team != null) {
                            teamsList.add(team);
                            
                            // For players, include only their teams
                            if (isPlayer) {
                                if (currentUser != null && currentUser.getTeamIds() != null && 
                                    currentUser.getTeamIds().contains(team.getTeamId())) {
                                    addTeamChip(team);
                                }
                            } else if (!isPlayer) {
                                // For non-players, include every team
                                addTeamChip(team);
                            }
                            // If isPlayer is false and currentUser is still null, do not add anything yet
                        }
                    }
                    
                    updateTeamFilter();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(requireContext(), "שגיאה בטעינת קבוצות", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        // Load courts
        courtViewModel = new ViewModelProvider(this).get(CourtViewModel.class);
        courtViewModel.getCourts().observe(getViewLifecycleOwner(), courts -> {
            if (courts != null) {
                courtsList.clear();
                chipGroupCourts.removeAllViews();
                courtsList.addAll(courts);
                for (Court court : courts) {
                    addCourtChip(court);
                }
            }
        });
    }

    private void addTeamChip(Team team) {
        Chip chip = new Chip(requireContext());
        chip.setText(team.getName());
        chip.setCheckable(true);
        chip.setTag(team);
        
        // Check whether the team was selected
        chip.setChecked(selectedTeamIds.contains(team.getTeamId()));

        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Team selectedTeam = (Team) buttonView.getTag();
            if (isChecked) {
                selectedTeamIds.add(selectedTeam.getTeamId());
            } else {
                selectedTeamIds.remove(selectedTeam.getTeamId());
            }
            saveFilters();
            updateTeamFilter();
        });

        chipGroupTeams.addView(chip);
    }

    private void addCourtChip(Court court) {
        Chip chip = new Chip(requireContext());
        chip.setText(court.getName());
        chip.setCheckable(true);
        chip.setTag(court);
        
        // Check whether the court was selected
        chip.setChecked(selectedCourtIds.contains(court.getCourtId()));

        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Court selectedCourt = (Court) buttonView.getTag();
            if (isChecked) {
                selectedCourtIds.add(selectedCourt.getCourtId());
            } else {
                selectedCourtIds.remove(selectedCourt.getCourtId());
            }
            saveFilters();
            updateCourtFilter();
        });

        chipGroupCourts.addView(chip);
    }

    private void filterTeamChips(String query) {
        String lowerCaseQuery = query.toLowerCase();
        for (int i = 0; i < chipGroupTeams.getChildCount(); i++) {
            View child = chipGroupTeams.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                Team team = (Team) chip.getTag();
                if (team != null) {
                    boolean matches = team.getName().toLowerCase().contains(lowerCaseQuery);
                    chip.setVisibility(matches ? View.VISIBLE : View.GONE);
                }
            }
        }
    }

    private void filterCourtChips(String query) {
        String lowerCaseQuery = query.toLowerCase();
        for (int i = 0; i < chipGroupCourts.getChildCount(); i++) {
            View child = chipGroupCourts.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                Court court = (Court) chip.getTag();
                if (court != null) {
                    boolean matches = court.getName().toLowerCase().contains(lowerCaseQuery);
                    chip.setVisibility(matches ? View.VISIBLE : View.GONE);
                }
            }
        }
    }

    private void updateTeamFilter() {
        viewModel.setTeamFilter(selectedTeamIds.isEmpty() ? null : selectedTeamIds);
    }

    private void updateCourtFilter() {
        viewModel.setLocationFilter(selectedCourtIds.isEmpty() ? null : selectedCourtIds);
    }

    private void updateDayFilter() {
        viewModel.setDayFilter(selectedDays.isEmpty() ? null : selectedDays);
    }

    private void updateMonthFilter() {
        viewModel.setMonthFilter(selectedMonths.isEmpty() ? null : selectedMonths);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TrainingViewModel.class);

        viewModel.getFilteredTrainings().observe(getViewLifecycleOwner(), trainings -> {
            if (trainings != null) {
                // Wait for user to be loaded before showing any trainings
                // This prevents showing unfiltered trainings briefly for players
                if (!userLoaded) {
                    android.util.Log.d("ScheduleFragment", "User not loaded yet, showing empty list");
                    adapter.setTrainings(new ArrayList<>());
                    return;
                }
                
                // For players: filter to show only their team's trainings
                if (isPlayer && currentUser != null && currentUser.getTeamIds() != null && !currentUser.getTeamIds().isEmpty()) {
                    android.util.Log.d("ScheduleFragment", "Filtering for player. Teams: " + currentUser.getTeamIds());
                    List<Training> playerTrainings = new ArrayList<>();
                    for (Training training : trainings) {
                        if (currentUser.getTeamIds().contains(training.getTeamId())) {
                            playerTrainings.add(training);
                        }
                    }
                    adapter.setTrainings(playerTrainings);
                } else if (isPlayer) {
                    // Player with no team - show empty list instead of all trainings
                    adapter.setTrainings(new ArrayList<>());
                } else {
                    adapter.setTrainings(trainings);
                }
                
                // Apply saved sort preference
                applyFiltersAndSort();
            }
        });

        viewModel.getTrainings().observe(getViewLifecycleOwner(), trainings -> {
            if (trainings != null) {
                viewModel.setShowOnlyThisWeek(chipThisWeek.isChecked());
            }
        });

        viewModel.getErrors().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFab() {
        fab.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), AddTrainingActivity.class));
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
                        if (!isAdded() || getContext() == null) {
                            return; // Fragment not attached, skip update
                        }
                        
                        currentUser = snapshot.getValue(User.class);
                        if (currentUser != null) {
                            isPlayer = "PLAYER".equals(currentUser.getRole());
                            userLoaded = true; // Mark user as loaded
                            android.util.Log.d("ScheduleFragment", "User loaded: " + currentUser.getRole() + ", isPlayer: " + isPlayer + ", teamIds: " + currentUser.getTeamIds());
                            boolean canEdit = currentUser.isAdmin() || currentUser.isCoordinator();

                            if (!canEdit) {
                                fab.setVisibility(View.GONE);
                            }
                        }
                        
                        // Setup filters after user is loaded
                        setupDayChips();
                        setupMonthChips();
                        loadTeamsAndCourts();
                        setupFilters();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireContext(), "Failed to load user permissions.", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    private void showDeleteConfirmDialog(Training training) {
        new AlertDialog.Builder(requireContext())
            .setTitle("מחיקת אימון")
            .setMessage("האם בטוח שברצונך למחוק אימון זה?\n" + training.getTeamName() + " - " + training.getStartTime() + " עד " + training.getEndTime())
            .setPositiveButton("מחוק", (dialog, which) -> {
                viewModel.deleteTraining(training.getTrainingId());
                Toast.makeText(requireContext(), "אימון נמחק", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("ביטול", null)
            .show();
    }

    private void showDuplicateDialog(Training training) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("שכפל אימון");

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 20);

        TextView message = new TextView(requireContext());
        message.setText("הזן את מספר השבועות לשכפול:");
        message.setTextSize(16f);
        message.setPadding(0, 0, 0, 20);
        layout.addView(message);

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(input);

        builder.setView(layout);

        builder.setPositiveButton("שכפל", (dialog, which) -> {
            String weeksStr = input.getText().toString();
            if (weeksStr.isEmpty()) {
                Toast.makeText(requireContext(), "יש להזין מספר שבועות", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int weeks = Integer.parseInt(weeksStr);
                if (weeks > 0 && weeks <= 52) {
                    duplicateTraining(training, weeks);
                } else {
                    Toast.makeText(requireContext(), "נא להזין מספר בין 1 ל-52", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "מספר לא תקין", Toast.LENGTH_SHORT).show();
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
                }

                @Override
                public void onConflict() {
                }

                @Override
                public void onFailure(String error) {
                }
            });
        }

        Toast.makeText(requireContext(), "אימון שוכפל " + weeks + " שבועות קדימה", Toast.LENGTH_SHORT).show();
    }

    private void applyFiltersAndSort() {
        // Get current trainings from adapter
        List<Training> currentTrainings = new ArrayList<>(adapter.trainings != null ? adapter.trainings : new ArrayList<>());
        
        if (currentTrainings.isEmpty()) {
            return;
        }

        // Apply sorting based on current sort type
        switch (currentSortType) {
            case BY_TEAM:
                currentTrainings.sort((t1, t2) -> t1.getTeamName().compareTo(t2.getTeamName()));
                break;
            case BY_TIME:
            default:
                // Sort by date first, then by start time
                currentTrainings.sort((t1, t2) -> {
                    // First compare by date
                    int dateCompare = Long.compare(t1.getDate(), t2.getDate());
                    if (dateCompare != 0) {
                        return dateCompare;
                    }
                    // If same date, compare by start time
                    return t1.getStartTime().compareTo(t2.getStartTime());
                });
                break;
        }

        // Update adapter with sorted list
        adapter.setTrainings(currentTrainings);
    }
}
