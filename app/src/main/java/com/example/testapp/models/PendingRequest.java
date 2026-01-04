package com.example.testapp.models;

public class PendingRequest {
    private User user;
    private String teamId;

    public PendingRequest(User user, String teamId) {
        this.user = user;
        this.teamId = teamId;
    }

    public User getUser() {
        return user;
    }

    public String getTeamId() {
        return teamId;
    }
}
