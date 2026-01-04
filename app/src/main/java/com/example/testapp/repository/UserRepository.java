package com.example.testapp.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.testapp.models.Player;
import com.example.testapp.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class UserRepository {
    private final FirebaseAuth auth;
    private final DatabaseReference usersRef;
    private final DatabaseReference playersRef;
    private final MutableLiveData<User> currentUserLiveData;
    private final MutableLiveData<String> errorLiveData;

    public UserRepository() {
        auth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");
        playersRef = FirebaseDatabase.getInstance().getReference("players");
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
                listener.onFailure(e.getMessage());
            });
    }

    private void loadUserData(String userId, OnLoginListener listener) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    user.setUserId(userId); // Ensure userId is set
                    if (user.isPlayer()) {
                        ensurePlayerRecordExists(user, () -> {
                            currentUserLiveData.setValue(user);
                            listener.onSuccess(user);
                        }, listener);
                    } else {
                        currentUserLiveData.setValue(user);
                        listener.onSuccess(user);
                    }
                } else {
                    listener.onFailure("User data not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    private void ensurePlayerRecordExists(User user, SimpleCallback onSuccess, OnLoginListener listener) {
        Query playerQuery = playersRef.orderByChild("userId").equalTo(user.getUserId());
        playerQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    onSuccess.call();
                } else {
                    createPlayerRecord(user, new OnRegisterListener() {
                        @Override
                        public void onSuccess(User updatedUser) {
                            onSuccess.call();
                        }

                        @Override
                        public void onFailure(String error) {
                            listener.onFailure(error);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.getMessage());
            }
        });
    }

    public void register(String email, String password, String name, String role, String phone, OnRegisterListener listener) {
        String authEmail = !email.isEmpty() ? email : generatePhoneBasedEmail(phone);

        auth.fetchSignInMethodsForEmail(authEmail)
            .addOnSuccessListener(result -> {
                boolean exists = result != null && result.getSignInMethods() != null && !result.getSignInMethods().isEmpty();
                if (exists) {
                    listener.onFailure("כתובת/טלפון כבר רשומים. השתמש בסיסמה הקיימת או אפס סיסמה.");
                    return;
                }

                auth.createUserWithEmailAndPassword(authEmail, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser firebaseUser = authResult.getUser();
                        if (firebaseUser != null) {
                            User user = new User(firebaseUser.getUid(), email.isEmpty() ? "" : email, name, role, phone);
                            usersRef.child(firebaseUser.getUid()).setValue(user)
                                .addOnSuccessListener(aVoid -> {
                                    if (role.equals("PLAYER")) {
                                        createPlayerRecord(user, listener);
                                    } else {
                                        currentUserLiveData.setValue(user);
                                        listener.onSuccess(user);
                                    }
                                })
                                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                        }
                    })
                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
            })
            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }
    
    private void createPlayerRecord(User user, OnRegisterListener listener) {
        String userId = user.getUserId();

        String playerId = playersRef.push().getKey();
        if (playerId == null) {
            listener.onFailure("Failed to create a new player ID.");
            return;
        }

        Player player = new Player();
        player.setPlayerId(playerId);
        player.setUserId(userId);
        
        String[] nameParts = user.getName().split(" ", 2);
        player.setFirstName(nameParts[0]);
        player.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        player.setPlayerPhone(user.getPhone());
        player.setCreatedAt(System.currentTimeMillis());
        player.setUpdatedAt(System.currentTimeMillis());
        
        playersRef.child(playerId).setValue(player)
            .addOnSuccessListener(aVoid -> {
                usersRef.child(userId).child("playerId").setValue(playerId)
                    .addOnSuccessListener(aVoid2 -> {
                        user.setPlayerId(playerId);
                        currentUserLiveData.setValue(user);
                        listener.onSuccess(user);
                    })
                    .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
            })
            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }
    
    public void loginWithEmailOrPhone(String input, String password, OnLoginListener listener) {
        String emailToLogin;
        if (input.contains("@")) {
            emailToLogin = input;
        } else {
            emailToLogin = generatePhoneBasedEmail(input);
        }
        login(emailToLogin, password, listener);
    }

    private String generatePhoneBasedEmail(String phone) {
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
    
    private interface SimpleCallback {
        void call();
    }

    public void sendPasswordResetEmail(String email, OnPasswordResetListener listener) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void sendPasswordResetPhone(String phone, OnPasswordResetListener listener) {
        String emailToReset = generatePhoneBasedEmail(phone);
        sendPasswordResetEmail(emailToReset, listener);
    }

    public interface OnPasswordResetListener {
        void onSuccess();
        void onFailure(String error);
    }
    
    public void createAdminUser(String email, String password, String name, String phone, OnRegisterListener listener) {
        register(email, password, name, "ADMIN", phone, listener);
    }
}
