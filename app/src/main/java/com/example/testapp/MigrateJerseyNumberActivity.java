package com.example.testapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.testapp.models.Player;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class MigrateJerseyNumberActivity extends AppCompatActivity {
    
    private DatabaseReference playersRef;
    private Button migrateButton;
    private ProgressBar progressBar;
    private TextView statusTextView;
    private int totalPlayers = 0;
    private int updatedPlayers = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_migrate_jersey_number);
        
        // אתחול Firebase
        playersRef = FirebaseDatabase.getInstance().getReference("players");
        
        // אתחול UI
        migrateButton = findViewById(R.id.migrateButton);
        progressBar = findViewById(R.id.progressBar);
        statusTextView = findViewById(R.id.statusTextView);
        
        migrateButton.setOnClickListener(v -> startMigration());
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("מיגרציה - מספרי גופיות");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
    
    private void startMigration() {
        migrateButton.setEnabled(false);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        statusTextView.setText("טוען שחקנים...");
        
        playersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                totalPlayers = (int) snapshot.getChildrenCount();
                updatedPlayers = 0;
                
                if (totalPlayers == 0) {
                    statusTextView.setText("לא נמצאו שחקנים");
                    progressBar.setVisibility(ProgressBar.GONE);
                    migrateButton.setEnabled(true);
                    return;
                }
                
                statusTextView.setText("מעדכן " + totalPlayers + " שחקנים...");
                
                Map<String, Object> updates = new HashMap<>();
                
                for (DataSnapshot playerSnapshot : snapshot.getChildren()) {
                    String playerKey = playerSnapshot.getKey();
                    Player player = playerSnapshot.getValue(Player.class);
                    
                    if (player != null) {
                        // אם השדה jerseyNumber לא קיים או הוא null, נוסיף אותו כמחרוזת ריקה
                        if (player.getJerseyNumber() == null) {
                            updates.put(playerKey + "/jerseyNumber", "");
                        }
                    }
                }
                
                if (updates.isEmpty()) {
                    statusTextView.setText("כל השחקנים כבר מעודכנים!");
                    progressBar.setVisibility(ProgressBar.GONE);
                    migrateButton.setEnabled(true);
                    Toast.makeText(MigrateJerseyNumberActivity.this, 
                        "המיגרציה הושלמה - לא נדרשו עדכונים", Toast.LENGTH_LONG).show();
                    return;
                }
                
                // ביצוע העדכון בפעולה אחת
                playersRef.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        statusTextView.setText("המיגרציה הושלמה בהצלחה!\n" + 
                            "עודכנו " + updates.size() + " שחקנים");
                        progressBar.setVisibility(ProgressBar.GONE);
                        migrateButton.setEnabled(true);
                        Toast.makeText(MigrateJerseyNumberActivity.this, 
                            "כל השחקנים עודכנו בהצלחה!", Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        statusTextView.setText("שגיאה במיגרציה: " + e.getMessage());
                        progressBar.setVisibility(ProgressBar.GONE);
                        migrateButton.setEnabled(true);
                        Toast.makeText(MigrateJerseyNumberActivity.this, 
                            "שגיאה: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
            }
            
            @Override
            public void onCancelled(DatabaseError error) {
                statusTextView.setText("שגיאה: " + error.getMessage());
                progressBar.setVisibility(ProgressBar.GONE);
                migrateButton.setEnabled(true);
                Toast.makeText(MigrateJerseyNumberActivity.this, 
                    "שגיאה: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
