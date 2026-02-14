package com.example.testapp.models;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for User model
 * 
 * Tests the functions and business logic of the user model:
 * - User creation
 * - Roles (ADMIN, COORDINATOR, COACH, PLAYER)
 * - Team management
 * - Registration status
 */
public class UserTest {

    @Test
    public void constructor_WithValidData_CreatesUser() {
        System.out.println("🧪 Test: Creating user with valid data");
        // Test: Creating user with valid data
        User user = new User("user123", "test@example.com", "יוסי כהן", "PLAYER", "0501234567");
        
        assertNotNull("User should not be null", user);
        assertEquals("user123", user.getUserId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("יוסי כהן", user.getName());
        assertEquals("PLAYER", user.getRole());
        assertEquals("0501234567", user.getPhone());
    }

    @Test
    public void constructor_InitializesEmptyLists() {
        System.out.println("🧪 Test: Initializing empty lists");
        // Test: Constructor initializes empty lists
        User user = new User("user123", "test@example.com", "משה לוי", "COACH", "0501111111");
        
        assertNotNull("Team IDs list should not be null", user.getTeamIds());
        assertTrue("Team IDs list should be empty", user.getTeamIds().isEmpty());
        assertNotNull("Pending team IDs list should not be null", user.getPendingTeamIds());
        assertTrue("Pending team IDs list should be empty", user.getPendingTeamIds().isEmpty());
    }

    @Test
    public void constructor_SetsDefaultRegistrationStatus() {
        System.out.println("🧪 Test: Default registration status");
        // Test: Registration status starts as NONE
        User user = new User("user123", "test@example.com", "דוד אברהם", "PLAYER", "0502222222");
        
        assertEquals("NONE", user.getRegistrationStatus());
        assertFalse("User should not be pending", user.isPending());
        assertFalse("User should not be approved", user.isApproved());
    }

    @Test
    public void constructor_SetsCreatedAtTimestamp() {
        System.out.println("🧪 Test: Creation timestamp");
        // Test: Creation time is set
        long beforeCreation = System.currentTimeMillis();
        User user = new User("user123", "test@example.com", "שרה כהן", "COORDINATOR", "0503333333");
        long afterCreation = System.currentTimeMillis();
        
        assertTrue("Created at should be set", user.getCreatedAt() > 0);
        assertTrue("Created at should be between before and after creation", 
                  user.getCreatedAt() >= beforeCreation && user.getCreatedAt() <= afterCreation);
    }

    @Test
    public void isAdmin_AdminRole_ReturnsTrue() {
        System.out.println("🧪 Test: ADMIN role");
        // Test: isAdmin returns true for admin
        User user = new User("user123", "admin@example.com", "Admin User", "ADMIN", "0504444444");
        
        assertTrue("User with ADMIN role should return true for isAdmin()", user.isAdmin());
        assertFalse("Admin should not be coordinator", user.isCoordinator());
        assertFalse("Admin should not be coach", user.isCoach());
        assertFalse("Admin should not be player", user.isPlayer());
    }

    @Test
    public void isCoordinator_CoordinatorRole_ReturnsTrue() {
        System.out.println("🧪 Test: COORDINATOR role");
        // Test: isCoordinator returns true for coordinator
        User user = new User("user123", "coord@example.com", "Coordinator User", "COORDINATOR", "0505555555");
        
        assertTrue("User with COORDINATOR role should return true for isCoordinator()", user.isCoordinator());
        assertFalse("Coordinator should not be admin", user.isAdmin());
        assertFalse("Coordinator should not be coach", user.isCoach());
        assertFalse("Coordinator should not be player", user.isPlayer());
    }

    @Test
    public void isCoach_CoachRole_ReturnsTrue() {
        System.out.println("🧪 Test: COACH role");
        // Test: isCoach returns true for coach
        User user = new User("user123", "coach@example.com", "Coach User", "COACH", "0506666666");
        
        assertTrue("User with COACH role should return true for isCoach()", user.isCoach());
        assertFalse("Coach should not be admin", user.isAdmin());
        assertFalse("Coach should not be coordinator", user.isCoordinator());
        assertFalse("Coach should not be player", user.isPlayer());
    }

    @Test
    public void isPlayer_PlayerRole_ReturnsTrue() {
        System.out.println("🧪 Test: PLAYER role");
        // Test: isPlayer returns true for player
        User user = new User("user123", "player@example.com", "Player User", "PLAYER", "0507777777");
        
        assertTrue("User with PLAYER role should return true for isPlayer()", user.isPlayer());
        assertFalse("Player should not be admin", user.isAdmin());
        assertFalse("Player should not be coordinator", user.isCoordinator());
        assertFalse("Player should not be coach", user.isCoach());
    }

    @Test
    public void isPending_PendingStatus_ReturnsTrue() {
        System.out.println("🧪 Test: PENDING status");
        // Test: isPending returns true for PENDING status
        User user = new User("user123", "test@example.com", "Test User", "PLAYER", "0508888888");
        user.setRegistrationStatus("PENDING");
        
        assertTrue("User with PENDING status should return true for isPending()", user.isPending());
        assertFalse("Pending user should not be approved", user.isApproved());
    }

    @Test
    public void isApproved_ApprovedStatus_ReturnsTrue() {
        System.out.println("🧪 Test: APPROVED status");
        // Test: isApproved returns true for APPROVED status
        User user = new User("user123", "test@example.com", "Test User", "PLAYER", "0509999999");
        user.setRegistrationStatus("APPROVED");
        
        assertTrue("User with APPROVED status should return true for isApproved()", user.isApproved());
        assertFalse("Approved user should not be pending", user.isPending());
    }

    @Test
    public void setTeamIds_WithList_UpdatesList() {
        System.out.println("🧪 Test: Updating team list");
        // Test: Updating team list
        User user = new User("user123", "test@example.com", "Test Player", "PLAYER", "0501111111");
        
        List<String> teamIds = new ArrayList<>();
        teamIds.add("team1");
        teamIds.add("team2");
        user.setTeamIds(teamIds);
        
        assertEquals("Team IDs list should have 2 teams", 2, user.getTeamIds().size());
        assertTrue("Team IDs should contain team1", user.getTeamIds().contains("team1"));
        assertTrue("Team IDs should contain team2", user.getTeamIds().contains("team2"));
    }

    @Test
    public void setPendingTeamId_AddsTeamToList() {
        System.out.println("🧪 Test: Adding pending team");
        // Test: Adding pending team
        User user = new User("user123", "test@example.com", "Test Player", "PLAYER", "0502222222");
        
        user.setPendingTeamId("team1");
        
        assertEquals("Should have 1 pending team", 1, user.getPendingTeamIds().size());
        assertEquals("First pending team should be team1", "team1", user.getPendingTeamId());
    }

    @Test
    public void setPendingTeamId_DuplicateTeam_DoesNotAddTwice() {
        System.out.println("🧪 Test: Preventing duplicates in pending teams");
        // Test: Duplicate team not added twice
        User user = new User("user123", "test@example.com", "Test Player", "PLAYER", "0503333333");
        
        user.setPendingTeamId("team1");
        user.setPendingTeamId("team1"); // Same team again
        
        assertEquals("Should have only 1 pending team (no duplicates)", 1, user.getPendingTeamIds().size());
    }

    @Test
    public void setPendingTeamId_MultipleDifferentTeams_AddsAll() {
        System.out.println("🧪 Test: Adding multiple pending teams");
        // Test: Multiple different teams added
        User user = new User("user123", "test@example.com", "Test Player", "PLAYER", "0504444444");
        
        user.setPendingTeamId("team1");
        user.setPendingTeamId("team2");
        user.setPendingTeamId("team3");
        
        assertEquals("Should have 3 pending teams", 3, user.getPendingTeamIds().size());
    }

    @Test
    public void getPendingTeamId_EmptyList_ReturnsNull() {
        System.out.println("🧪 Test: Pending team - empty list");
        // Test: Empty list returns null
        User user = new User("user123", "test@example.com", "Test Player", "PLAYER", "0505555555");
        
        assertNull("Should return null when no pending teams", user.getPendingTeamId());
    }

    @Test
    public void getTeamIds_NullList_ReturnsEmptyList() {
        System.out.println("🧪 Test: teamIds - null list");
        // Test: Null list returns empty list (not null)
        User user = new User();
        user.setTeamIds(null);
        
        assertNotNull("getTeamIds should never return null", user.getTeamIds());
        assertTrue("Should return empty list when teamIds is null", user.getTeamIds().isEmpty());
    }

    @Test
    public void getPendingTeamIds_NullList_ReturnsEmptyList() {
        System.out.println("🧪 Test: pendingTeamIds - null list");
        // Test: Null list returns empty list (not null)
        User user = new User();
        user.setPendingTeamIds(null);
        
        assertNotNull("getPendingTeamIds should never return null", user.getPendingTeamIds());
        assertTrue("Should return empty list when pendingTeamIds is null", user.getPendingTeamIds().isEmpty());
    }

    @Test
    public void setPlayerId_UpdatesPlayerId() {
        System.out.println("🧪 Test: Updating playerId");
        // Test: Updating playerId
        User user = new User("user123", "test@example.com", "Test Player", "PLAYER", "0506666666");
        
        user.setPlayerId("player123");
        
        assertEquals("player123", user.getPlayerId());
    }

    @Test
    public void setTeamId_CoachRole_UpdatesTeamId() {
        System.out.println("🧪 Test: Updating teamId for coach");
        // Test: Coach gets one teamId
        User user = new User("user123", "coach@example.com", "Coach User", "COACH", "0507777777");
        
        user.setTeamId("team123");
        
        assertEquals("team123", user.getTeamId());
    }

    @Test
    public void emptyConstructor_CreatesEmptyUser() {
        System.out.println("🧪 Test: Empty constructor for user");
        // Test: Empty constructor (required for Firebase)
        User user = new User();
        
        assertNotNull("Empty constructor should create user", user);
        assertNull("User ID should be null", user.getUserId());
        assertNull("Email should be null", user.getEmail());
        assertNull("Name should be null", user.getName());
    }

    @Test
    public void setName_UpdatesName() {
        System.out.println("🧪 Test: Updating name");
        // Test: Updating name
        User user = new User();
        user.setName("עדכון שם");
        
        assertEquals("עדכון שם", user.getName());
    }

    @Test
    public void setEmail_UpdatesEmail() {
        System.out.println("🧪 Test: Updating email");
        // Test: Updating email
        User user = new User();
        user.setEmail("new@example.com");
        
        assertEquals("new@example.com", user.getEmail());
    }

    @Test
    public void setPhone_UpdatesPhone() {
        System.out.println("🧪 Test: Updating phone");
        // Test: Updating phone
        User user = new User();
        user.setPhone("0508888888");
        
        assertEquals("0508888888", user.getPhone());
    }

    @Test
    public void setRole_UpdatesRole() {
        System.out.println("🧪 Test: Updating role");
        // Test: Updating role
        User user = new User();
        user.setRole("ADMIN");
        
        assertEquals("ADMIN", user.getRole());
        assertTrue("Should be admin", user.isAdmin());
    }
}
