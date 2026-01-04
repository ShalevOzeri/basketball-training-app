package com.example.testapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.example.testapp.models.Training;
import com.example.testapp.repository.TrainingRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TrainingViewModel extends ViewModel {
    private final TrainingRepository repository;
    private final LiveData<List<Training>> trainings;
    private final MediatorLiveData<List<Training>> filteredTrainings;
    private String currentTeamFilter = null;
    private String currentDayFilter = null;
    private String currentLocationFilter = null;
    private boolean showOnlyThisWeek = false;

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
    
    public void setTeamFilter(String teamId) {
        this.currentTeamFilter = teamId;
        applyFilters();
    }
    
    public void setDayFilter(String day) {
        this.currentDayFilter = day;
        applyFilters();
    }
    
    public void setLocationFilter(String location) {
        this.currentLocationFilter = location;
        applyFilters();
    }
    
    public void setShowOnlyThisWeek(boolean showOnlyThisWeek) {
        this.showOnlyThisWeek = showOnlyThisWeek;
        applyFilters();
    }
    
    public void clearFilters() {
        this.currentTeamFilter = null;
        this.currentDayFilter = null;
        this.currentLocationFilter = null;
        this.showOnlyThisWeek = false;
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
            
            // Apply team filter
            if (currentTeamFilter != null && !currentTeamFilter.isEmpty()) {
                if (!currentTeamFilter.equals(training.getTeamId())) {
                    matches = false;
                }
            }
            
            // Apply day filter
            if (currentDayFilter != null && !currentDayFilter.isEmpty()) {
                if (!currentDayFilter.equals(training.getDayOfWeek())) {
                    matches = false;
                }
            }
            
            // Apply location filter (by court name)
            if (currentLocationFilter != null && !currentLocationFilter.isEmpty()) {
                if (!currentLocationFilter.equals(training.getCourtName())) {
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
            
            if (matches) {
                filtered.add(training);
            }
        }
        
        filteredTrainings.setValue(filtered);
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
