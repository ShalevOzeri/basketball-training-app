package com.example.testapp.models;

import com.example.testapp.models.Team;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit Tests for Team model
 * Tests team creation, getters/setters, and business logic
 */
public class TeamTest {

    @Test
    public void testCreateTeam_AllFieldsSet() {
        System.out.println("🧪 Test: Creating team with all fields");
        // Arrange
        long beforeCreation = System.currentTimeMillis();
        
        // Act
        Team team = new Team(
            "team123",
            "מכבי תל אביב",
            "U14",
            "Advanced",
            "coach123",
            "דני רודיק",
            "#FF0000"
        );
        
        long afterCreation = System.currentTimeMillis();
        
        // Assert
        assertEquals("team123", team.getTeamId());
        assertEquals("מכבי תל אביב", team.getName());
        assertEquals("U14", team.getAgeGroup());
        assertEquals("Advanced", team.getLevel());
        assertEquals("coach123", team.getCoachId());
        assertEquals("דני רודיק", team.getCoachName());
        assertEquals("#FF0000", team.getColor());
        assertEquals(0, team.getNumberOfPlayers()); // Default value
        assertTrue(team.getCreatedAt() >= beforeCreation && team.getCreatedAt() <= afterCreation);
        assertTrue(team.getUpdatedAt() >= beforeCreation && team.getUpdatedAt() <= afterCreation);
    }

    @Test
    public void testTeamInitialization_DefaultValues() {
        System.out.println("🧪 Test: Team initialization - default values");
        // Act
        Team team = new Team(
            null,
            "הפועל ירושלים",
            "U12",
            "Beginner",
            "",
            "ללא מאמן",
            "#3DDC84"
        );
        
        // Assert
        assertNull(team.getTeamId());
        assertEquals(0, team.getNumberOfPlayers());
        assertTrue(team.getCreatedAt() > 0);
        assertTrue(team.getUpdatedAt() > 0);
    }

    @Test
    public void testSetNumberOfPlayers() {
        System.out.println("🧪 Test: Updating number of players");
        // Arrange
        Team team = new Team(
            "team456",
            "מכבי חיפה",
            "U16",
            "Intermediate",
            "coach456",
            "יוסי אבוקסיס",
            "#0000FF"
        );
        
        // Act
        team.setNumberOfPlayers(15);
        
        // Assert
        assertEquals(15, team.getNumberOfPlayers());
    }

    @Test
    public void testToString_Format() {
        System.out.println("🧪 Test: toString format");
        // Arrange
        Team team = new Team(
            "team789",
            "הפועל חולון",
            "U18",
            "Advanced",
            "coach789",
            "ארז אדלשטיין",
            "#FFEB3B"
        );
        
        // Act
        String result = team.toString();
        
        // Assert
        assertEquals("הפועל חולון (U18)", result);
    }

    @Test
    public void testSettersUpdate_AllFields() {
        System.out.println("🧪 Test: Updating all fields");
        // Arrange
        Team team = new Team();
        
        // Act
        team.setTeamId("newTeam123");
        team.setName("אליצור נתניה");
        team.setAgeGroup("U20");
        team.setLevel("Professional");
        team.setCoachId("newCoach123");
        team.setCoachName("מולי קינן");
        team.setColor("#9C27B0");
        team.setNumberOfPlayers(12);
        team.setCreatedAt(1000000L);
        team.setUpdatedAt(2000000L);
        
        // Assert
        assertEquals("newTeam123", team.getTeamId());
        assertEquals("אליצור נתניה", team.getName());
        assertEquals("U20", team.getAgeGroup());
        assertEquals("Professional", team.getLevel());
        assertEquals("newCoach123", team.getCoachId());
        assertEquals("מולי קינן", team.getCoachName());
        assertEquals("#9C27B0", team.getColor());
        assertEquals(12, team.getNumberOfPlayers());
        assertEquals(1000000L, team.getCreatedAt());
        assertEquals(2000000L, team.getUpdatedAt());
    }

    @Test
    public void testColor_HexFormat() {
        System.out.println("🧪 Test: Color hex format");
        // Arrange & Act
        Team team1 = new Team(
            "t1", "Team 1", "U12", "Beginner", "c1", "Coach 1", "#3DDC84"
        );
        Team team2 = new Team(
            "t2", "Team 2", "U14", "Intermediate", "c2", "Coach 2", "#FF5733"
        );
        
        // Assert
        assertTrue(team1.getColor().startsWith("#"));
        assertTrue(team2.getColor().startsWith("#"));
        assertEquals(7, team1.getColor().length()); // #RRGGBB format
        assertEquals(7, team2.getColor().length());
    }

    @Test
    public void testEmptyConstructor_FirebaseCompatibility() {
        System.out.println("🧪 Test: Empty constructor - Firebase");
        // Act
        Team team = new Team();
        
        // Assert - Should not throw NullPointerException
        assertNull(team.getName());
        assertNull(team.getAgeGroup());
        assertEquals(0, team.getNumberOfPlayers());
    }

    @Test
    public void testUpdateTeamData_ReflectsChanges() {
        System.out.println("🧪 Test: Updating team data");
        // Arrange
        Team team = new Team(
            "team001",
            "מכבי ראשון",
            "U12",
            "Beginner",
            "coach001",
            "משה לוי",
            "#2196F3"
        );
        
        // Act - Simulate team update
        team.setName("מכבי ראשון לציון");
        team.setLevel("Intermediate");
        team.setNumberOfPlayers(10);
        long updateTime = System.currentTimeMillis();
        team.setUpdatedAt(updateTime);
        
        // Assert
        assertEquals("מכבי ראשון לציון", team.getName());
        assertEquals("Intermediate", team.getLevel());
        assertEquals(10, team.getNumberOfPlayers());
        assertEquals(updateTime, team.getUpdatedAt());
    }

    @Test
    public void testCoachChange_UpdatesCoachInfo() {
        System.out.println("🧪 Test: Changing coach");
        // Arrange
        Team team = new Team(
            "team002",
            "הפועל גליל עליון",
            "U16",
            "Advanced",
            "coach_old",
            "יוסי כהן",
            "#F44336"
        );
        
        // Act
        team.setCoachId("coach_new");
        team.setCoachName("דוד מזרחי");
        
        // Assert
        assertEquals("coach_new", team.getCoachId());
        assertEquals("דוד מזרחי", team.getCoachName());
    }

    @Test
    public void testTeamWithNullCoach() {
        System.out.println("🧪 Test: Team without coach");
        // Act
        Team team = new Team(
            "team003",
            "קבוצה ללא מאמן",
            "U10",
            "Beginner",
            null,
            "ללא מאמן",
            "#00BCD4"
        );
        
        // Assert
        assertNull(team.getCoachId());
        assertEquals("ללא מאמן", team.getCoachName());
    }
}
