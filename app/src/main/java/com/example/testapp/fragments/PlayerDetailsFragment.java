package com.example.testapp.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.testapp.R;
import com.example.testapp.models.Player;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PlayerDetailsFragment extends Fragment {

    private EditText firstNameEditText, lastNameEditText, gradeEditText, schoolEditText, playerPhoneEditText, parentPhoneEditText, idNumberEditText, birthDateEditText, jerseyNumberEditText;
    private Spinner shirtSizeSpinner;
    private Button saveButton;
    private ProgressBar progressBar;

    private DatabaseReference usersRef, playersRef;
    private String userId, playerId;

    private static final String[] SHIRT_SIZES = {"XS", "S", "M", "L", "XL", "XXL"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        playersRef = FirebaseDatabase.getInstance().getReference("players");

        setupShirtSizeSpinner();
        loadInitialData();

        saveButton.setOnClickListener(v -> savePlayerDetails());
        birthDateEditText.setOnClickListener(v -> showCustomDatePicker());
    }

    private void showCustomDatePicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_custom_datepicker, null);

        DatePicker datePicker = dialogView.findViewById(R.id.datePicker);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth();
            int year = datePicker.getYear();
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            birthDateEditText.setText(sdf.format(calendar.getTime()));
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void initializeViews(View view) {
        firstNameEditText = view.findViewById(R.id.firstNameEditText);
        lastNameEditText = view.findViewById(R.id.lastNameEditText);
        gradeEditText = view.findViewById(R.id.gradeEditText);
        schoolEditText = view.findViewById(R.id.schoolEditText);
        playerPhoneEditText = view.findViewById(R.id.playerPhoneEditText);
        parentPhoneEditText = view.findViewById(R.id.parentPhoneEditText);
        idNumberEditText = view.findViewById(R.id.idNumberEditText);
        birthDateEditText = view.findViewById(R.id.birthDateEditText);
        jerseyNumberEditText = view.findViewById(R.id.jerseyNumberEditText);
        shirtSizeSpinner = view.findViewById(R.id.shirtSizeSpinner);
        saveButton = view.findViewById(R.id.saveButton);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupShirtSizeSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, SHIRT_SIZES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        shirtSizeSpinner.setAdapter(adapter);
    }

    private void loadInitialData() {
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    com.example.testapp.models.User user = snapshot.getValue(com.example.testapp.models.User.class);
                    if (user != null) {
                        if (user.getName() != null) {
                            String[] nameParts = user.getName().split(" ", 2);
                            firstNameEditText.setText(nameParts[0]);
                            if (nameParts.length > 1) {
                                lastNameEditText.setText(nameParts[1]);
                            }
                        }
                        playerPhoneEditText.setText(user.getPhone());
                        playerId = user.getPlayerId();
                    }
                }
                loadPlayerSpecificData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
            }
        });
    }
    
    private void loadPlayerByPlayerId(String id) {
        playersRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                if (snapshot.exists()) {
                    Player player = snapshot.getValue(Player.class);
                    if (player != null) {
                        populatePlayerFields(player);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                handleSaveError(error.getMessage());
            }
        });
    }

    private void loadPlayerSpecificData() {
        if (playerId != null && !playerId.isEmpty()) {
            loadPlayerByPlayerId(playerId);
        } else {
            loadPlayerByUserId();
        }
    }

    private void loadPlayerByUserId() {
        if (userId == null || userId.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        playersRef.orderByChild("userId").equalTo(userId).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressBar.setVisibility(View.GONE);
                        saveButton.setEnabled(true);
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                Player player = child.getValue(Player.class);
                                if (player != null) {
                                    populatePlayerFields(player);
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                       handleSaveError(error.getMessage());
                    }
                });
    }

    private void populatePlayerFields(Player player) {
        if (TextUtils.isEmpty(firstNameEditText.getText().toString().trim()) && player.getFirstName() != null) {
            firstNameEditText.setText(player.getFirstName());
        }
        if (TextUtils.isEmpty(lastNameEditText.getText().toString().trim()) && player.getLastName() != null) {
            lastNameEditText.setText(player.getLastName());
        }
        gradeEditText.setText(player.getGrade() != null ? player.getGrade() : "");
        schoolEditText.setText(player.getSchool() != null ? player.getSchool() : "");
        if (TextUtils.isEmpty(playerPhoneEditText.getText().toString().trim()) && player.getPlayerPhone() != null) {
            playerPhoneEditText.setText(player.getPlayerPhone());
        }
        parentPhoneEditText.setText(player.getParentPhone() != null ? player.getParentPhone() : "");
        idNumberEditText.setText(player.getIdNumber() != null ? player.getIdNumber() : "");
        birthDateEditText.setText(player.getBirthDate() != null ? player.getBirthDate() : "");
        jerseyNumberEditText.setText(player.getJerseyNumber() != null ? player.getJerseyNumber() : "");

        for (int i = 0; i < SHIRT_SIZES.length; i++) {
            if (SHIRT_SIZES[i].equals(player.getShirtSize())) {
                shirtSizeSpinner.setSelection(i);
                break;
            }
        }
    }

    private void savePlayerDetails() {
        String firstName = firstNameEditText.getText().toString().trim();
        String lastName = lastNameEditText.getText().toString().trim();
        String grade = gradeEditText.getText().toString().trim();
        String school = schoolEditText.getText().toString().trim();
        String playerPhone = playerPhoneEditText.getText().toString().trim();
        String parentPhone = parentPhoneEditText.getText().toString().trim();
        String idNumber = idNumberEditText.getText().toString().trim();
        String birthDate = birthDateEditText.getText().toString().trim();
        String shirtSize = shirtSizeSpinner.getSelectedItem().toString();
        String jerseyNumber = jerseyNumberEditText.getText().toString().trim();

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)) {
            Toast.makeText(requireContext(), "שם פרטי ושם משפחה הם שדות חובה", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("name", firstName + " " + lastName);
        userUpdates.put("phone", playerPhone);

        usersRef.child(userId).updateChildren(userUpdates)
            .addOnSuccessListener(aVoid -> findOrCreatePlayerRecord(firstName, lastName, grade, school, playerPhone, parentPhone, idNumber, birthDate, shirtSize, jerseyNumber))
            .addOnFailureListener(e -> handleSaveError("שגיאה בעדכון פרטי משתמש: " + e.getMessage()));
    }

    private void findOrCreatePlayerRecord(String firstName, String lastName, String grade, String school,
                                        String playerPhone, String parentPhone, String idNumber,
                                        String birthDate, String shirtSize, String jerseyNumber) {
        playersRef.orderByChild("userId").equalTo(userId).limitToFirst(1)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String playerKey;
                    Player existingPlayer = null;
                    boolean isNewPlayer = false;

                    if (snapshot.exists()) {
                        DataSnapshot playerSnapshot = snapshot.getChildren().iterator().next();
                        playerKey = playerSnapshot.getKey();
                        existingPlayer = playerSnapshot.getValue(Player.class);
                    } else {
                        playerKey = playersRef.push().getKey();
                        isNewPlayer = true;
                    }

                    if (playerKey == null) {
                        handleSaveError("שגיאה ביצירת מזהה שחקן.");
                        return;
                    }

                    final String finalPlayerKey = playerKey;
                    final boolean finalIsNewPlayer = isNewPlayer;

                    String teamId = (existingPlayer != null) ? existingPlayer.getTeamId() : null;
                    if (!TextUtils.isEmpty(jerseyNumber) && teamId != null) {
                        checkJerseyNumberAvailability(teamId, jerseyNumber, userId, isAvailable -> {
                            if (isAvailable) {
                                performPlayerUpdate(finalPlayerKey, finalIsNewPlayer, firstName, lastName, grade, school, playerPhone, parentPhone, idNumber, birthDate, shirtSize, jerseyNumber);
                            } else {
                                Toast.makeText(requireContext(), "מספר גופיה " + jerseyNumber + " כבר תפוס בקבוצה.", Toast.LENGTH_LONG).show();
                                performPlayerUpdate(finalPlayerKey, finalIsNewPlayer, firstName, lastName, grade, school, playerPhone, parentPhone, idNumber, birthDate, shirtSize, null);
                            }
                        });
                    } else {
                        performPlayerUpdate(finalPlayerKey, finalIsNewPlayer, firstName, lastName, grade, school, playerPhone, parentPhone, idNumber, birthDate, shirtSize, jerseyNumber);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    handleSaveError(error.getMessage());
                }
            });
    }

    private void performPlayerUpdate(String playerKey, boolean isNewPlayer, String firstName, String lastName,
                                   String grade, String school, String playerPhone, String parentPhone,
                                   String idNumber, String birthDate, String shirtSize, String jerseyNumber) {

        Map<String, Object> playerUpdates = new HashMap<>();
        playerUpdates.put("firstName", firstName);
        playerUpdates.put("lastName", lastName);
        playerUpdates.put("grade", grade);
        playerUpdates.put("school", school);
        playerUpdates.put("playerPhone", playerPhone);
        playerUpdates.put("parentPhone", parentPhone);
        playerUpdates.put("idNumber", idNumber);
        playerUpdates.put("birthDate", birthDate);
        playerUpdates.put("shirtSize", shirtSize);
        playerUpdates.put("jerseyNumber", jerseyNumber);
        playerUpdates.put("updatedAt", System.currentTimeMillis());
        playerUpdates.put("userId", userId);
        
        if (isNewPlayer) {
            playerUpdates.put("createdAt", System.currentTimeMillis());
        }

        Task<Void> updatePlayerTask = playersRef.child(playerKey).updateChildren(playerUpdates);
        Task<Void> updateUserPlayerIdTask = usersRef.child(userId).child("playerId").setValue(playerKey);

        Tasks.whenAll(updatePlayerTask, updateUserPlayerIdTask)
            .addOnSuccessListener(aVoid -> handleSaveSuccess())
            .addOnFailureListener(e -> handleSaveError(e.getMessage()));
    }

    private void checkJerseyNumberAvailability(String teamId, String jerseyNumber, String currentUserId,
                                              OnJerseyCheckListener listener) {
        // Fixed access: check via users.teamIds instead of player.teamId
        // Because a player can be in multiple teams and player.teamId might not be updated
        
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("role").equalTo("PLAYER")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<String> userIdsInTeam = new ArrayList<>();
                    
                    // Find all users belonging to this team
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        DataSnapshot teamIdsSnapshot = userSnapshot.child("teamIds");
                        if (teamIdsSnapshot.exists()) {
                            for (DataSnapshot teamSnapshot : teamIdsSnapshot.getChildren()) {
                                String tid = teamSnapshot.getValue(String.class);
                                if (teamId.equals(tid)) {
                                    userIdsInTeam.add(userSnapshot.getKey());
                                    break;
                                }
                            }
                        }
                    }
                    
                    // Now check jersey number for each player in the team
                    if (userIdsInTeam.isEmpty()) {
                        listener.onResult(true);
                        return;
                    }
                    
                    playersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot playersSnapshot) {
                            boolean isAvailable = true;
                            for (DataSnapshot playerSnapshot : playersSnapshot.getChildren()) {
                                Player player = playerSnapshot.getValue(Player.class);
                                if (player != null &&
                                    userIdsInTeam.contains(player.getUserId()) &&
                                    jerseyNumber.equals(player.getJerseyNumber()) &&
                                    !currentUserId.equals(player.getUserId())) {
                                    isAvailable = false;
                                    break;
                                }
                            }
                            listener.onResult(isAvailable);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            listener.onResult(true);
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    listener.onResult(true);
                }
            });
    }
    
    private void handleSaveSuccess() {
        progressBar.setVisibility(View.GONE);
        saveButton.setEnabled(true);
        Toast.makeText(requireContext(), "הפרטים עודכנו בהצלחה", Toast.LENGTH_SHORT).show();
    }

    private void handleSaveError(String errorMessage) {
        progressBar.setVisibility(View.GONE);
        saveButton.setEnabled(true);
        Toast.makeText(requireContext(), "שגיאה: " + errorMessage, Toast.LENGTH_LONG).show();
    }

    private interface OnJerseyCheckListener {
        void onResult(boolean isAvailable);
    }
}
