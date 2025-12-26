package com.example.testapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.testapp.models.Court;
import com.example.testapp.repository.CourtRepository;

import java.util.List;

public class CourtViewModel extends ViewModel {
    private final CourtRepository repository;
    private final LiveData<List<Court>> courts;

    public CourtViewModel() {
        repository = new CourtRepository();
        courts = repository.getCourts();
    }

    public LiveData<List<Court>> getCourts() {
        return courts;
    }

    public void addCourt(Court court) {
        repository.addCourt(court);
    }

    public void updateCourt(Court court) {
        repository.updateCourt(court);
    }

    public void deleteCourt(String courtId) {
        repository.deleteCourt(courtId);
    }

    public LiveData<String> getErrors() {
        return repository.getErrors();
    }
}
