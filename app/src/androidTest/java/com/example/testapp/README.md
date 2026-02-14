# Android Instrumentation Tests

This folder contains **UI and Integration tests** that run on an emulator or physical device.

## Folder Structure

### 📁 crud/
**CRUD (Create, Read, Update, Delete)** tests for all entities:
- `CourtCRUDTest.java` - Complete tests for courts
- `PlayerCRUDTest.java` - Complete tests for players  
- `TeamCRUDTest.java` - Complete tests for teams
- `TrainingCRUDTest.java` - Complete tests for training sessions

**Total:** 4 test files

### 📁 flow/
**End-to-End Flow** tests of business processes:
- `EndToEndFlowTest.java` - Complete flow from login to complex operations
- `LoginFlowTest.java` - Login and user registration process
- `PlayerFullFlowTest.java` - Complete player lifecycle (registration→update→join team→deletion)
- `TeamManagementTest.java` - Team management including adding/removing players

**Total:** 4 test files

### 📁 integration/
**Integration** tests with external services:
- `CleanupTestDataTest.java` - Clean up test data from Firebase
- `FirebaseIntegrationTest.java` - Integration with Firebase Realtime Database
- `PlayerDetailsTest.java` - Player details tests including Firebase changes

**Total:** 3 test files

### 📁 utils/
**Utilities** shared by all tests:
- `RecyclerViewMatchers.java` - Custom Espresso matchers for RecyclerView

---

## Running Tests

### Run all tests:
```bash
.\gradlew connectedAndroidTest
```

### Run specific category:
```bash
# CRUD tests only
.\gradlew connectedAndroidTest --tests "com.example.testapp.crud.*"

# Flow tests only
.\gradlew connectedAndroidTest --tests "com.example.testapp.flow.*"

# Integration tests only
.\gradlew connectedAndroidTest --tests "com.example.testapp.integration.*"
```

### Run single test:
```bash
.\gradlew connectedAndroidTest --tests "com.example.testapp.crud.CourtCRUDTest"
```

---

## Requirements

- ✅ Android emulator or device connected
- ✅ Firebase Test Project with clean data
- ✅ Stable internet connection

---

**Total test files:** 11 (4 CRUD + 4 Flow + 3 Integration)  
**Estimated runtime:** ~5-10 minutes (depends on device speed)
