# 🧪 Automated Testing Guide - Basketball Training Management System

## 📋 Table of Contents

1.  [Overview](#overview)
2.  [Test Structure](#test-structure)
3.  [How to Run Tests](#how-to-run-tests)
4.  [Test Descriptions](#test-descriptions)
5.  [Requirements](#requirements)

---

## 🎯 Overview

The testing system covers all core models and functions of the application.

**📊 Statistics:**

-   **67 Unit Tests** (run fast, no emulator needed)
-   **14 Instrumented Tests** (require emulator)
-   **Total: 81 automated tests**

The testing system is divided into two main types:

### 1. **Unit Tests**

-   📁 Location: `app/src/test/java/`
-   ⚡ Run **fast** - no emulator needed
-   🎯 Purpose: Test isolated business logic
-   💻 Run on JVM (Java Virtual Machine)

### 2. **Instrumented Tests** (Integration & UI)

-   📁 Location: `app/src/androidTest/java/`
-   🐌 Run **slower** - require emulator/physical device
-   🎯 Purpose: Test UI interactions and Firebase integration
-   📱 Run on Android emulator or real device

---

## 📂 Test Structure

```
app/src/├── test/java/com/example/testapp/tests/           # 📁 Main folder - Unit tests│   ├── UserRepositoryTest.java                   # Tests for User model (8 tests)│   ├── TrainingTest.java                         # Tests for Training model (5 tests)│   ├── TeamTest.java                             # Tests for Team model (10 tests)│   ├── CourtTest.java                            # Tests for Court model (14 tests)│   ├── PlayerTest.java                           # Tests for Player model (12 tests)│   └── DayScheduleTest.java                      # Tests for DaySchedule model (18 tests)│└── androidTest/java/com/example/testapp/tests/   # 📁 Main folder - UI & Integration tests    ├── LoginFlowTest.java                        # UI tests for login screen (7 tests)    ├── TeamManagementTest.java                   # UI tests for team management (2 tests)    ├── FirebaseIntegrationTest.java              # Integration tests for Firebase (5 tests)    └── AllInstrumentedTests.java                 # Suite - aggregates all tests
```

**✅ All tests are located in a single central `tests` folder for convenience and organization**

---

## 🚀 How to Run Tests

### Method 1: Terminal (Command Line)

#### Run all Unit Tests:

```bash
./gradlew test
```

#### Run all Instrumented Tests:

```bash
./gradlew connectedAndroidTest
```

**⚠️ Note:** Requires emulator or connected device!

#### Run specific test:

```bash
# Unit tests./gradlew test --tests com.example.testapp.tests.UserRepositoryTest./gradlew test --tests com.example.testapp.tests.TrainingTest# UI/Integration tests./gradlew connectedAndroidTest --tests com.example.testapp.tests.LoginFlowTest./gradlew connectedAndroidTest --tests com.example.testapp.tests.TeamManagementTest./gradlew connectedAndroidTest --tests com.example.testapp.tests.FirebaseIntegrationTest# Run all tests from central Suite./gradlew connectedAndroidTest --tests com.example.testapp.tests.AllInstrumentedTests
```

### Method 2: Android Studio / VS Code

#### Android Studio:

1.  Open the test file
2.  Right-click on the class name
3.  Select: **Run 'TestName'**

#### VS Code:

1.  Install "Java Test Runner" extension
2.  Open the test file
3.  Click ▶️ next to class or method name

### Method 3: Gradle Task Viewer

1.  Open Gradle menu (right side in Android Studio)
2.  Navigate to: `app → Tasks → verification`
3.  Double-click on `test` or `connectedAndroidTest`

---

## 📖 Test Descriptions

### 📁 Unit Tests

#### 1️⃣ `UserRepositoryTest.java` (8 tests)

**Purpose:** Test User model and Role Management functions

Test

What it does

Why it's important

`testCreateUser_WithConstructor`

Creates user with all fields and verifies all are saved correctly

Ensure no data loss

`testUserRole_IsCoach`

Checks that coach is correctly identified as coach (isCoach=true, isPlayer=false)

Ensure proper access control

`testUserRole_IsAdmin`

Checks that admin is correctly identified

Ensure admin permissions

`testRegistrationStatus_IsPending`

Checks identification of pending registration status

Identify users awaiting approval

`testTeamIds_ReturnsEmptyListNotNull`

Verifies empty teams list returns empty list not null

Prevent NullPointerException

`testAddPendingTeam`

Tests that same team can't be added twice

Prevent duplicates

`testGetFirstPendingTeam`

Returns first team in pending list

Manage approval queue

`testGetFirstPendingTeam_Empty`

Returns null when no pending teams

Prevent crashes

**How to run:**

```bash
./gradlew test --tests UserRepositoryTest
```

---

#### 2️⃣ `TrainingTest.java` (5 tests)

**Purpose:** Test Training model and its logic

Test

What it does

Why it's important

`testCreateTraining_AllFieldsSet`

Saves and reads all training fields

Ensure no data loss

`testCalculateTrainingDuration`

Calculates training duration (18:00-20:00 = 120 min)

Correct calculations

`testDuration_OverMidnight`

Tests calculation for training crossing midnight (discovers bug!)

Identify logic issues

`testNoConflict_DifferentCourts`

Verifies trainings on different courts don't conflict

Prevent false positives

`testNoConflict_BackToBack_LogicOnly`

Tests that 2 consecutive trainings don't conflict

Allow maximum utilization

**How to run:**

```bash
./gradlew test --tests TrainingTest
```

---

#### 3️⃣ `TeamTest.java` (10 tests)

**Purpose:** Test Team model

Test

What it does

Why it's important

`testCreateTeam_AllFieldsSet`

Creates team and verifies all fields are saved

data integrity

`testTeamInitialization_DefaultValues`

Tests default values (0 players, timestamps)

consistency

`testSetNumberOfPlayers`

Updates player count

List management

`testToString_Format`

Tests correct display format: "Name (Year)"

User display

`testSettersUpdate_AllFields`

Verifies all setters work

Full CRUD

`testColor_HexFormat`

Tests color in correct hex format (#RRGGBB)

Color display

`testEmptyConstructor_FirebaseCompatibility`

Tests empty constructor for Firebase

Firebase compatibility

`testUpdateTeamData_ReflectsChanges`

Updates team data → verify change

Update logic

`testCoachChange_UpdatesCoachInfo`

Change coach → update ID and name

Coach management

`testTeamWithNullCoach`

Team without coach (null)

Handle edge case

**How to run:**

```bash
./gradlew test --tests TeamTest
```

---

#### 4️⃣ `CourtTest.java` (14 tests)

**Purpose:** Test Court model and calendar logic

Test

What it does

Why it's important

`testCreateCourt_AllFieldsSet`

Creates court with all details

Data integrity

`testCourtInitialization_DefaultSchedule`

Tests that all 7 days initialize with defaults

Consistency

`testGetScheduleForDay_AllDays`

Returns correct schedule for each day of week

Availability management

`testSetScheduleForDay_UpdatesCorrectDay`

Updates specific day

Daily editing

`testIsActiveOnDay_ActiveDay`

Identifies active day

Availability detection

`testIsActiveOnDay_InactiveDay`

Identifies closed day

Unavailability detection

`testWeeklySchedule_GetAndSet`

Read/write weekly schedule as Map

Firebase integration

`testSetWeeklySchedule_FromMap`

Populates schedule from Map

Deserialize from Firebase

`testCourtAvailability_Toggle`

Changes availability status (active/inactive)

Court management

`testCourtWithCustomActiveDays`

Creates court with custom active days

Flexibility

`testCourtSetters_UpdateAllFields`

Tests all setters

Full CRUD

`testCourtToString_ReturnsName`

Returns court name

Display

`testScheduleForInvalidDay_ReturnsDefault`

Invalid day (0, 8) → default value

Error handling

`testCourtNullSchedule_HandledGracefully`

Null schedule → default value

Robustness

**How to run:**

```bash
./gradlew test --tests CourtTest
```

---

#### 5️⃣ `PlayerTest.java` (12 tests)

**Purpose:** Test Player model

Test

What it does

Why it's important

`testCreatePlayer_FullConstructor`

Creates player with all details

Data completeness

`testCreatePlayer_MinimalConstructor`

Creates minimal player (registration approval)

Registration process

`testGetFullName`

Returns full name (first + last)

Display

`testNameSplit_MinimalConstructor_SingleName`

Splits single name → firstName, lastName=""

Edge case

`testNameSplit_MinimalConstructor_FullName`

Splits "first last" → 2 parts

Correct parsing

`testNameSplit_MinimalConstructor_MultipleSpaces`

Splits "first last middle" → firstName + rest

Complex names

`testJerseyNumber_DefaultEmptyString`

Tests jersey number starts as "" not null

Null safety

`testSettersUpdate_AllFields`

Tests all setters

CRUD

`testEmptyConstructor_FirebaseCompatibility`

Empty constructor for Firebase

Firebase integration

`testUpdatePlayerInfo_ReflectsChanges`

Update player details → verify change

Update logic

`testPlayerWithNullOptionalFields`

Optional fields null

Robustness

`testPlayerTeamChange`

Transfer player to another team

Team management

**How to run:**

```bash
./gradlew test --tests PlayerTest
```

---

#### 6️⃣ `DayScheduleTest.java` (18 tests)

**Purpose:** Test DaySchedule model (daily schedule)

Test

What it does

Why it's important

`testCreateDaySchedule_Active`

Creates active day

Basic creation

`testCreateDaySchedule_Inactive`

Creates closed day

Rest days

`testDefaultConstructor`

Tests default values (inactive, 08:00-22:00)

Consistency

`testToString_Active`

Display: "08:00 - 22:00"

UI display

`testToString_Inactive`

Display: "Closed"

UI display

`testSetActive_ToggleState`

Toggle active/closed state

Availability management

`testSetOpeningHour`

Update opening hour

Editing

`testSetClosingHour`

Update closing hour

Editing

`testUpdateScheduleHours`

Extend active hours

Flexibility

`testScheduleDifferentTimeFormats`

Different time formats

Compatibility

`testClosedDay_ToString`

Closed day always shows "Closed"

Consistency

`testFullDaySchedule_24Hours`

24-hour schedule

Edge case

`testMidnightCrossing_NotSupported`

(Note: not supported but preserved)

Documentation

`testSetAllFields`

Tests all setters

CRUD

`testTypicalWorkdaySchedule`

Typical workday

Real-world

`testTypicalWeekendSchedule`

Typical weekend

Real-world

`testInactiveDayRetainHours`

Closed day keeps hours

Data preservation

`testReactivateDay_KeepsHours`

Reactivation keeps hours

UX

**How to run:**

```bash
./gradlew test --tests DayScheduleTest
```

---

### 📱 Instrumented Tests (UI & Integration)

#### 7️⃣ `LoginFlowTest.java` (7 tests)

**Purpose:** Test login process

Test

What it does

Why it's important

`loginScreen_DisplaysAllElements`

Tests all elements displayed (email, password, buttons)

Ensure UI is correct

`loginWithEmptyFields_ShowsError`

Tries login without details → should show error

Prevent empty login

`loginWithInvalidEmail_ShowsError`

Enter invalid email → error

Correct validation

`clickRegisterButton_NavigatesToRegisterActivity`

Click "Register" → correct navigation

Ensure navigation

`clickForgotPassword_ShowsDialog`

Click "Forgot password" → dialog opens

Password recovery flow

`successfulLogin_NavigatesToMainActivity`

Successful login → go to main page

Complete process

`loginInProgress_ShowsProgressBar`

During login → progress bar displayed

User feedback

**How to run:**

```bash
./gradlew connectedAndroidTest --tests LoginFlowTest
```

**⚠️ Requirements:**

-   Emulator or connected device
-   Test user in Firebase: `test@example.com` / `password123`

---

#### 8️⃣ `TeamManagementTest.java` (2 tests)

**Purpose:** Test team management

Test

What it does

Why it's important

`teamsScreen_DisplaysRecyclerView`

RecyclerView displayed

UI works

`clickAddTeamButton_ShowsDialog`

Click + → dialog opens

Add process

`createTeam_WithAllDetails_Success`

Fill all fields → team created

Complete process

`createTeam_WithoutName_ShowsError`

Try create without name → error

Validation

`clickTeam_OpensTeamPlayersScreen`

Click team → players screen

Navigation

`editTeam_ShowsCurrentDetails`

Edit → existing details shown

Correct UX

`deleteTeam_WithConfirmation_Removes`

Delete with confirmation → team removed

Deletion process

`coachUser_SeesOnlyTheirTeams`

Coach sees only their teams

Permissions

**How to run:**

```bash
./gradlew connectedAndroidTest --tests TeamManagementTest
```

**⚠️ Requirements:**

-   COORDINATOR user in Firebase for create/edit/delete tests
-   COACH user for filtering test

---

#### 9️⃣ `FirebaseIntegrationTest.java` (5 tests)

**Purpose:** Test Firebase integration

Test

What it does

Why it's important

`writeAndReadTraining_Success`

Saves training to Firebase and reads back

Basic CRUD

`deleteTraining_Success`

Deletes training → verify deletion

Delete operation

`updateTraining_Success`

Updates field in training → verify update

Update operation

`queryTrainingsByTeam_ReturnsCorrectResults`

Query by teamId → get only relevant ones

Correct queries

`valueEventListener_TriggersOnDataChange`

Listener fires when data changes

Real-time updates

**How to run:**

```bash
./gradlew connectedAndroidTest --tests FirebaseIntegrationTest
```

**⚠️ Requirements:**

-   Internet connection
-   Firebase Test Environment (or test data in production)
-   Tests clean up after themselves (under `test_data/`)

---

## 🔧 Requirements

### Software:

-   ✅ Android Studio / VS Code with Java Extension
-   ✅ JDK 11+ (Java 17 recommended)
-   ✅ Android SDK
-   ✅ Gradle 8.13+

### For Running Instrumented Tests:

-   ✅ Android Emulator (API 24+) or physical device
-   ✅ USB Debugging enabled (for physical device)
-   ✅ Internet connection (for Firebase tests)

### Firebase Test Users:

To run Login and Team Management tests, create these users in Firebase:

```
test@example.com / password123      (COORDINATOR)coach@example.com / password123     (COACH)player@example.com / password123    (PLAYER)
```

---

## 📊 Running All Tests Together

### Unit Tests Only (fast):

```bash
./gradlew test
```

**📊 67 tests**  
**Estimated time:** 10-20 seconds

### Instrumented Tests Only (slow):

```bash
./gradlew connectedAndroidTest
```

**📊 14 tests**  
**Estimated time:** 3-5 minutes

### **Everything Together:**

```bash
./gradlew test connectedAndroidTest
```

**📊 81 total tests**  
**Estimated time:** 4-6 minutes

---

## 📈 Test Reports

After running tests, HTML reports are generated at:

```
app/build/reports/tests/testDebugUnitTest/index.html        (Unit Tests)app/build/reports/androidTests/connected/index.html         (Instrumented Tests)
```

Open these files in a browser to see:

-   ✅ How many tests passed
-   ❌ How many failed
-   ⏱️ Execution time
-   📝 Details for each test

---

## 🆘 Common Troubleshooting

### ❌ "No connected devices"

**Solution:** Start an emulator or connect a physical device with USB Debugging enabled

### ❌ "Firebase Authentication failed"

**Solution:** Ensure internet connection and that test users exist

### ❌ "Task 'test' not found"

**Solution:** Run from the project root directory:

```bash
cd "c:UsersshaleDesktopBasketball Training Management System - Android App".gradlew.bat test
```

### ❌ "Could not find or load main class"

**Solution:** Clean the cache:

```bash
.gradlew.bat clean.gradlew.bat test
```

---

## 📚 Additional Resources

-   [JUnit Documentation](https://junit.org/junit4/)
-   [Espresso Testing Docs](https://developer.android.com/training/testing/espresso)
-   [Firebase Test Lab](https://firebase.google.com/docs/test-lab)

---

**🎉 Good luck with testing!**