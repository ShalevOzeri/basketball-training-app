package com.example.testapp.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.testapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private final FirebaseAuth auth;
    private final DatabaseReference usersRef;
    private final MutableLiveData<User> currentUserLiveData;
    private final MutableLiveData<String> errorLiveData;

    public UserRepository() {
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        currentUserLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
    }

    public void login(String email, String password, OnLoginListener listener) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                FirebaseUser firebaseUser = authResult.getUser();
                if (firebaseUser != null) {
                    loadUserData(firebaseUser.getUid(), listener);
                }
            })
            .addOnFailureListener(e -> {
                errorLiveData.setValue(e.getMessage());
                listener.onFailure(e.getMessage());
            });
    }

    private void loadUserData(String userId, OnLoginListener listener) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    currentUserLiveData.setValue(user);
                    listener.onSuccess(user);
                } else {
                    listener.onFailure("User data not found");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                errorLiveData.setValue(error.getMessage());
                listener.onFailure(error.getMessage());
            }
        });
    }

    public void register(String email, String password, String name, String role, String phone, OnRegisterListener listener) {
        // If email is empty, use phone as the email (Firebase requires email format)
        String authEmail = !email.isEmpty() ? email : generatePhoneBasedEmail(phone);

        // First check if this email already exists; if yes, attempt login instead of failing the flow
        auth.fetchSignInMethodsForEmail(authEmail)
            .addOnSuccessListener(result -> {
                boolean exists = result != null && result.getSignInMethods() != null && !result.getSignInMethods().isEmpty();
                if (exists) {
                    // Account exists; try logging in with provided credentials
                    android.util.Log.d("UserRepository", "Email already exists, attempting login instead of registration");
                    login(authEmail, password, new OnLoginListener() {
                        @Override
                        public void onSuccess(User user) {
                            listener.onSuccess(user);
                        }

                        @Override
                        public void onFailure(String error) {
                            listener.onFailure("כתובת/טלפון כבר רשומים. השתמש בסיסמה הקיימת או אפס סיסמה.");
                        }
                    });
                    return;
                }

                // Not existing, proceed to create
                auth.createUserWithEmailAndPassword(authEmail, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser firebaseUser = authResult.getUser();
                        if (firebaseUser != null) {
                            User user = new User(firebaseUser.getUid(), email.isEmpty() ? "" : email, name, role, phone);
                            usersRef.child(firebaseUser.getUid()).setValue(user)
                                .addOnSuccessListener(aVoid -> {
                                    // For players, also create a Player record
                                    if (role.equals("PLAYER")) {
                                        createInitialPlayerRecord(firebaseUser.getUid(), name, phone, listener);
                                    } else {
                                        currentUserLiveData.setValue(user);
                                        listener.onSuccess(user);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    errorLiveData.setValue(e.getMessage());
                                    listener.onFailure(e.getMessage());
                                });
                        }
                    })
                    .addOnFailureListener(e -> {
                        errorLiveData.setValue(e.getMessage());
                        listener.onFailure(e.getMessage());
                    });
            })
            .addOnFailureListener(e -> {
                errorLiveData.setValue(e.getMessage());
                listener.onFailure(e.getMessage());
            });
    }
    
    private void createInitialPlayerRecord(String userId, String name, String phone, OnRegisterListener listener) {
        android.util.Log.d("UserRepository", "Creating initial player record for userId: " + userId + ", name: " + name);
        try {
            DatabaseReference playersRef = FirebaseDatabase.getInstance().getReference("players");
            String playerId = playersRef.push().getKey();
            android.util.Log.d("UserRepository", "Generated playerId: " + playerId);
            
            if (playerId != null) {
                // Create player with initial data
                com.example.testapp.models.Player player = new com.example.testapp.models.Player();
                player.setPlayerId(playerId);
                player.setUserId(userId);
                
                // Parse name into first and last
                String[] nameParts = name.split(" ", 2);
                player.setFirstName(nameParts[0]);
                player.setLastName(nameParts.length > 1 ? nameParts[1] : "");
                player.setPlayerPhone(phone);
                player.setCreatedAt(System.currentTimeMillis());
                player.setUpdatedAt(System.currentTimeMillis());
                
                android.util.Log.d("UserRepository", "Setting player record: " + playerId + " with firstName: " + player.getFirstName());
                playersRef.child(playerId).setValue(player)
                    .addOnSuccessListener(aVoid -> {
                        android.util.Log.d("UserRepository", "Player record created successfully: " + playerId);
                        // Now update User record with playerId
                        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                User user = snapshot.getValue(User.class);
                                if (user != null) {
                                    user.setPlayerId(playerId);
                                    usersRef.child(userId).setValue(user)
                                        .addOnSuccessListener(aVoid2 -> {
                                            android.util.Log.d("UserRepository", "User record updated with playerId: " + playerId);
                                            currentUserLiveData.setValue(user);
                                            listener.onSuccess(user);
                                        })
                                        .addOnFailureListener(e -> {
                                            android.util.Log.e("UserRepository", "Failed to update user with playerId: " + e.getMessage());
                                            listener.onFailure(e.getMessage());
                                        });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                android.util.Log.e("UserRepository", "Failed to read user record: " + error.getMessage());
                                listener.onFailure(error.getMessage());
                            }
                        });
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("UserRepository", "Failed to create player record: " + e.getMessage());
                        errorLiveData.setValue(e.getMessage());
                        listener.onFailure(e.getMessage());
                    });
            }
        } catch (Exception e) {
            android.util.Log.e("UserRepository", "Exception in createInitialPlayerRecord: " + e.getMessage());
            errorLiveData.setValue(e.getMessage());
            listener.onFailure(e.getMessage());
        }
    }
    
    public void loginWithEmailOrPhone(String input, String password, OnLoginListener listener) {
        // First try to login with email directly
        if (input.contains("@")) {
            android.util.Log.d("LoginWithEmailOrPhone", "Attempting email login: " + input);
            login(input, password, listener);
            return;
        }

        // It's a phone number: try multiple phone→email variants without DB reads (rules block unauth reads)
        android.util.Log.d("LoginWithEmailOrPhone", "Attempting phone login with phone: " + input);

        List<String> emailVariants = buildPhoneEmailVariants(input);
        tryEmailVariant(emailVariants, 0, password, listener);
    }

    private List<String> buildPhoneEmailVariants(String inputPhone) {
        List<String> variants = new ArrayList<>();
        String digits = normalizePhone(inputPhone);

        // Base variants of phone text
        variants.add(inputPhone);
        if (!digits.isEmpty() && !digits.equals(inputPhone)) variants.add(digits);
        if (digits.startsWith("0") && digits.length() > 1) {
            String noZero = digits.substring(1);
            variants.add("972" + noZero);
            variants.add("+972" + noZero);
        }
        if (digits.startsWith("972")) variants.add("+" + digits);

        // Map to email forms
        List<String> emails = new ArrayList<>();
        for (String v : variants) {
            String norm = normalizePhone(v);
            if (!norm.isEmpty()) emails.add("player_" + norm + "@basketballapp.local");
            // legacy as-entered
            emails.add("player_" + v + "@basketballapp.local");
        }

        // Deduplicate
        List<String> dedup = new ArrayList<>();
        for (String e : emails) {
            if (!dedup.contains(e)) dedup.add(e);
        }
        return dedup;
    }

    private void tryEmailVariant(List<String> emails, int index, String password, OnLoginListener listener) {
        if (index >= emails.size()) {
            listener.onFailure("מייל/סיסמה שגויים. נסה עם האימייל או אפס סיסמה.");
            return;
        }

        String email = emails.get(index);
        android.util.Log.d("LoginWithEmailOrPhone", "Trying phone-derived email: " + email);
        login(email, password, new OnLoginListener() {
            @Override
            public void onSuccess(User user) {
                listener.onSuccess(user);
            }

            @Override
            public void onFailure(String error) {
                tryEmailVariant(emails, index + 1, password, listener);
            }
        });
    }

    
    private String generatePhoneBasedEmail(String phone) {
        // Normalize phone to digits only so registration/login use the same derived email
        String digits = normalizePhone(phone);
        return "player_" + digits + "@basketballapp.local";
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("[^0-9]", "");
    }

    public void logout() {
        auth.signOut();
        currentUserLiveData.setValue(null);
    }

    public LiveData<User> getCurrentUser() {
        return currentUserLiveData;
    }

    public LiveData<String> getErrors() {
        return errorLiveData;
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public interface OnLoginListener {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public interface OnRegisterListener {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public void sendPasswordResetEmail(String email, OnPasswordResetListener listener) {
        // Firebase Authentication handles checking if user exists
        // It won't send email to non-existent users (security feature)
        // But it will always return success to avoid revealing user existence
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void sendPasswordResetPhone(String phone, OnPasswordResetListener listener) {
        // Try all phone→email variants (same as loginWithEmailOrPhone)
        android.util.Log.d("PasswordResetPhone", "Attempting password reset for phone: " + phone);
        List<String> emailVariants = buildPhoneEmailVariants(phone);
        tryPasswordResetVariant(emailVariants, 0, listener);
    }

    private void tryPasswordResetVariant(List<String> emails, int index, OnPasswordResetListener listener) {
        if (index >= emails.size()) {
            listener.onFailure("לא נמצא חשבון עבור מספר הטלפון הזה.");
            return;
        }

        String email = emails.get(index);
        android.util.Log.d("PasswordResetPhone", "Trying password reset for email: " + email);
        sendPasswordResetEmail(email, new OnPasswordResetListener() {
            @Override
            public void onSuccess() {
                android.util.Log.d("PasswordResetPhone", "Password reset sent to: " + email);
                listener.onSuccess();
            }

            @Override
            public void onFailure(String error) {
                tryPasswordResetVariant(emails, index + 1, listener);
            }
        });
    }

    public interface OnPasswordResetListener {
        void onSuccess();
        void onFailure(String error);
    }
    
    // Method to create initial admin user
    public void createAdminUser(String email, String password, String name, String phone, OnRegisterListener listener) {
        register(email, password, name, "ADMIN", phone, listener);
    }
}
