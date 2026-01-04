package com.example.testapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.R;
import com.example.testapp.models.Team;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectableTeamAdapter extends RecyclerView.Adapter<SelectableTeamAdapter.TeamViewHolder> {

    private List<Team> teams = new ArrayList<>();
    private Set<String> selectedTeamIds = new HashSet<>();

    public void setTeams(List<Team> teams) {
        this.teams = teams;
        notifyDataSetChanged();
    }

    public Set<String> getSelectedTeamIds() {
        return selectedTeamIds;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team_selectable, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        Team team = teams.get(position);
        holder.bind(team, selectedTeamIds);
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView teamName, teamAgeGroup, teamCoach;
        CheckBox teamCheckbox;
        View colorIndicator;

        TeamViewHolder(View itemView) {
            super(itemView);
            teamName = itemView.findViewById(R.id.teamName);
            teamAgeGroup = itemView.findViewById(R.id.teamAgeGroup);
            teamCoach = itemView.findViewById(R.id.teamCoach);
            teamCheckbox = itemView.findViewById(R.id.teamCheckbox);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
        }

        void bind(Team team, Set<String> selectedIds) {
            teamName.setText(team.getName());
            teamAgeGroup.setText("שכבת גיל " + team.getAgeGroup());
            teamCoach.setText("מאמן: " + team.getCoachName());
            
            try {
                colorIndicator.setBackgroundColor(Color.parseColor(team.getColor()));
            } catch (Exception e) {
                colorIndicator.setBackgroundColor(Color.parseColor("#3DDC84"));
            }

            teamCheckbox.setChecked(selectedIds.contains(team.getTeamId()));
            
            itemView.setOnClickListener(v -> {
                boolean isChecked = !teamCheckbox.isChecked();
                teamCheckbox.setChecked(isChecked);
                
                if (isChecked) {
                    selectedIds.add(team.getTeamId());
                } else {
                    selectedIds.remove(team.getTeamId());
                }
            });
            
            teamCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedIds.add(team.getTeamId());
                } else {
                    selectedIds.remove(team.getTeamId());
                }
            });
        }
    }
}
