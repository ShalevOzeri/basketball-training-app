package com.example.testapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.testapp.models.Team;
import com.example.testapp.repository.TeamRepository;

import java.util.List;

public class TeamViewModel extends ViewModel {
    private final TeamRepository repository;
    private final LiveData<List<Team>> teams;

    public TeamViewModel() {
        repository = new TeamRepository();
        teams = repository.getTeams();
    }

    public LiveData<List<Team>> getTeams() {
        return teams;
    }

    public void filterByCoach(String coachId) {
        repository.setCoachFilter(coachId);
    }

    public void clearFilter() {
        repository.clearFilter();
    }

    public void addTeam(Team team) {
        repository.addTeam(team);
    }

    public void updateTeam(Team team) {
        repository.updateTeam(team);
    }

    public void deleteTeam(String teamId) {
        repository.deleteTeam(teamId);
    }

    public LiveData<String> getErrors() {
        return repository.getErrors();
    }
}
