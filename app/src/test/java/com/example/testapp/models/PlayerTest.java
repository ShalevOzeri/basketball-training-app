package com.example.testapp.models;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Player model
 * 
 * Tests the functions and business logic of the player model:
 * - Player creation
 * - Personal details
 * - Team assignment
 * - Jersey number
 * - Full name
 */
public class PlayerTest {

    @Test
    public void constructor_FullDetails_CreatesPlayer() {
        System.out.println("🧪 Test: Constructor with all player details");
        // Test: Creating player with all details
        Player player = new Player(
            "player123", 
            "user456", 
            "יוסי", 
            "כהן",
            "י", 
            "תיכון הדר", 
            "0501234567", 
            "0509876543",
            "123456789", 
            "01/01/2010", 
            "M", 
            "23",
            "team789"
        );
        
        assertNotNull("Player should not be null", player);
        assertEquals("player123", player.getPlayerId());
        assertEquals("user456", player.getUserId());
        assertEquals("יוסי", player.getFirstName());
        assertEquals("כהן", player.getLastName());
        assertEquals("י", player.getGrade());
        assertEquals("תיכון הדר", player.getSchool());
        assertEquals("0501234567", player.getPlayerPhone());
        assertEquals("0509876543", player.getParentPhone());
        assertEquals("123456789", player.getIdNumber());
        assertEquals("01/01/2010", player.getBirthDate());
        assertEquals("M", player.getShirtSize());
        assertEquals("23", player.getJerseyNumber());
        assertEquals("team789", player.getTeamId());
    }

    @Test
    public void constructor_MinimalDetails_CreatesPlayer() {
        System.out.println("🧪 Test: Constructor with minimal player details");
        // Test: Creating player with minimal details
        Player player = new Player("player123", "user456", "דוד לוי", "team789", System.currentTimeMillis());
        
        assertNotNull("Player should not be null", player);
        assertEquals("player123", player.getPlayerId());
        assertEquals("user456", player.getUserId());
        assertEquals("דוד", player.getFirstName());
        assertEquals("לוי", player.getLastName());
        assertEquals("team789", player.getTeamId());
        assertEquals("", player.getJerseyNumber()); // Default value
    }

    @Test
    public void constructor_MinimalDetails_SplitsName() {
        System.out.println("🧪 Test: Splitting full name");
        // Test: Splitting full name into first and last name
        Player player = new Player("player123", "user456", "משה אברהם", "team789", System.currentTimeMillis());
        
        assertEquals("משה", player.getFirstName());
        assertEquals("אברהם", player.getLastName());
    }

    @Test
    public void constructor_MinimalDetails_SingleName_SetsLastNameEmpty() {
        System.out.println("🧪 Test: Single name without last name");
        // Test: Single name (without last name)
        Player player = new Player("player123", "user456", "יוסי", "team789", System.currentTimeMillis());
        
        assertEquals("יוסי", player.getFirstName());
        assertEquals("", player.getLastName());
    }

    @Test
    public void constructor_MinimalDetails_ThreePartName_SplitsCorrectly() {
        System.out.println("🧪 Test: Splitting three-part name");
        // Test: Three-part name splits correctly
        Player player = new Player("player123", "user456", "דוד בן גוריון", "team789", System.currentTimeMillis());
        
        assertEquals("דוד", player.getFirstName());
        assertEquals("בן גוריון", player.getLastName()); // Rest of name goes to last name
    }

    @Test
    public void constructor_SetsTimestamps() {
        System.out.println("🧪 Test: Creation and update timestamps");
        // Test: Creation and update times are set
        long beforeCreation = System.currentTimeMillis();
        Player player = new Player(
            "player123", "user456", "שרה", "כהן",
            "ט", "תיכון ABC", "0501111111", "0502222222",
            "111111111", "15/05/2011", "S", "10", "team123"
        );
        long afterCreation = System.currentTimeMillis();
        
        assertTrue("Created at should be set", player.getCreatedAt() > 0);
        assertTrue("Updated at should be set", player.getUpdatedAt() > 0);
        assertTrue("Created at should be between before and after", 
                  player.getCreatedAt() >= beforeCreation && player.getCreatedAt() <= afterCreation);
        assertEquals("Created at and updated at should be equal initially", 
                    player.getCreatedAt(), player.getUpdatedAt());
    }

    @Test
    public void getFullName_WithBothNames_ReturnsCombinedName() {
        System.out.println("🧪 Test: Combined full name");
        // Test: Full name returns combination of first and last name
        Player player = new Player();
        player.setFirstName("יעקב");
        player.setLastName("לוי");
        
        assertEquals("יעקב לוי", player.getFullName());
    }

    @Test
    public void getFullName_OnlyFirstName_ReturnsFirstNameWithSpace() {
        System.out.println("🧪 Test: Full name - only first name");
        // Test: Only first name
        Player player = new Player();
        player.setFirstName("מיכל");
        player.setLastName("");
        
        assertEquals("מיכל ", player.getFullName());
    }

    @Test
    public void getFullName_EmptyNames_ReturnsSpace() {
        System.out.println("🧪 Test: Full name - empty names");
        // Test: Empty names
        Player player = new Player();
        player.setFirstName("");
        player.setLastName("");
        
        assertEquals(" ", player.getFullName());
    }

    @Test
    public void setJerseyNumber_UpdatesNumber() {
        System.out.println("🧪 Test: Updating jersey number");
        // Test: Updating jersey number
        Player player = new Player();
        player.setJerseyNumber("99");
        
        assertEquals("99", player.getJerseyNumber());
    }

    @Test
    public void setJerseyNumber_EmptyString_Allowed() {
        System.out.println("🧪 Test: Empty jersey number");
        // Test: Empty string allowed
        Player player = new Player();
        player.setJerseyNumber("");
        
        assertEquals("", player.getJerseyNumber());
    }

    @Test
    public void setGrade_UpdatesGrade() {
        System.out.println("🧪 Test: Updating grade");
        // Test: Updating grade
        Player player = new Player();
        player.setGrade("יא");
        
        assertEquals("יא", player.getGrade());
    }

    @Test
    public void setSchool_UpdatesSchool() {
        System.out.println("🧪 Test: Updating school");
        // Test: Updating school
        Player player = new Player();
        player.setSchool("תיכון הרצליה");
        
        assertEquals("תיכון הרצליה", player.getSchool());
    }

    @Test
    public void setPlayerPhone_UpdatesPhone() {
        System.out.println("🧪 Test: Updating player phone");
        // Test: Updating player phone
        Player player = new Player();
        player.setPlayerPhone("0503333333");
        
        assertEquals("0503333333", player.getPlayerPhone());
    }

    @Test
    public void setParentPhone_UpdatesPhone() {
        System.out.println("🧪 Test: Updating parent phone");
        // Test: Updating parent phone
        Player player = new Player();
        player.setParentPhone("0504444444");
        
        assertEquals("0504444444", player.getParentPhone());
    }

    @Test
    public void setIdNumber_UpdatesIdNumber() {
        System.out.println("🧪 Test: Updating ID number");
        // Test: Updating ID number
        Player player = new Player();
        player.setIdNumber("987654321");
        
        assertEquals("987654321", player.getIdNumber());
    }

    @Test
    public void setBirthDate_UpdatesBirthDate() {
        System.out.println("🧪 Test: Updating birth date");
        // Test: Updating birth date
        Player player = new Player();
        player.setBirthDate("25/12/2009");
        
        assertEquals("25/12/2009", player.getBirthDate());
    }

    @Test
    public void setShirtSize_UpdatesSize() {
        System.out.println("🧪 Test: Updating shirt size");
        // Test: Updating shirt size
        Player player = new Player();
        player.setShirtSize("XL");
        
        assertEquals("XL", player.getShirtSize());
    }

    @Test
    public void setTeamId_UpdatesTeamId() {
        System.out.println("🧪 Test: Updating team ID");
        // Test: Updating team ID
        Player player = new Player();
        player.setTeamId("newTeam123");
        
        assertEquals("newTeam123", player.getTeamId());
    }

    @Test
    public void emptyConstructor_CreatesEmptyPlayer() {
        System.out.println("🧪 Test: Empty constructor");
        // Test: Empty constructor (required for Firebase)
        Player player = new Player();
        
        assertNotNull("Empty constructor should create player", player);
        assertNull("Player ID should be null", player.getPlayerId());
        assertNull("User ID should be null", player.getUserId());
        assertNull("First name should be null", player.getFirstName());
    }

    @Test
    public void setFirstName_UpdatesName() {
        System.out.println("🧪 Test: Updating first name");
        // Test: Updating first name
        Player player = new Player();
        player.setFirstName("אבי");
        
        assertEquals("אבי", player.getFirstName());
    }

    @Test
    public void setLastName_UpdatesName() {
        System.out.println("🧪 Test: Updating last name");
        // Test: Updating last name
        Player player = new Player();
        player.setLastName("שמעון");
        
        assertEquals("שמעון", player.getLastName());
    }

    @Test
    public void setPlayerId_UpdatesId() {
        System.out.println("🧪 Test: Updating player ID");
        // Test: Updating player ID
        Player player = new Player();
        player.setPlayerId("newPlayer456");
        
        assertEquals("newPlayer456", player.getPlayerId());
    }

    @Test
    public void setUserId_UpdatesId() {
        System.out.println("🧪 Test: Updating user ID");
        // Test: Updating user ID
        Player player = new Player();
        player.setUserId("newUser789");
        
        assertEquals("newUser789", player.getUserId());
    }

    @Test
    public void setCreatedAt_UpdatesTimestamp() {
        System.out.println("🧪 Test: Updating creation timestamp");
        // Test: Updating creation timestamp
        Player player = new Player();
        long timestamp = 1234567890L;
        player.setCreatedAt(timestamp);
        
        assertEquals(timestamp, player.getCreatedAt());
    }

    @Test
    public void setUpdatedAt_UpdatesTimestamp() {
        System.out.println("🧪 Test: Updating update timestamp");
        // Test: Updating update timestamp
        Player player = new Player();
        long timestamp = 1234567890L;
        player.setUpdatedAt(timestamp);
        
        assertEquals(timestamp, player.getUpdatedAt());
    }

    @Test
    public void jerseyNumber_DefaultValue_IsEmptyString() {
        System.out.println("🧪 Test: Default value for jersey number");
        // Test: Default value for jersey number is empty string
        Player player = new Player();
        
        assertNotNull("Jersey number should not be null", player.getJerseyNumber());
        assertEquals("Jersey number default should be empty string", "", player.getJerseyNumber());
    }

    @Test
    public void minimalConstructor_SetsDefaultJerseyNumber() {
        System.out.println("🧪 Test: Minimal constructor - jersey number");
        // Test: Minimal constructor sets default jersey number
        Player player = new Player("player123", "user456", "Test Player", "team789", System.currentTimeMillis());
        
        assertNotNull("Jersey number should not be null", player.getJerseyNumber());
        assertEquals("Jersey number should be empty string", "", player.getJerseyNumber());
    }

    @Test
    public void fullConstructor_PreservesAllData() {
        System.out.println("🧪 Test: Full constructor - preserving all data");
        // Test: Full constructor preserves all data
        String playerId = "p123";
        String userId = "u456";
        String firstName = "רועי";
        String lastName = "כהן";
        String grade = "יב";
        String school = "תיכון אורט";
        String playerPhone = "0505555555";
        String parentPhone = "0506666666";
        String idNumber = "555555555";
        String birthDate = "10/10/2008";
        String shirtSize = "L";
        String jerseyNumber = "7";
        String teamId = "t789";
        
        Player player = new Player(
            playerId, userId, firstName, lastName, grade, school,
            playerPhone, parentPhone, idNumber, birthDate, shirtSize, jerseyNumber, teamId
        );
        
        // Verify all fields are saved correctly
        assertEquals(playerId, player.getPlayerId());
        assertEquals(userId, player.getUserId());
        assertEquals(firstName, player.getFirstName());
        assertEquals(lastName, player.getLastName());
        assertEquals(grade, player.getGrade());
        assertEquals(school, player.getSchool());
        assertEquals(playerPhone, player.getPlayerPhone());
        assertEquals(parentPhone, player.getParentPhone());
        assertEquals(idNumber, player.getIdNumber());
        assertEquals(birthDate, player.getBirthDate());
        assertEquals(shirtSize, player.getShirtSize());
        assertEquals(jerseyNumber, player.getJerseyNumber());
        assertEquals(teamId, player.getTeamId());
    }
}
