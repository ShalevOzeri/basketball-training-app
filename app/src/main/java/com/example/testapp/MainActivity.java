package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.testapp.repository.UserRepository;
import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    private CardView courtsCard, teamsCard, scheduleCard, allCourtsCard;
    private MaterialToolbar toolbar;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        userRepository = new UserRepository();
        
        setupToolbar();
        setupCardClicks();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        courtsCard = findViewById(R.id.courtsCard);
        teamsCard = findViewById(R.id.teamsCard);
        scheduleCard = findViewById(R.id.scheduleCard);
        allCourtsCard = findViewById(R.id.allCourtsCard);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("מערכת ניהול מתחם אימונים");
        }
    }

    private void setupCardClicks() {
        courtsCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CourtsActivity.class);
            startActivity(intent);
        });

        teamsCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TeamsActivity.class);
            startActivity(intent);
        });

        scheduleCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScheduleActivity.class);
            startActivity(intent);
        });

        allCourtsCard.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AllCourtsViewActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        userRepository.logout();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

