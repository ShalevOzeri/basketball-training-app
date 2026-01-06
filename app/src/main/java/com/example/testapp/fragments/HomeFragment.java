package com.example.testapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.testapp.LoginActivity;
import com.example.testapp.R;
import com.example.testapp.models.User;
import com.example.testapp.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    private CardView courtsCard, teamsCard, scheduleCard, allCourtsCard, manageUsersCard, playerDetailsCard;
    private UserRepository userRepository;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        userRepository = new UserRepository();
        
        loadCurrentUserAndCheckPermissions();
        setupCardClicks(view);
    }

    private void initializeViews(View view) {
        courtsCard = view.findViewById(R.id.courtsCard);
        teamsCard = view.findViewById(R.id.teamsCard);
        scheduleCard = view.findViewById(R.id.scheduleCard);
        allCourtsCard = view.findViewById(R.id.allCourtsCard);
        manageUsersCard = view.findViewById(R.id.manageUsersCard);
        playerDetailsCard = view.findViewById(R.id.playerDetailsCard);
    }

    private void setupCardClicks(View view) {
        // ניווט למגרשים
        if (courtsCard != null) {
            courtsCard.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(view).navigate(R.id.action_home_to_courts);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "שגיאה בניווט: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // ניווט לקבוצות
        if (teamsCard != null) {
            teamsCard.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(view).navigate(R.id.action_home_to_teams);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "שגיאה בניווט: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // ניווט ללוח אימונים
        scheduleCard.setOnClickListener(v -> {
            try {
                Navigation.findNavController(view).navigate(R.id.action_home_to_schedule);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "שגיאה בניווט: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // ניווט לתצוגת כל המגרשים
        allCourtsCard.setOnClickListener(v -> {
            try {
                Navigation.findNavController(view).navigate(R.id.action_home_to_allCourtsView);
            } catch (Exception e) {
                Toast.makeText(requireContext(), "שגיאה בניווט: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // ניווט לניהול משתמשים
        if (manageUsersCard != null) {
            manageUsersCard.setOnClickListener(v -> {
                try {
                    Navigation.findNavController(view).navigate(R.id.action_home_to_manageUsers);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "שגיאה בניווט: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // ניווט לפרטי שחקן
        if (playerDetailsCard != null) {
            playerDetailsCard.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(requireContext(), com.example.testapp.PlayerDetailsActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "שגיאה בניווט: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadCurrentUserAndCheckPermissions() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            // אם אין משתמש מחובר, חזרה למסך התחברות
            Intent intent = new Intent(requireActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentUser = snapshot.getValue(User.class);
                if (currentUser != null) {
                    // עדכון כותרת עם תפקיד משתמש
                    updateToolbarWithRole(currentUser);

                    // הצגת תכונות לפי תפקיד
                    if (currentUser.isAdmin()) {
                        // מנהל רואה הכל
                        if (manageUsersCard != null) manageUsersCard.setVisibility(View.VISIBLE);
                        if (courtsCard != null) courtsCard.setVisibility(View.VISIBLE);
                        if (teamsCard != null) teamsCard.setVisibility(View.VISIBLE);
                    } else if (currentUser.isCoordinator()) {
                        // רכז רואה ניהול מגרשים וקבוצות
                        if (courtsCard != null) courtsCard.setVisibility(View.VISIBLE);
                        if (teamsCard != null) teamsCard.setVisibility(View.VISIBLE);
                    } else if (currentUser.isCoach()) {
                        // מאמן רואה את הקבוצות שלו
                        if (teamsCard != null) teamsCard.setVisibility(View.VISIBLE);
                    } else if (currentUser.isPlayer()) {
                        // שחקן רואה את פרטי השחקן
                        if (playerDetailsCard != null) playerDetailsCard.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "שגיאה בטעינת נתוני משתמש", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateToolbarWithRole(User user) {
        String roleText = "";
        switch (user.getRole()) {
            case "ADMIN":
                roleText = "מנהל";
                break;
            case "COORDINATOR":
                roleText = "רכז";
                break;
            case "COACH":
                roleText = "מאמן";
                break;
            case "PLAYER":
                roleText = "שחקן";
                break;
        }

        if (requireActivity() != null && ((androidx.appcompat.app.AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((androidx.appcompat.app.AppCompatActivity) requireActivity()).getSupportActionBar()
                    .setTitle("TIMEOUT • " + user.getName() + " • " + roleText);
        }
    }
}
