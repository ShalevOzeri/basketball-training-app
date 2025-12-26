package com.example.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.adapters.CourtAdapter;
import com.example.testapp.models.Court;
import com.example.testapp.viewmodel.CourtViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CourtsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CourtAdapter adapter;
    private CourtViewModel viewModel;
    private FloatingActionButton fab;
    private MaterialToolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courts);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        setupFab();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.courtsRecyclerView);
        fab = findViewById(R.id.fab);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("מגרשים");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CourtAdapter(court -> showCourtOptionsDialog(court));
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(CourtViewModel.class);
        viewModel.getCourts().observe(this, courts -> {
            adapter.setCourts(courts);
        });

        viewModel.getErrors().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFab() {
        fab.setOnClickListener(v -> showAddCourtDialog());
    }

    private void showAddCourtDialog() {
        // Create a full-screen dialog instead
        Intent intent = new Intent(this, AddEditCourtActivity.class);
        startActivity(intent);
    }

    private void showCourtOptionsDialog(Court court) {
        String[] options = {"ערוך", "מחק"};
        new AlertDialog.Builder(this)
            .setTitle(court.getName())
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Edit
                        openEditCourtActivity(court);
                        break;
                    case 1: // Delete
                        confirmDelete(court);
                        break;
                }
            })
            .show();
    }
    
    private void openEditCourtActivity(Court court) {
        Intent intent = new Intent(this, AddEditCourtActivity.class);
        intent.putExtra("COURT_ID", court.getCourtId());
        startActivity(intent);
    }

    private void confirmDelete(Court court) {
        new AlertDialog.Builder(this)
            .setTitle("מחיקת מגרש")
            .setMessage("האם אתה בטוח שברצונך למחוק את " + court.getName() + "?")
            .setPositiveButton("מחק", (dialog, which) -> viewModel.deleteCourt(court.getCourtId()))
            .setNegativeButton("ביטול", null)
            .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
