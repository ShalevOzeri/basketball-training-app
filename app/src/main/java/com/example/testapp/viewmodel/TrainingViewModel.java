package com.example.testapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.testapp.models.Training;
import com.example.testapp.repository.TrainingRepository;

import java.util.List;

public class TrainingViewModel extends ViewModel {
    private final TrainingRepository repository;
    private final LiveData<List<Training>> trainings;

    public TrainingViewModel() {
        repository = new TrainingRepository();
        trainings = repository.getTrainings();
    }

    public LiveData<List<Training>> getTrainings() {
        return trainings;
    }

    public LiveData<List<Training>> getTrainingsByTeam(String teamId) {
        return repository.getTrainingsByTeam(teamId);
    }

    public LiveData<List<Training>> getTrainingsByCourt(String courtId) {
        return repository.getTrainingsByCourt(courtId);
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
