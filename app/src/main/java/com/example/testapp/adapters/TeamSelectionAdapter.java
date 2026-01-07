package com.example.testapp.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.R;
import com.example.testapp.models.Team;

import java.util.List;

/**
 * Adapter for selecting a team from a list in the training scheduling dialog
 */
public class TeamSelectionAdapter extends RecyclerView.Adapter<TeamSelectionAdapter.TeamViewHolder> {
    
    private List<Team> teams;
    private OnTeamSelectedListener listener;
    private int selectedPosition = -1;
    
    public interface OnTeamSelectedListener {
        void onTeamSelected(Team team);
    }
    
    public TeamSelectionAdapter(List<Team> teams, OnTeamSelectedListener listener) {
        this.teams = teams;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team_selection, parent, false);
        return new TeamViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        Team team = teams.get(position);
        holder.bind(team, position);
    }
    
    @Override
    public int getItemCount() {
        return teams.size();
    }
    
    class TeamViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        View colorIndicator;
        TextView tvTeamName;
        TextView tvAgeGroup;
        RadioButton radioButton;
        
        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardTeam);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            tvAgeGroup = itemView.findViewById(R.id.tvAgeGroup);
            radioButton = itemView.findViewById(R.id.radioButton);
        }
        
        public void bind(Team team, int position) {
            tvTeamName.setText(team.getName());
            tvAgeGroup.setText(team.getAgeGroup());
            
            // Team color indicator
            try {
                colorIndicator.setBackgroundColor(Color.parseColor(team.getColor()));
            } catch (Exception e) {
                colorIndicator.setBackgroundColor(Color.GRAY);
            }
            
            // Selection state
            radioButton.setChecked(position == selectedPosition);
            
            // Click action
            cardView.setOnClickListener(v -> {
                int previousPosition = selectedPosition;
                selectedPosition = getAdapterPosition();
                
                // Update the view state
                notifyItemChanged(previousPosition);
                notifyItemChanged(selectedPosition);
                
                // Invoke listener
                if (listener != null) {
                    listener.onTeamSelected(team);
                }
            });
        }
    }
}
