package com.example.testapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.testapp.models.Training;
import com.example.testapp.repository.TrainingRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

public class TrainingViewModel extends ViewModel {
    private final TrainingRepository repository;
    private final LiveData<List<Training>> trainings;
    private final MediatorLiveData<List<Training>> filteredTrainings;
    private Set<String> selectedTeamIds = null;
    private Set<String> selectedDays = null;
    private Set<String> selectedCourtIds = null;
    private Set<Integer> selectedMonths = null;
    private boolean showOnlyThisWeek = false;
    private boolean hidePastTrainings = true;

    public TrainingViewModel() {
        repository = new TrainingRepository();
        trainings = repository.getTrainings();
        filteredTrainings = new MediatorLiveData<>();
        
        // Automatically update filtered trainings when source data changes
        filteredTrainings.addSource(trainings, trainingList -> applyFilters());
    }

    public LiveData<List<Training>> getTrainings() {
        return trainings;
    }
    
    public LiveData<List<Training>> getFilteredTrainings() {
        return filteredTrainings;
    }

    public LiveData<List<Training>> getTrainingsByTeam(String teamId) {
        return repository.getTrainingsByTeam(teamId);
    }

    public LiveData<List<Training>> getTrainingsByCourt(String courtId) {
        return repository.getTrainingsByCourt(courtId);
    }
    
    public void setTeamFilter(Set<String> teamIds) {
        this.selectedTeamIds = teamIds;
        applyFilters();
    }
    
    public void setDayFilter(Set<String> days) {
        this.selectedDays = days;
        applyFilters();
    }
    
    public void setLocationFilter(Set<String> courtIds) {
        this.selectedCourtIds = courtIds;
        applyFilters();
    }
    
    public void setMonthFilter(Set<Integer> months) {
        this.selectedMonths = months;
        applyFilters();
    }
    
    public void setShowOnlyThisWeek(boolean showOnlyThisWeek) {
        this.showOnlyThisWeek = showOnlyThisWeek;
        applyFilters();
    }
    
    public void setHidePastTrainings(boolean hidePastTrainings) {
        this.hidePastTrainings = hidePastTrainings;
        applyFilters();
    }
    
    public void clearFilters() {
        this.selectedTeamIds = null;
        this.selectedDays = null;
        this.selectedCourtIds = null;
        this.selectedMonths = null;
        this.showOnlyThisWeek = false;
        this.hidePastTrainings = true;
        applyFilters();
    }
    
    private void applyFilters() {
        List<Training> allTrainings = trainings.getValue();
        if (allTrainings == null) {
            filteredTrainings.setValue(new ArrayList<>());
            return;
        }
        
        List<Training> filtered = new ArrayList<>();
        
        // Get current week bounds
        Calendar now = Calendar.getInstance();
        Calendar weekStart = (Calendar) now.clone();
        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        weekStart.set(Calendar.HOUR_OF_DAY, 0);
        weekStart.set(Calendar.MINUTE, 0);
        weekStart.set(Calendar.SECOND, 0);
        
        Calendar weekEnd = (Calendar) weekStart.clone();
        weekEnd.add(Calendar.DAY_OF_WEEK, 7);
        
        long weekStartMillis = weekStart.getTimeInMillis();
        long weekEndMillis = weekEnd.getTimeInMillis();
        
        for (Training training : allTrainings) {
            boolean matches = true;
            
            // Apply team filter (multi-select)
            if (selectedTeamIds != null && !selectedTeamIds.isEmpty()) {
                if (!selectedTeamIds.contains(training.getTeamId())) {
                    matches = false;
                }
            }
            
            // Apply day filter (multi-select)
            if (selectedDays != null && !selectedDays.isEmpty()) {
                if (!selectedDays.contains(training.getDayOfWeek())) {
                    matches = false;
                }
            }
            
            // Apply location/court filter (multi-select)
            if (selectedCourtIds != null && !selectedCourtIds.isEmpty()) {
                if (!selectedCourtIds.contains(training.getCourtId())) {
                    matches = false;
                }
            }
            
            // Apply month filter (multi-select)
            if (selectedMonths != null && !selectedMonths.isEmpty()) {
                Calendar trainingCal = Calendar.getInstance();
                trainingCal.setTimeInMillis(training.getDate());
                int trainingMonth = trainingCal.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based
                if (!selectedMonths.contains(trainingMonth)) {
                    matches = false;
                }
            }
            
            // Apply week filter
            if (showOnlyThisWeek) {
                long trainingDate = training.getDate();
                if (trainingDate < weekStartMillis || trainingDate >= weekEndMillis) {
                    matches = false;
                }
            }
            
            // Apply hide past trainings filter using actual end time
            if (hidePastTrainings) {
                long currentTimeMillis = System.currentTimeMillis();
                if (isTrainingPast(training, currentTimeMillis)) {
                    matches = false;
                }
            }
            
            if (matches) {
                filtered.add(training);
            }
        }
        
        filteredTrainings.setValue(filtered);
    }

    private boolean isTrainingPast(Training training, long currentTime) {
        if (training == null) return false;
        if (training.getDate() == 0) return false;

        int endMinutes = timeToMinutes(training.getEndTime());
        if (endMinutes < 0) return false;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(training.getDate());
        cal.set(Calendar.HOUR_OF_DAY, endMinutes / 60);
        cal.set(Calendar.MINUTE, endMinutes % 60);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis() < currentTime;
    }

    private int timeToMinutes(String time) {
        try {
            if (time == null || time.isEmpty()) return -1;
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

    public void addTraining(Training training, TrainingRepository.OnConflictCheckListener listener) {
        repository.addTraining(training, listener);
    }

    public void updateTraining(Training training) {
        repository.updateTraining(training);
    }

    public void deleteTraining(String trainingId) {
        repository.deleteTraining(trainingId);
    }

    public LiveData<String> getErrors() {
        return repository.getErrors();
    }
}
