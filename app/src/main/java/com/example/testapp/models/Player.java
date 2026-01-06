package com.example.testapp.models;

public class Player {
    private String playerId;
    private String userId; // Link to User
    private String firstName;
    private String lastName;
    private String grade; // כיתה
    private String school; // בית ספר
    private String playerPhone; // טלפון שחקן
    private String parentPhone; // טלפון הורה
    private String idNumber; // מספר תז
    private String birthDate; // תאריך לידה
    private String shirtSize; // מידת גופיה
    private String jerseyNumber = ""; // מספר גופיה - מאותחל למחרוזת ריקה כדי שהשדה תמיד יופיע ב-Firebase
    private String teamId;
    private long createdAt;
    private long updatedAt;

    public Player() {
        // Required empty constructor for Firebase
    }

    public Player(String playerId, String userId, String firstName, String lastName, 
                  String grade, String school, String playerPhone, String parentPhone,
                  String idNumber, String birthDate, String shirtSize, String jerseyNumber, String teamId) {
        this.playerId = playerId;
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.grade = grade;
        this.school = school;
        this.playerPhone = playerPhone;
        this.parentPhone = parentPhone;
        this.idNumber = idNumber;
        this.birthDate = birthDate;
        this.shirtSize = shirtSize;
        this.jerseyNumber = jerseyNumber;
        this.teamId = teamId;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    // Constructor for minimal player creation (when approving a registration)
    public Player(String playerId, String userId, String fullName, String teamId, long createdAt) {
        this.playerId = playerId;
        this.userId = userId;
        
        // Split full name into first and last
        String[] nameParts = fullName.split(" ", 2);
        this.firstName = nameParts[0];
        this.lastName = nameParts.length > 1 ? nameParts[1] : "";
        
        this.teamId = teamId;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.jerseyNumber = ""; // Default jersey number - empty string, not null
    }

    // Getters
    public String getPlayerId() { return playerId; }
    public String getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getGrade() { return grade; }
    public String getSchool() { return school; }
    public String getPlayerPhone() { return playerPhone; }
    public String getParentPhone() { return parentPhone; }
    public String getIdNumber() { return idNumber; }
    public String getBirthDate() { return birthDate; }
    public String getShirtSize() { return shirtSize; }
    public String getJerseyNumber() { return jerseyNumber; }
    public String getTeamId() { return teamId; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    // Setters
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setGrade(String grade) { this.grade = grade; }
    public void setSchool(String school) { this.school = school; }
    public void setPlayerPhone(String playerPhone) { this.playerPhone = playerPhone; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public void setShirtSize(String shirtSize) { this.shirtSize = shirtSize; }
    public void setJerseyNumber(String jerseyNumber) { this.jerseyNumber = jerseyNumber; }
    public void setTeamId(String teamId) { this.teamId = teamId; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
