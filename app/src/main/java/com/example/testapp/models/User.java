package com.example.testapp.models;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String userId;
    private String email;
    private String name;
    private String role; // "ADMIN", "COORDINATOR", "COACH", "PLAYER"
    private String phone;
    private String teamId; // For COACH roles (single team)
    private List<String> teamIds; // For PLAYER roles (multiple teams)
    private List<String> pendingTeamIds; // Teams waiting for approval
    private String registrationStatus; // "NONE", "PENDING", "APPROVED"
    private String playerId; // For PLAYER roles - link to player record
    private long createdAt;

    public User() {
        // Required empty constructor for Firebase
    }

    public User(String userId, String email, String name, String role, String phone) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.teamId = null; // Will be set later
        this.teamIds = new ArrayList<>();
        this.pendingTeamIds = new ArrayList<>();
        this.registrationStatus = "NONE";
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getPhone() { return phone; }
    public String getTeamId() { return teamId; }
    public List<String> getTeamIds() { return teamIds != null ? teamIds : new ArrayList<>(); }
    public String getPendingTeamId() { return pendingTeamIds != null && !pendingTeamIds.isEmpty() ? pendingTeamIds.get(0) : null; }
    public List<String> getPendingTeamIds() { return pendingTeamIds != null ? pendingTeamIds : new ArrayList<>(); }
    public String getRegistrationStatus() { return registrationStatus; }
    public String getPlayerId() { return playerId; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setRole(String role) { this.role = role; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setTeamId(String teamId) { this.teamId = teamId; }
    public void setTeamIds(List<String> teamIds) { this.teamIds = teamIds; }
    public void setPendingTeamId(String pendingTeamId) { 
        if (this.pendingTeamIds == null) this.pendingTeamIds = new ArrayList<>();
        if (!this.pendingTeamIds.contains(pendingTeamId)) {
            this.pendingTeamIds.add(pendingTeamId);
        }
    }
    public void setPendingTeamIds(List<String> pendingTeamIds) { this.pendingTeamIds = pendingTeamIds; }
    public void setRegistrationStatus(String registrationStatus) { this.registrationStatus = registrationStatus; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Helper methods
    public boolean isAdmin() { return "ADMIN".equals(role); }
    public boolean isCoordinator() { return "COORDINATOR".equals(role); }
    public boolean isCoach() { return "COACH".equals(role); }
    public boolean isPlayer() { return "PLAYER".equals(role); }
    public boolean isPending() { return "PENDING".equals(registrationStatus); }
    public boolean isApproved() { return "APPROVED".equals(registrationStatus); }
}
