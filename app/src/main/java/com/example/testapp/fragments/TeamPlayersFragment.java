package com.example.testapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.R;
import com.example.testapp.adapters.TeamPlayersAdapter;
import com.example.testapp.models.Player;
import com.example.testapp.models.Team;
import com.example.testapp.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamPlayersFragment extends Fragment {

    private RecyclerView recyclerView;
    private TeamPlayersAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyView;
    private MaterialToolbar toolbar;
    
    private String teamId;
    private String teamName;
    private Team team;
    private DatabaseReference playersRef;
    private DatabaseReference usersRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_team_players, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Get arguments
        if (getArguments() != null) {
            teamId = getArguments().getString("teamId");
            teamName = getArguments().getString("teamName");
            team = getArguments().getParcelable("team");
        }

        // Enable options menu
        setHasOptionsMenu(true);

        initializeViews(view);
        setupToolbar(view);
        
        playersRef = FirebaseDatabase.getInstance().getReference("players");
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        setupRecyclerView();
        loadTeamPlayers();
    }

    private void initializeViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        recyclerView = view.findViewById(R.id.playersRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);
    }

    private void setupToolbar(View view) {
        // Setup toolbar with NavController
        NavController navController = Navigation.findNavController(view);
        
        // Set as action bar
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        
        // Setup navigation
        NavigationUI.setupWithNavController(toolbar, navController);
        
        // Set title
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setTitle("שחקני " + teamName);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload players when returning from other screens
        loadTeamPlayers();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear(); // Clear MainActivity menu items first
        inflater.inflate(R.menu.team_players_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add_player) {
            // Navigate to AddPlayersFragment
            Bundle args = new Bundle();
            args.putString("teamId", teamId);
            args.putString("teamName", teamName);
            if (getView() != null) {
                Navigation.findNavController(getView()).navigate(
                    R.id.action_teamPlayers_to_addPlayers, args);
            }
            return true;
        } else if (item.getItemId() == R.id.action_export_players) {
            exportPlayersToExcel();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TeamPlayersAdapter(new TeamPlayersAdapter.OnPlayerEditListener() {
            @Override
            public void onEditPlayer(Player player) {
                editPlayerDetails(player);
            }

            @Override
            public void onDeletePlayer(Player player) {
                showDeleteConfirmationDialog(player);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private void loadTeamPlayers() {
        progressBar.setVisibility(View.VISIBLE);

        usersRef.orderByChild("role").equalTo("PLAYER")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    List<User> teamUsers = new ArrayList<>();

                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            // Set userId from Firebase key
                            user.setUserId(userSnapshot.getKey());
                            
                            if (user.getTeamIds().contains(teamId)) {
                                teamUsers.add(user);
                            }
                        }
                    }
                    
                    if (teamUsers.isEmpty()) {
                        adapter.setPlayers(new ArrayList<>());
                        progressBar.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        return;
                    }

                    fetchPlayersForUsers(teamUsers);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "שגיאה בטעינת שחקנים", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void fetchPlayersForUsers(List<User> teamUsers) {
        List<Player> players = new ArrayList<>();
        final int total = teamUsers.size();
        final int[] done = {0};

        for (User user : teamUsers) {
            loadPlayerByUser(user, player -> {
                if (player != null) {
                    // Player loaded successfully (teamId context is already in User.teamIds)
                    players.add(player);
                }
                done[0]++;
                if (done[0] == total) {
                    adapter.setPlayers(players);
                    progressBar.setVisibility(View.GONE);

                    if (players.isEmpty()) {
                        emptyView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        emptyView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
    }

    private interface PlayerCallback {
        void onLoaded(Player player);
    }

    private void loadPlayerByUser(User user, PlayerCallback callback) {
        String playerId = user.getPlayerId();
        String userId = user.getUserId();

        if (playerId != null && !playerId.isEmpty()) {
            playersRef.child(playerId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Player player = snapshot.getValue(Player.class);
                        if (player != null) {
                            player.setUserId(userId);
                            callback.onLoaded(player);
                            return;
                        }
                    }
                    // Fallback to query by userId
                    queryPlayerByUserId(userId, callback);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    queryPlayerByUserId(userId, callback);
                }
            });
        } else {
            queryPlayerByUserId(userId, callback);
        }
    }

    private void queryPlayerByUserId(String userId, PlayerCallback callback) {
        playersRef.orderByChild("userId").equalTo(userId).limitToFirst(1)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Player player = child.getValue(Player.class);
                            if (player != null) {
                                player.setUserId(userId);
                                callback.onLoaded(player);
                                return;
                            }
                        }
                    }
                    callback.onLoaded(null);
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    callback.onLoaded(null);
                }
            });
    }

    private void editPlayerDetails(Player player) {
        // Navigate to PlayerDetailsFragment
        Bundle args = new Bundle();
        args.putString("playerId", player.getPlayerId());
        args.putString("userId", player.getUserId());
        args.putString("teamId", teamId);
        if (getView() != null) {
            Navigation.findNavController(getView()).navigate(
                R.id.action_teamPlayers_to_playerDetails, args);
        }
    }

    private void showDeleteConfirmationDialog(Player player) {
        new AlertDialog.Builder(requireContext())
            .setTitle("הסרת שחקן")
            .setMessage("האם להסיר את " + player.getFirstName() + " " + player.getLastName() + " מהקבוצה?")
            .setPositiveButton("הסר", (dialog, which) -> deletePlayer(player))
            .setNegativeButton("ביטול", null)
            .show();
    }

    private void deletePlayer(Player player) {
        progressBar.setVisibility(View.VISIBLE);
        
        String userId = player.getUserId();

        // Remove this team from user's teamIds (keep single player record)
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("teamIds").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<String> teamIds = new ArrayList<>();
                if (snapshot.exists()) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String tid = child.getValue(String.class);
                        if (tid != null && !tid.equals(teamId)) {
                            teamIds.add(tid);
                        }
                    }
                }

                userRef.child("teamIds").setValue(teamIds.isEmpty() ? null : teamIds)
                    .addOnSuccessListener(aVoid -> {
                        if (teamIds.isEmpty()) {
                            userRef.child("registrationStatus").setValue("NONE");
                        }
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "השחקן הוסר בהצלחה", Toast.LENGTH_SHORT).show();
                        loadTeamPlayers();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "שגיאה בעדכון נתוני השחקן", Toast.LENGTH_SHORT).show();
                    });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "שגיאה בטעינת נתוני השחקן", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddPlayersDialog() {
        // Load all players
        usersRef.orderByChild("role").equalTo("PLAYER").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<User> allPlayers = new ArrayList<>();
                
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setUserId(userSnapshot.getKey());
                        allPlayers.add(user);
                    }
                }
                
                if (allPlayers.isEmpty()) {
                    Toast.makeText(requireContext(), "אין שחקנים במערכת", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                // Show dialog with search
                showPlayerSearchDialog(allPlayers);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(requireContext(), "שגיאה בטעינת שחקנים", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void showPlayerSearchDialog(List<User> allPlayers) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("הוסף שחקנים לקבוצה " + teamName);
        
        // Create layout with search box and list
        LinearLayout mainLayout = new LinearLayout(requireContext());
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setPadding(20, 20, 20, 20);
        
        // Create search box
        EditText searchBox = new EditText(requireContext());
        searchBox.setHint("חפש שחקן לפי שם...");
        searchBox.setSingleLine(true);
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        searchParams.bottomMargin = 20;
        mainLayout.addView(searchBox, searchParams);
        
        // Create list view with checkboxes
        ListView listView = new ListView(requireContext());
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        
        // Create adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_list_item_multiple_choice);
        
        // Add all players initially
        List<User> filteredPlayers = new ArrayList<>(allPlayers);
        updateListAdapter(adapter, filteredPlayers);
        
        listView.setAdapter(adapter);
        
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            800);
        mainLayout.addView(listView, listParams);
        
        // Search functionality
        searchBox.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filteredPlayers.clear();
                String query = s.toString().toLowerCase().trim();
                
                if (query.isEmpty()) {
                    filteredPlayers.addAll(allPlayers);
                } else {
                    for (User player : allPlayers) {
                        if (player.getName().toLowerCase().contains(query) || 
                            player.getEmail().toLowerCase().contains(query)) {
                            filteredPlayers.add(player);
                        }
                    }
                }
                
                updateListAdapter(adapter, filteredPlayers);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        builder.setView(mainLayout);
        
        builder.setPositiveButton("הוסף", (dialog, which) -> {
            android.util.SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
            int addedCount = 0;
            
            if (checkedItems != null) {
                for (int i = 0; i < checkedItems.size(); i++) {
                    int position = checkedItems.keyAt(i);
                    boolean isChecked = checkedItems.valueAt(i);
                    
                    if (isChecked && position >= 0 && position < filteredPlayers.size()) {
                        User player = filteredPlayers.get(position);
                        addPlayerToTeam(player);
                        addedCount++;
                    }
                }
            }
            
            if (addedCount > 0) {
                Toast.makeText(requireContext(), addedCount + " שחקנים נוספו לקבוצה", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "לא נבחרו שחקנים", Toast.LENGTH_SHORT).show();
            }
        })
        .setNegativeButton("ביטול", null)
        .show();
    }
    
    private void updateListAdapter(ArrayAdapter<String> adapter, List<User> players) {
        adapter.clear();
        for (User player : players) {
            adapter.add(player.getName() + " (" + player.getEmail() + ")");
        }
        adapter.notifyDataSetChanged();
    }
    
    private void addPlayerToTeam(User player) {
        // Add team to player's teamIds
        List<String> teamIds = player.getTeamIds();
        if (teamIds == null) {
            teamIds = new ArrayList<>();
        }
        if (!teamIds.contains(teamId)) {
            teamIds.add(teamId);
        }
        
        // Update user with new team membership; player record is single per user
        Map<String, Object> updates = new HashMap<>();
        updates.put("teamIds", teamIds);
        
        // Always sync teamId with first team in teamIds (for backward compatibility)
        if (!teamIds.isEmpty()) {
            updates.put("teamId", teamIds.get(0));
        }
        
        usersRef.child(player.getUserId()).updateChildren(updates)
            .addOnSuccessListener(aVoid -> loadTeamPlayers())
            .addOnFailureListener(e -> Toast.makeText(requireContext(), "שגיאה בהוספת שחקן", Toast.LENGTH_SHORT).show());
    }

    private void exportPlayersToExcel() {
        List<Player> currentPlayers = adapter.getPlayers();
        if (currentPlayers == null || currentPlayers.isEmpty()) {
            Toast.makeText(requireContext(), "אין שחקנים לייצוא", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet(teamName);

            // Set column width
            sheet.setColumnWidth(0, 3000);
            sheet.setColumnWidth(1, 3000);
            sheet.setColumnWidth(2, 2500);
            sheet.setColumnWidth(3, 3000);
            sheet.setColumnWidth(4, 3500);
            sheet.setColumnWidth(5, 3500);
            sheet.setColumnWidth(6, 3000);
            sheet.setColumnWidth(7, 3000);
            sheet.setColumnWidth(8, 2500);
            sheet.setColumnWidth(9, 2500);

            // Create header row with proper RTL order
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "שם פרטי",
                "שם משפחה",
                "כיתה",
                "בית ספר",
                "טלפון שחקן",
                "טלפון הורה",
                "תעודת זהות",
                "תאריך לידה",
                "מידת גופיה",
                "מספר גופיה"
            };

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Add player data rows
            int rowNum = 1;
            for (Player player : currentPlayers) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(nvl(player.getFirstName()));
                row.createCell(1).setCellValue(nvl(player.getLastName()));
                row.createCell(2).setCellValue(nvl(player.getGrade()));
                row.createCell(3).setCellValue(nvl(player.getSchool()));
                row.createCell(4).setCellValue(nvl(player.getPlayerPhone()));
                row.createCell(5).setCellValue(nvl(player.getParentPhone()));
                row.createCell(6).setCellValue(nvl(player.getIdNumber()));
                row.createCell(7).setCellValue(nvl(player.getBirthDate()));
                row.createCell(8).setCellValue(nvl(player.getShirtSize()));
                row.createCell(9).setCellValue(nvl(player.getJerseyNumber()));
            }

            // Create file and save
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String fileName = "Players_" + teamName + "_" + System.currentTimeMillis() + ".xlsx";
            File file = new File(downloadsDir, fileName);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
                workbook.close();

                // Get current user email and send
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                FirebaseDatabase.getInstance().getReference("users").child(userId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            User user = snapshot.getValue(User.class);
                            if (user != null && user.getEmail() != null) {
                                sendExcelViaEmail(file, user.getEmail(), fileName);
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(requireContext(), "לא ניתן למצוא את כתובת המייל שלך", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(requireContext(), "שגיאה בטעינת נתוני המשתמש", Toast.LENGTH_SHORT).show();
                        }
                    });
            }
        } catch (IOException e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "שגיאה ביצירת הקובץ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendExcelViaEmail(File file, String userEmail, String fileName) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{userEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "דוח שחקנים - " + teamName);
        emailIntent.putExtra(Intent.EXTRA_TEXT, "מצורף דוח רשימת שחקני הקבוצה");
        
        try {
            android.net.Uri fileUri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                requireContext().getApplicationContext().getPackageName() + ".fileprovider",
                file
            );
            emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            progressBar.setVisibility(View.GONE);
            startActivity(Intent.createChooser(emailIntent, "בחר אפליקציית דוא\"ל"));
        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(requireContext(), "שגיאה בשליחת הדוא\"ל: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String nvl(String value) {
        return value != null ? value : "";
    }
}
