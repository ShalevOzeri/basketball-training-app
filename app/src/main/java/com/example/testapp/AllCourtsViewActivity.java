package com.example.testapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.testapp.models.Court;
import com.example.testapp.models.Training;
import com.example.testapp.viewmodel.CourtViewModel;
import com.example.testapp.viewmodel.TrainingViewModel;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class AllCourtsViewActivity extends AppCompatActivity {

    private ScrollView scrollView;
    private LinearLayout courtsContainer;
    private MaterialToolbar toolbar;
    private CourtViewModel courtViewModel;
    private TrainingViewModel trainingViewModel;
    
    private List<Court> courts = new ArrayList<>();
    private List<Training> trainings = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_courts_view);

        initializeViews();
        setupToolbar();
        setupViewModels();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        scrollView = findViewById(R.id.scrollView);
        courtsContainer = findViewById(R.id.courtsContainer);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("תצוגת כל המגרשים");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupViewModels() {
        courtViewModel = new ViewModelProvider(this).get(CourtViewModel.class);
        trainingViewModel = new ViewModelProvider(this).get(TrainingViewModel.class);

        courtViewModel.getCourts().observe(this, courtsList -> {
            courts = courtsList;
            updateUI();
        });

        trainingViewModel.getTrainings().observe(this, trainingsList -> {
            trainings = trainingsList;
            updateUI();
        });
    }

    private void updateUI() {
        if (courts.isEmpty()) return;
        
        courtsContainer.removeAllViews();
        
        for (Court court : courts) {
            View courtView = createCourtView(court);
            courtsContainer.addView(courtView);
        }
    }

    private View createCourtView(Court court) {
        LinearLayout courtLayout = new LinearLayout(this);
        courtLayout.setOrientation(LinearLayout.VERTICAL);
        courtLayout.setPadding(16, 16, 16, 16);
        
        // Court name header
        TextView courtName = new TextView(this);
        courtName.setText(court.getName());
        courtName.setTextSize(20);
        courtName.setTextColor(Color.BLACK);
        courtName.setPadding(0, 0, 0, 16);
        courtLayout.addView(courtName);

        // Timeline view
        View timelineView = createTimelineView(court);
        courtLayout.addView(timelineView);
        
        // Divider
        View divider = new View(this);
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 2);
        dividerParams.setMargins(0, 24, 0, 24);
        divider.setLayoutParams(dividerParams);
        divider.setBackgroundColor(Color.LTGRAY);
        courtLayout.addView(divider);
        
        return courtLayout;
    }

    private View createTimelineView(Court court) {
        LinearLayout timeline = new LinearLayout(this);
        timeline.setOrientation(LinearLayout.HORIZONTAL);
        timeline.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 200));
        timeline.setBackgroundColor(Color.parseColor("#F5F5F5"));
        
        // Find trainings for this court
        List<Training> courtTrainings = new ArrayList<>();
        for (Training training : trainings) {
            if (training.getCourtId().equals(court.getCourtId())) {
                courtTrainings.add(training);
            }
        }
        
        if (courtTrainings.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("אין אימונים");
            emptyText.setTextColor(Color.GRAY);
            emptyText.setPadding(16, 80, 16, 80);
            timeline.addView(emptyText);
        } else {
            for (Training training : courtTrainings) {
                View trainingBlock = createTrainingBlock(training);
                timeline.addView(trainingBlock);
            }
        }
        
        return timeline;
    }

    private View createTrainingBlock(Training training) {
        TextView trainingView = new TextView(this);
        trainingView.setText(training.getTeamName() + "\n" + 
                           training.getStartTime() + "-" + training.getEndTime());
        trainingView.setTextColor(Color.WHITE);
        trainingView.setPadding(12, 12, 12, 12);
        trainingView.setTextSize(12);
        
        try {
            trainingView.setBackgroundColor(Color.parseColor(training.getTeamColor()));
        } catch (Exception e) {
            trainingView.setBackgroundColor(Color.parseColor("#3DDC84"));
        }
        
        int durationMinutes = training.getDurationInMinutes();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            durationMinutes * 2, // 2px per minute for visualization
            LinearLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(4, 4, 4, 4);
        trainingView.setLayoutParams(params);
        
        trainingView.setOnClickListener(v -> {
            Toast.makeText(this, 
                training.getTeamName() + "\n" +
                training.getDayOfWeek() + "\n" +
                training.getStartTime() + " - " + training.getEndTime(),
                Toast.LENGTH_LONG).show();
        });
        
        return trainingView;
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
