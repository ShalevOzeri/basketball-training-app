package com.example.testapp.models;

public class User {
    private String userId;
    private String email;
    private String name;
    private String role; // "ADMIN", "COORDINATOR", "COACH"
    private String phone;
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
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getPhone() { return phone; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setRole(String role) { this.role = role; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    // Helper methods
    public boolean isAdmin() { return "ADMIN".equals(role); }
    public boolean isCoordinator() { return "COORDINATOR".equals(role); }
    public boolean isCoach() { return "COACH".equals(role); }
}
