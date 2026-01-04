# יצירת משתמש אדמין ראשוני

## שיטה 1: דרך Firebase Console (מומלץ)

1. היכנס ל-[Firebase Console](https://console.firebase.google.com)
2. בחר את הפרויקט שלך
3. לך ל-**Authentication** → **Users**
4. צור משתמש חדש עם מייל וסיסמה
5. העתק את ה-**UID** של המשתמש שנוצר
6. לך ל-**Realtime Database**
7. נווט ל-`users/{UID}` (במקום {UID} הדבק את ה-UID שהעתקת)
8. שנה את השדה `role` מ-`COACH` ל-`ADMIN`

---

## שיטה 2: דרך הקוד (חד-פעמי)

הוסף את הקוד הבא ל-`LoginActivity` **רק פעם אחת** כדי ליצור אדמין:

```java
// Add this method to LoginActivity
private void createInitialAdmin() {
    UserRepository userRepository = new UserRepository();
    
    // Change these to your desired admin credentials
    String adminEmail = "admin@basketball.com";
    String adminPassword = "Admin123456";
    String adminName = "מנהל ראשי";
    String adminPhone = "050-1234567";
    
    userRepository.createAdminUser(adminEmail, adminPassword, adminName, adminPhone, 
        new UserRepository.OnRegisterListener() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(LoginActivity.this, 
                    "משתמש אדמין נוצר בהצלחה!", 
                    Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(LoginActivity.this, 
                    "שגיאה ביצירת אדמין: " + error, 
                    Toast.LENGTH_LONG).show();
            }
        });
}
```

ואז קרא לפונקציה ב-`onCreate`:
```java
// Add this line ONCE in onCreate method
// createInitialAdmin();  // Remove after first run!
```

**חשוב:** מחק את השורה הזו אחרי שהאדמין נוצר!

---

## שיטה 3: הרשמה רגילה + שינוי ידני

1. הרשם דרך האפליקציה כמשתמש רגיל
2. פתח Firebase Console → Realtime Database
3. מצא את המשתמש שלך תחת `users`
4. שנה את `role` מ-`COACH` ל-`ADMIN`
5. צא ממשתמש והתחבר מחדש

---

## נתוני אדמין לדוגמה

```
Email: admin@basketball.com
Password: Admin123456
Role: ADMIN
```

**זכור לשנות את הסיסמה אחרי ההתחברות הראשונה!**
