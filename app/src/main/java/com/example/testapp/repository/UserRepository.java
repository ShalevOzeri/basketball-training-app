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
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                FirebaseUser firebaseUser = authResult.getUser();
                if (firebaseUser != null) {
                    User user = new User(firebaseUser.getUid(), email, name, role, phone);
                    usersRef.child(firebaseUser.getUid()).setValue(user)
                        .addOnSuccessListener(aVoid -> {
                            currentUserLiveData.setValue(user);
                            listener.onSuccess(user);
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
}
