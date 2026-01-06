# מערכת ניהול מתחם אימונים - Basketball Training Management System

## 🏀 סקירת הפרויקט

מערכת ממוחשבת לניהול מתחם אימונים למחלקת נוער באגודת כדורסל.
האפליקציה מאפשרת ניהול יעיל של לוחות זמנים, מגרשים וקבוצות בצורה ויזואלית ואינטואיטיבית.

**🎯 גרסה:** 2.0 - Single Activity Architecture  
**📅 עדכון אחרון:** ינואר 2026

---

## ✨ תכונות עיקריות

### 1. ניהול משתמשים
- 🔐 התחברות והרשמה עם Firebase Authentication
- 👥 4 רמות הרשאות: שחקן, מאמן, רכז, מנהל
- 📝 ניהול פרופיל משתמש ופרטי שחקן
- 🛡️ בקרת גישה מבוססת תפקידים

### 2. ניהול קבוצות
- ➕ הוספה, עריכה ומחיקה של קבוצות
- 👨‍🏫 שיוך מאמנים לקבוצות
- 🎯 סיווג לפי שנת לידה ורמה
- 🎨 צביעה ייחודית לכל קבוצה
- 👦 ניהול שחקנים בקבוצה

### 3. ניהול מגרשים
- 🏟️ הגדרת מגרשים במתחם
- ⏰ קביעת שעות פעילות
- 📍 ניהול מיקומים וזמינות

### 4. ניהול לוחות זמנים
- 📅 יצירת ועריכת אימונים
- ⚠️ מניעת התנגשויות אוטומטית
- 🔍 צפייה לפי קבוצה/מגרש/יום
- 📊 תצוגה אינפוגרפית של כל המגרשים
- 📋 שכפול אימונים

### 5. תצוגת מתחם (All Courts View)
- 🗺️ מסך מרכזי המציג את כל המגרשים בו-זמנית
- 🌈 בלוקים צבעוניים לפי קבוצה
- 👆 לחיצה על אימון להצגת פרטים מלאים

---

## 🏗️ ארכיטקטורה

### Single Activity Architecture
האפליקציה מבוססת על **ארכיטקטורת Single Activity** עם **Navigation Component**:

- **MainActivity** - Activity יחיד שמכיל NavHostFragment
- **Fragments** - כל מסך הוא Fragment נפרד
- **Navigation Graph** - ניהול ניווט מרכזי

### MVVM Pattern
הפרויקט בנוי לפי עקרון **MVVM (Model-View-ViewModel)**:

```
📁 com.example.testapp/
├── 📁 models/           # Data models
│   ├── User.java
│   ├── Team.java
│   ├── Court.java
│   ├── Training.java
│   └── Player.java
│
├── 📁 fragments/        # UI Fragments (NEW!)
│   ├── HomeFragment.java
│   ├── TeamsFragment.java
│   ├── CourtsFragment.java
│   ├── ScheduleFragment.java
│   ├── AllCourtsViewFragment.java
│   ├── ManageUsersFragment.java
│   └── PlayerDetailsFragment.java
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

## 🧪 בדיקות (Testing)

### בדיקות אוטומטיות
האפליקציה כוללת בדיקות Espresso UI:

```bash
# הרצת כל הבדיקות
./gradlew connectedAndroidTest

# הרצת בדיקה ספציפית
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.testapp.NavigationTests
```

**קבצי בדיקה:**
- `NavigationTests.java` - בדיקות ניווט
- `TeamsFragmentTests.java` - בדיקות קבוצות
- `CourtsFragmentTests.java` - בדיקות מגרשים
- `ScheduleFragmentTests.java` - בדיקות אימונים

### בדיקות ידניות
ראה את [TESTING_GUIDE.md](TESTING_GUIDE.md) למדריך מלא להרצת בדיקות

### תוכנית בדיקות
ראה את [TEST_PLAN.md](TEST_PLAN.md) ל-40+ תסריטי בדיקה מפורטים

---

## 📋 שימוש באפליקציה

### התחברות ראשונית
1. הרשם עם אימייל וסיסמה
2. בחר תפקיד (PLAYER/COACH/COORDINATOR/ADMIN)
3. התחבר למערכת

### ניהול קבוצות (רכז/מנהל)
1. לחץ על כרטיס "קבוצות" במסך הראשי
2. לחץ על כפתור "הוסף קבוצה"
3. מלא פרטים: שם, שנת לידה, ימי אימון
4. שייך מאמן לקבוצה
5. הקבוצה תקבל צבע ייחודי אוטומטית

### ניהול מגרשים (רכז/מנהל)
1. לחץ על כרטיס "מגרשים"
2. לחץ על "הוסף מגרש"
3. הגדר שם, מיקום, שעות פעילות
4. המגרש מוכן לשימוש

### יצירת אימון (מאמן/רכז/מנהל)
1. לחץ על כרטיס "לוח אימונים"
2. לחץ על "הוסף אימון"
3. בחר קבוצה, מגרש, יום ושעות
4. המערכת תבדוק התנגשויות אוטומטית
5. שכפל אימונים עם כפתור "שכפל"

### תצוגת מתחם
1. לחץ על כרטיס "תצוגת מגרשים"
2. ראה את כל המגרשים והאימונים בתצוגה אחת
3. לחץ על בלוק אימון לפרטים נוספים

### ניהול משתמשים (רכז/מנהל)
1. לחץ על כרטיס "ניהול משתמשים"
2. סנן משתמשים לפי תפקיד או קבוצה
3. שנה הרשאות משתמש עם כפתור "שנה הרשאות"
4. מחק משתמשים (בזהירות!)

### עריכת פרטי שחקן (שחקן)
1. לחץ על כרטיס "הפרטים שלי"
2. מלא פרטים אישיים: שם, כיתה, בית ספר, טלפונים
3. בחר תאריך לידה ומידת גופיה
4. לחץ "שמור פרטים"

---

## 🔮 תכונות עתידיות (Roadmap)

- [ ] ניהול נוכחות שחקנים
- [ ] התראות Push למאמנים ושחקנים
- [ ] ייצוא ללוח שנה (Google Calendar)
- [ ] דוחות וסטטיסטיקות (אחוזי נוכחות, ניצול מגרשים)
- [ ] ניהול תחרויות ומשחקים
- [ ] גרירת אימונים (Drag & Drop) בתצוגת מתחם
- [ ] סינון וחיפוש מתקדם
- [ ] מצב Offline עם סנכרון
- [ ] תמיכה במספר מתחמים
- [ ] אפליקציית Web מקבילה

---

## 🐛 בעיות נפוצות ופתרונות

### Firebase Connection Issues
אם יש בעיות התחברות ל-Firebase:
1. ודא ש-`google-services.json` נמצא ב-`app/`
2. בדוק שה-package name תואם (com.example.testapp)
3. Sync Gradle מחדש: File > Sync Project with Gradle Files
4. בדוק חיבור לאינטרנט

### Build Errors
- נקה ובנה מחדש: Build > Clean Project > Rebuild Project
- Invalidate Caches: File > Invalidate Caches / Restart
- מחק את תיקיית `build/` ו-`.gradle/`

### Navigation Issues
אם הניווט לא עובד:
1. בדוק ש-`nav_graph.xml` קיים ב-`res/navigation/`
2. ודא ש-`MainActivity` מגדיר את ה-NavController נכון
3. נקה והרץ מחדש את האפליקציה

### Permission Errors
אם משתמש לא רואה כרטיסים:
1. בדוק את תפקיד המשתמש ב-Firebase
2. התנתק והתחבר מחדש
3. בדוק שה-HomeFragment טוען את נתוני המשתמש

---

## 👨‍💻 מחבר

**שלו עוזרי**  
פרויקט גמר - מערכות מידע  
שנת לימודים: 2025

📧 Email: [your-email@example.com]  
🔗 LinkedIn: [Your LinkedIn Profile]  
💻 GitHub: [Your GitHub]

---

## 📄 רישיון

פרויקט זה נוצר למטרות לימודיות.  
© 2025 שלו עוזרי. All rights reserved.

---

## 📚 קבצי תיעוד נוספים

- [TESTING_GUIDE.md](TESTING_GUIDE.md) - מדריך מלא להרצת בדיקות
- [TEST_PLAN.md](TEST_PLAN.md) - 40+ תסריטי בדיקה מפורטים
- [CREATE_ADMIN.md](CREATE_ADMIN.md) - הוראות ליצירת מנהל ראשי

---

## 🙏 תודות

תודה מיוחדת ל:
- Firebase עבור פלטפורמת Backend מעולה
- Material Design עבור UI Components
- Android Jetpack עבור Navigation Component
- הקהילה של Stack Overflow

---

## 📝 הערות למפתחים

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
