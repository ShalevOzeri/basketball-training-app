# בדיקת שמירת מספר גופיה - Debug Guide

## מה לבדוק ב-Firebase Console:

### שלב 1: בדוק אם השחקן נשמר בכלל
1. פתח Firebase Console: https://console.firebase.google.com
2. בחר את הפרויקט שלך
3. לך ל-Realtime Database
4. חפש את `players/{playerId}`
5. ✅ בדוק שהשחקן קיים

### שלב 2: בדוק אילו שדות קיימים
ב-Firebase, תראה משהו כזה:
```
players
  └─ {playerId}
      ├─ playerId: "..."
      ├─ userId: "..."
      ├─ firstName: "..."
      ├─ lastName: "..."
      ├─ grade: "..."
      ├─ school: "..."
      ├─ playerPhone: "..."
      ├─ parentPhone: "..."
      ├─ idNumber: "..."
      ├─ birthDate: "..."
      ├─ shirtSize: "..."
      ├─ jerseyNumber: ""    ← האם השדה הזה קיים?
      ├─ teamId: "..."
      ├─ createdAt: 123456789
      └─ updatedAt: 123456789
```

### שלב 3: מה השדה jerseyNumber מציג?
- ✅ **אם יש `jerseyNumber: ""`** - מצוין! השדה נשמר כמחרוזת ריקה
- ✅ **אם יש `jerseyNumber: "23"`** - מצוין! השדה נשמר עם ערך
- ❌ **אם אין `jerseyNumber` בכלל** - זו הבעיה שצריך לתקן

## אם השדה לא מופיע בכלל:

הסיבות האפשריות:
1. ❌ Firebase לא מסדרר (serialize) את השדה כי אין getter
2. ❌ השדה הוא null והגדרות Firebase לא שומרות null
3. ❌ יש exception בזמן השמירה שלא מודפס

## פתרונות אפשריים:

### פתרון 1: וידוא ידני שהשדה נשמר
במקום להסתמך על אתחול אוטומטי, נשמור את השדה ידנית:

```java
// ב-PlayerDetailsFragment, אחרי יצירת Player חדש:
Map<String, Object> playerData = new HashMap<>();
playerData.put("playerId", playerId);
playerData.put("userId", userId);
playerData.put("firstName", firstName);
playerData.put("lastName", lastName);
playerData.put("grade", grade);
playerData.put("school", school);
playerData.put("playerPhone", playerPhone);
playerData.put("parentPhone", parentPhone);
playerData.put("idNumber", idNumber);
playerData.put("birthDate", birthDate);
playerData.put("shirtSize", shirtSize);
playerData.put("jerseyNumber", jerseyNumber != null ? jerseyNumber : "");
playerData.put("teamId", teamId);
playerData.put("createdAt", System.currentTimeMillis());
playerData.put("updatedAt", System.currentTimeMillis());

playersRef.child(newPlayerId).setValue(playerData);
```

### פתרון 2: אכוף שדות חובה ב-Firebase Rules
ב-Firebase Realtime Database Rules:
```json
{
  "rules": {
    "players": {
      "$playerId": {
        ".validate": "newData.hasChildren(['playerId', 'userId', 'firstName', 'lastName', 'jerseyNumber'])"
      }
    }
  }
}
```

### פתרון 3: הוסף logging לראות מה בדיוק נשמר
```java
player.setJerseyNumber(jerseyNumber);
Log.d("PlayerDetails", "Saving player with jerseyNumber: [" + player.getJerseyNumber() + "]");
playersRef.child(newPlayerId).setValue(player);
```

## בדיקה מהירה:
הרץ את קוד הבדיקה הזה ב-Android Studio Logcat לראות מה בדיוק נשמר.
