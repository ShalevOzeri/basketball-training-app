package com.example.testapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.R;
import com.example.testapp.models.Player;

import java.util.ArrayList;
import java.util.List;

public class TeamPlayersAdapter extends RecyclerView.Adapter<TeamPlayersAdapter.PlayerViewHolder> {

    private List<Player> players = new ArrayList<>();
    private final OnPlayerEditListener listener;

    public interface OnPlayerEditListener {
        void onEditPlayer(Player player);
        void onDeletePlayer(Player player);
    }

    public TeamPlayersAdapter(OnPlayerEditListener listener) {
        this.listener = listener;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_team_player, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Player player = players.get(position);
        holder.bind(player, listener);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    static class PlayerViewHolder extends RecyclerView.ViewHolder {
        TextView playerName, playerPhone, playerInfo;
        Button editButton, deleteButton;

        PlayerViewHolder(View itemView) {
            super(itemView);
            playerName = itemView.findViewById(R.id.playerName);
            playerPhone = itemView.findViewById(R.id.playerPhone);
            playerInfo = itemView.findViewById(R.id.playerInfo);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        void bind(Player player, OnPlayerEditListener listener) {
            playerName.setText(player.getFirstName() + " " + player.getLastName());
            playerPhone.setText(player.getPlayerPhone() != null ? player.getPlayerPhone() : "אין טלפון");
            playerInfo.setText("כיתה: " + player.getGrade() + " | בית ספר: " + (player.getSchool() != null ? player.getSchool() : "לא צויין"));

            editButton.setOnClickListener(v -> listener.onEditPlayer(player));
            deleteButton.setOnClickListener(v -> listener.onDeletePlayer(player));
        }
    }
}
