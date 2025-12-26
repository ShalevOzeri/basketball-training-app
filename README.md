# מערכת ניהול מתחם אימונים - Basketball Training Management System

## סקירת הפרויקט

מערכת ממוחשבת לניהול מתחם אימונים למחלקת נוער באגודת כדורסל.
האפליקציה מאפשרת ניהול יעיל של לוחות זמנים, מגרשים וקבוצות בצורה ויזואלית ואינטואיטיבית.

## תכונות עיקריות

### 1. ניהול משתמשים
- התחברות והרשמה עם Firebase Authentication
- 3 רמות הרשאות: מנהל, רכז, מאמן
- ניהול פרופיל משתמש

### 2. ניהול קבוצות
- הוספה, עריכה ומחיקה של קבוצות
- שיוך מאמנים לקבוצות
- סיווג לפי גיל ורמה
- צביעה ייחודית לכל קבוצה

### 3. ניהול מגרשים
- הגדרת מגרשים במתחם
- קביעת שעות פעילות
- ניהול זמינות

### 4. ניהול לוחות זמנים
- יצירת ועריכת אימונים
- מניעת התנגשויות אוטומטית
- צפייה לפי קבוצה/מגרש/יום
- תצוגה אינפוגרפית של כל המגרשים

### 5. תצוגת מתחם (All Courts View)
- מסך מרכזי המציג את כל המגרשים בו-זמנית
- בלוקים צבעוניים לפי קבוצה
- לחיצה על אימון להצגת פרטים

## ארכיטקטורה

הפרויקט בנוי לפי עקרון **MVVM (Model-View-ViewModel)**:

```
📁 com.example.testapp/
├── 📁 models/           # Data models
│   ├── User.java
│   ├── Team.java
│   ├── Court.java
│   └── Training.java
│
├── 📁 repository/       # Data layer
│   ├── UserRepository.java
│   ├── TeamRepository.java
│   ├── CourtRepository.java
│   └── TrainingRepository.java
│
├── 📁 viewmodel/        # Business logic
│   ├── TeamViewModel.java
│   ├── CourtViewModel.java
│   └── TrainingViewModel.java
│
├── 📁 adapters/         # RecyclerView adapters
│   ├── TeamAdapter.java
│   ├── CourtAdapter.java
│   └── TrainingAdapter.java
│
├── 📁 utils/            # Helper classes
│   ├── DateUtils.java
│   ├── ColorUtils.java
│   ├── Constants.java
│   └── TimeSlotUtils.java
│
└── 📁 Activities        # UI screens
    ├── LoginActivity.java
    ├── RegisterActivity.java
    ├── MainActivity.java
    ├── TeamsActivity.java
    ├── CourtsActivity.java
    ├── ScheduleActivity.java
    └── AllCourtsViewActivity.java
```

## טכנולוגיות

- **Language**: Java
- **IDE**: Android Studio
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34
- **Database**: Firebase Realtime Database
- **Authentication**: Firebase Auth
- **UI Components**: 
  - Material Design 3
  - RecyclerView
  - CardView
  - ConstraintLayout

## Dependencies

```gradle
// Firebase
implementation platform('com.google.firebase:firebase-bom:32.7.0')
implementation 'com.google.firebase:firebase-database'
implementation 'com.google.firebase:firebase-auth'

// AndroidX
implementation 'androidx.lifecycle:lifecycle-viewmodel:2.6.2'
implementation 'androidx.lifecycle:lifecycle-livedata:2.6.2'
implementation 'androidx.recyclerview:recyclerview:1.3.2'

// Material Design
implementation 'com.google.android.material:material:1.11.0'
```

## התקנה והרצה

### 1. דרישות מקדימות
- Android Studio Arctic Fox (2020.3.1) או גרסה חדשה יותר
- JDK 8 או גרסה חדשה יותר
- חשבון Firebase (ראה הגדרות Firebase למטה)

### 2. שלבי התקנה

1. שכפל את הפרויקט
2. פתח ב-Android Studio
3. הגדר את Firebase (ראה למטה)
4. Sync Gradle
5. הרץ על אמולטור או מכשיר

### 3. הגדרת Firebase

**חשוב מאוד**: הקובץ `google-services.json` הנוכחי הוא קובץ דמה להדגמה בלבד!

לשימוש בפועל:

1. צור פרויקט ב-[Firebase Console](https://console.firebase.google.com)
2. הוסף אפליקציית Android עם package name: `com.example.testapp`
3. הורד את קובץ `google-services.json` האמיתי
4. החלף את הקובץ הקיים ב-`app/google-services.json`
5. אפשר את Firebase Authentication (Email/Password)
6. אפשר את Firebase Realtime Database
7. הגדר Database Rules:

```json
{
  "rules": {
    ".read": "auth != null",
    ".write": "auth != null"
  }
}
```

## מבנה בסיס הנתונים

```
basketball-training-mgmt/
├── users/
│   └── {userId}/
│       ├── name
│       ├── email
│       ├── role
│       └── phone
│
├── teams/
│   └── {teamId}/
│       ├── name
│       ├── ageGroup
│       ├── level
│       ├── coachId
│       └── color
│
├── courts/
│   └── {courtId}/
│       ├── name
│       ├── location
│       ├── openingHour
│       └── closingHour
│
└── trainings/
    └── {trainingId}/
        ├── teamId
        ├── courtId
        ├── dayOfWeek
        ├── startTime
        └── endTime
```

## שימוש באפליקציה

### התחברות ראשונית
1. הרשם עם אימייל וסיסמה
2. בחר תפקיד (COACH/COORDINATOR/ADMIN)
3. התחבר למערכת

### ניהול קבוצות
1. לחץ על "ניהול קבוצות" במסך הראשי
2. לחץ על כפתור ה-FAB (+) להוספת קבוצה
3. מלא פרטים: שם, גיל, רמה, מאמן
4. הקבוצה תקבל צבע ייחודי אוטומטית

### ניהול מגרשים
1. לחץ על "ניהול מגרשים"
2. הוסף מגרשים עם שעות פעילות
3. הגדר זמינות

### יצירת אימון
1. לחץ על "לוח אימונים"
2. לחץ על כפתור ה-FAB
3. בחר קבוצה, מגרש, יום ושעות
4. המערכת תבדוק התנגשויות אוטומטית

### תצוגת מתחם
1. לחץ על "תצוגת כל המגרשים"
2. ראה את כל המגרשים והאימונים בתצוגה אחת
3. לחץ על אימון לפרטים נוספים

## תכונות עתידיות (לפיתוח)

- [ ] ניהול שחקנים ונוכחות
- [ ] התראות Push למאמנים
- [ ] ייצוא ללוח שנה (Google Calendar)
- [ ] דוחות וסטטיסטיקות
- [ ] ניהול תחרויות ומשחקים
- [ ] גרירת אימונים (Drag & Drop) בתצוגת מתחם
- [ ] סינון וחיפוש מתקדם
- [ ] מצב Offline

## בעיות נפוצות ופתרונות

### Firebase Connection Issues
אם יש בעיות התחברות ל-Firebase:
1. ודא ש-`google-services.json` נמצא במקום הנכון
2. בדוק שה-package name תואם
3. Sync Gradle מחדש

### Build Errors
- נקה ובנה מחדש: Build > Clean Project > Rebuild Project
- Invalidate Caches: File > Invalidate Caches / Restart

## מחבר

**שלו עוזרי**  
פרויקט גמר - מערכות מידע  
שנת לימודים: 2025

## רישיון

פרויקט זה נוצר למטרות לימודיות.

---

## הערות למפתחים

### הרחבת הפרויקט

#### הוספת Activity חדש:
1. צור class חדש שיורש מ-AppCompatActivity
2. הוסף layout XML מתאים
3. הוסף את ה-Activity ל-AndroidManifest.xml
4. צור intent במקום הנדרש

#### הוספת Model חדש:
1. צור class ב-package models
2. הוסף constructor ריק ל-Firebase
3. צור Repository מתאים
4. צור ViewModel מתאים
5. עדכן את מבנה ה-Database

#### שיפורים מומלצים:
- הוסף Validation מתקדם לטפסים
- שפר את ה-Error Handling
- הוסף Loading States
- צור Dialogs מותאמים אישית
- הוסף Animations
- שפר את הנגישות (Accessibility)

---

## תמיכה

לשאלות או בעיות, צור Issue בגיטהאב או פנה למייל.
