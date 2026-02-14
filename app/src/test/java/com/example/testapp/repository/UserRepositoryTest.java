package com.example.testapp.repository;

import com.example.testapp.models.User;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Unit Tests for User Model
 * 
 * Unit tests for User model - tests actual functionality
 * All tests check logic that actually exists in the code
 */
public class UserRepositoryTest {

    /**
     * Test 1: Creating User with Constructor
     * Tests: Constructor initializes all fields correctly
     */
    @Test
    public void testCreateUser_WithConstructor() {
        System.out.println("🧪 Test: Creating User with Constructor");
        // Given
        String userId = "user123";
        String email = "coach@example.com";
        String name = "ישראל ישראלי";
        String role = "COACH";
        String phone = "0501234567";
        
        // When
        User user = new User(userId, email, name, role, phone);
        
        // Then
        assertEquals(userId, user.getUserId());
        assertEquals(email, user.getEmail());
        assertEquals(name, user.getName());
        assertEquals(role, user.getRole());
        assertEquals(phone, user.getPhone());
        assertEquals("NONE", user.getRegistrationStatus()); // Default value
        assertNotNull(user.getTeamIds()); // Initialized to empty list
        assertNotNull(user.getPendingTeamIds());
    }

    /**
     * Test 2: Testing roles - isCoach()
     * Tests: Actual function that exists in User.java
     */
    @Test
    public void testUserRole_IsCoach() {
        System.out.println("🧪 Test: Role - isCoach()");
        // Given
        User coach = new User();
        coach.setRole("COACH");
        
        User player = new User();
        player.setRole("PLAYER");
        
        // When & Then
        assertTrue(coach.isCoach());
        assertFalse(coach.isPlayer());
        assertFalse(coach.isAdmin());
        assertFalse(coach.isCoordinator());
        
        assertFalse(player.isCoach());
        assertTrue(player.isPlayer());
    }

    /**
     * Test 3: Testing roles - isAdmin()
     * Tests: System correctly identifies Admin
     */
    @Test
    public void testUserRole_IsAdmin() {
        System.out.println("🧪 Test: Role - isAdmin()");
        // Given
        User admin = new User();
        admin.setRole("ADMIN");
        
        // When & Then
        assertTrue(admin.isAdmin());
        assertFalse(admin.isCoach());
        assertFalse(admin.isPlayer());
        assertFalse(admin.isCoordinator());
    }

    /**
     * Test 4: Testing registration status - isPending()
     * Tests: Actual function isPending() in User
     */
    @Test
    public void testRegistrationStatus_IsPending() {
        System.out.println("🧪 Test: Registration status - isPending()");
        // Given
        User pendingUser = new User();
        pendingUser.setRegistrationStatus("PENDING");
        
        User approvedUser = new User();
        approvedUser.setRegistrationStatus("APPROVED");
        
        // When & Then
        assertTrue(pendingUser.isPending());
        assertFalse(pendingUser.isApproved());
        
        assertFalse(approvedUser.isPending());
        assertTrue(approvedUser.isApproved());
    }

    /**
     * Test 5: Testing team list - getTeamIds()
     * Tests: Function returns empty list instead of null
     */
    @Test
    public void testTeamIds_ReturnsEmptyListNotNull() {
        System.out.println("🧪 Test: getTeamIds() - empty list");
        // Given
        User user = new User();
        user.setTeamIds(null); // Set null explicitly
        
        // When
        List<String> teamIds = user.getTeamIds();
        
        // Then
        assertNotNull(teamIds); // Not null!
        assertEquals(0, teamIds.size()); // Empty list
    }

    /**
     * Test 6: Adding team to pendingTeamIds
     * Tests: setPendingTeamId() - function that adds to list
     */
    @Test
    public void testAddPendingTeam() {
        System.out.println("🧪 Test: Adding team to pendingTeamIds");
        // Given
        User user = new User();
        
        // When
        user.setPendingTeamId("team1");
        user.setPendingTeamId("team2");
        user.setPendingTeamId("team1"); // duplicate
        
        // Then
        List<String> pending = user.getPendingTeamIds();
        assertEquals(2, pending.size()); // Doesn't add duplicates
        assertTrue(pending.contains("team1"));
        assertTrue(pending.contains("team2"));
    }

    /**
     * Test 7: getPendingTeamId() - first team
     * Tests: Function returns the first team
     */
    @Test
    public void testGetFirstPendingTeam() {
        System.out.println("🧪 Test: getPendingTeamId() - first team");
        // Given
        User user = new User();
        List<String> pending = new ArrayList<>();
        pending.add("team1");
        pending.add("team2");
        user.setPendingTeamIds(pending);
        
        // When
        String first = user.getPendingTeamId();
        
        // Then
        assertEquals("team1", first); // First in list
    }

    /**
     * Test 8: getPendingTeamId() - empty list
     * Tests: Function returns null when no pending teams
     */
    @Test
    public void testGetFirstPendingTeam_Empty() {
        System.out.println("🧪 Test: getPendingTeamId() - empty list");
        // Given
        User user = new User();
        
        // When
        String first = user.getPendingTeamId();
        
        // Then
        assertNull(first); // No teams - returns null
    }
}
