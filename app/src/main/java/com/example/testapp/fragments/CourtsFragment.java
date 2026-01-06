package com.example.testapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.AddEditCourtActivity;
import com.example.testapp.R;
import com.example.testapp.adapters.CourtAdapter;
import com.example.testapp.models.Court;
import com.example.testapp.viewmodel.CourtViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class CourtsFragment extends Fragment {

    private RecyclerView recyclerView;
    private CourtAdapter adapter;
    private CourtViewModel viewModel;
    private FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_courts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupViewModel();
        setupFab();
    }

    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.courtsRecyclerView);
        fab = view.findViewById(R.id.fab);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CourtAdapter(court -> showCourtOptionsDialog(court));
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(CourtViewModel.class);
        viewModel.getCourts().observe(getViewLifecycleOwner(), courts -> {
            adapter.setCourts(courts);
        });

        viewModel.getErrors().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFab() {
        fab.setOnClickListener(v -> showAddCourtDialog());
    }

    private void showAddCourtDialog() {
        Intent intent = new Intent(requireActivity(), AddEditCourtActivity.class);
        startActivity(intent);
    }

    private void showCourtOptionsDialog(Court court) {
        String[] options = {"ערוך", "מחק"};
        new AlertDialog.Builder(requireContext())
            .setTitle(court.getName())
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        openEditCourtActivity(court);
                        break;
                    case 1:
                        confirmDelete(court);
                        break;
                }
            })
            .show();
    }
    
    private void openEditCourtActivity(Court court) {
        Intent intent = new Intent(requireActivity(), AddEditCourtActivity.class);
        intent.putExtra("COURT_ID", court.getCourtId());
        startActivity(intent);
    }

    private void confirmDelete(Court court) {
        new AlertDialog.Builder(requireContext())
            .setTitle("מחיקת מגרש")
            .setMessage("האם אתה בטוח שברצונך למחוק את " + court.getName() + "?")
            .setPositiveButton("מחק", (dialog, which) -> viewModel.deleteCourt(court.getCourtId()))
            .setNegativeButton("ביטול", null)
            .show();
    }
}
