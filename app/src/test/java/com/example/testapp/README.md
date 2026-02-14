# Unit Tests

This directory contains **Unit Tests** that run on JVM without Android dependencies.

## Folder Structure

### 📁 models/
**Data model tests** (POJO classes):
- `CourtTest.java` - XML mapping and court data structure tests
- `DayScheduleTest.java` - Daily schedule logic tests
- `PlayerTest.java` - Constructors, name splitting, getters/setters tests
- `TeamTest.java` - Team model tests
- `TrainingTest.java` - Training model tests
- `UserTest.java` - Roles, registration status, team management tests

**Total:** 6 test files | **128 tests**

### 📁 repository/
**Data access layer tests**:
- `UserRepositoryTest.java` - User repository CRUD logic tests

**Total:** 1 test file

### 📁 utils/
**Utility functions tests**:
- `ColorUtilsTest.java` - Color format, indices, uniqueness tests (12 tests)
- `DateUtilsTest.java` - Dates, times, Israel timezone tests (17 tests)
- `TimeSlotUtilsTest.java` - Time slot creation and range validation tests (23 tests)

**Total:** 3 test files | **52 tests**

---

## Running Tests

### Run all tests:
```bash
.\gradlew test
```

### Run specific category:
```bash
# Models tests only
.\gradlew test --tests "com.example.testapp.models.*"

# Utils tests only
.\gradlew test --tests "com.example.testapp.utils.*"

# Repository tests only
.\gradlew test --tests "com.example.testapp.repository.*"
```

### Run single test:
```bash
.\gradlew test --tests "com.example.testapp.models.PlayerTest"
```

### Run single method:
```bash
.\gradlew test --tests "com.example.testapp.models.PlayerTest.testPlayer_FullConstructor_AllFieldsSet"
```

---

## Requirements

- ✅ JDK 8 or higher
- ✅ **No Android dependencies** - runs on regular JVM
- ✅ **High performance** - all tests run in seconds

---

## Important Notes

⚠️ **These tests do not use the Android framework**  
If a class uses `android.graphics.Color` or `Context`, move the test to `androidTest/` or use Robolectric.

✅ **Suitable for testing:**
- Business logic
- Mathematical calculations
- String parsing and processing
- Validations
- Data structures

❌ **Not suitable for testing:**
- UI components
- Activities/Fragments
- Firebase/Network
- Android-specific APIs

---

**Total test files:** 10  
**Total tests:** 179+ tests  
**Execution time:** < 5 seconds
