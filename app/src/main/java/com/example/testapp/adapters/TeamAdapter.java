package com.example.testapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.R;
import com.example.testapp.models.Team;

import java.util.ArrayList;
import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

    private List<Team> teams = new ArrayList<>();
    private final OnTeamClickListener listener;

    public interface OnTeamClickListener {
        void onTeamClick(Team team);
    }

    public TeamAdapter(OnTeamClickListener listener) {
        this.listener = listener;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        Team team = teams.get(position);
        holder.bind(team, listener);
    }

    @Override
    public int getItemCount() {
        return teams.size();
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView teamName, teamAgeGroup, teamLevel, teamCoach;
        CardView cardView;
        View colorIndicator;

        TeamViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            teamName = itemView.findViewById(R.id.teamName);
            teamAgeGroup = itemView.findViewById(R.id.teamAgeGroup);
            teamLevel = itemView.findViewById(R.id.teamLevel);
            teamCoach = itemView.findViewById(R.id.teamCoach);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
        }

        void bind(Team team, OnTeamClickListener listener) {
            teamName.setText(team.getName());
            teamAgeGroup.setText(team.getAgeGroup());
            teamLevel.setText(team.getLevel());
            teamCoach.setText("מאמן: " + team.getCoachName());
            
            try {
                colorIndicator.setBackgroundColor(Color.parseColor(team.getColor()));
            } catch (Exception e) {
                colorIndicator.setBackgroundColor(Color.parseColor("#3DDC84"));
            }

            itemView.setOnClickListener(v -> listener.onTeamClick(team));
        }
    }
}
