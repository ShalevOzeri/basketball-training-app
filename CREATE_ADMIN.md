# Creating Initial Admin User

## Method 1: Through Firebase Console (Recommended)

1. Log in to [Firebase Console](https://console.firebase.google.com)
2. Select your project
3. Go to **Authentication** → **Users**
4. Create a new user with email and password
5. Copy the **UID** of the created user
6. Go to **Realtime Database**
7. Navigate to `users/{UID}` (replace {UID} with the UID you copied)
8. Change the `role` field from `COACH` to `ADMIN`

---

## Method 2: Through Code (One-Time)

Add the following code to `LoginActivity` **only once** to create an admin:

```java
// Add this method to LoginActivity
private void createInitialAdmin() {
    UserRepository userRepository = new UserRepository();
    
    // Change these to your desired admin credentials
    String adminEmail = "admin@basketball.com";
    String adminPassword = "Admin123456";
    String adminName = "Main Administrator";
    String adminPhone = "050-1234567";
    
    userRepository.createAdminUser(adminEmail, adminPassword, adminName, adminPhone, 
        new UserRepository.OnRegisterListener() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(LoginActivity.this, 
                    "Admin user created successfully!", 
                    Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(LoginActivity.this, 
                    "Error creating admin: " + error, 
                    Toast.LENGTH_LONG).show();
            }
        });
}
```

Then call the function in `onCreate`:
```java
// Add this line ONCE in onCreate method
// createInitialAdmin();  // Remove after first run!
```

**Important:** Delete this line after the admin is created!

---

## Method 3: Regular Registration + Manual Change

1. Register through the app as a regular user
2. Open Firebase Console → Realtime Database
3. Find your user under `users`
4. Change `role` from `COACH` to `ADMIN`
5. Log out and log back in

---

## Sample Admin Credentials

```
Email: admin@basketball.com
Password: Admin123456
Role: ADMIN
```

**Remember to change the password after the first login!**

---

## Spark Plan (Free) Limitations

⚠️ **The project currently runs on Firebase's free Spark plan.**

**What doesn't work in the free version:**

1. ❌ **Deleting users through the app**
   - Requires Cloud Functions (available only in Blaze plan)
   - **Solution:** Manual deletion through Firebase Console

2. ❌ **Phone Authentication (SMS)**
   - Requires Blaze plan
   - **Solution:** Currently using only Email/Password

**For manual user deletion:**
1. [Firebase Console](https://console.firebase.google.com)
2. Authentication → Delete user
3. Realtime Database → `/users/{uid}` → Delete
4. If player: `/players/{playerId}` → Delete

**To upgrade to Blaze plan (optional):**
- Link: https://console.firebase.google.com/project/basketball-training-management/usage/details
- Free quota: 2 million calls per month
- Cost: $0 until you exceed the free quota
