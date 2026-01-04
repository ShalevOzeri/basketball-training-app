package com.example.testapp.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.R;
import com.example.testapp.models.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddPlayersAdapter extends RecyclerView.Adapter<AddPlayersAdapter.PlayerViewHolder> {

    private List<User> players;
    private Set<String> selectedPlayerIds;

    public AddPlayersAdapter(List<User> players) {
        this.players = players;
        this.selectedPlayerIds = new HashSet<>();
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        android.view.View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_add_player, parent, false);
        return new PlayerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        User player = players.get(position);
        holder.bind(player, selectedPlayerIds);
    }

    @Override
    public int getItemCount() {
        return players.size();
    }

    public List<User> getSelectedPlayers() {
        List<User> selected = new ArrayList<>();
        for (User player : players) {
            if (selectedPlayerIds.contains(player.getUserId())) {
                selected.add(player);
            }
        }
        return selected;
    }

    class PlayerViewHolder extends RecyclerView.ViewHolder {

        private TextView playerName;
        private TextView playerEmail;
        private CheckBox selectCheckBox;

        public PlayerViewHolder(@NonNull android.view.View itemView) {
            super(itemView);
            playerName = itemView.findViewById(R.id.playerName);
            playerEmail = itemView.findViewById(R.id.playerEmail);
            selectCheckBox = itemView.findViewById(R.id.selectCheckBox);
        }

        public void bind(User player, Set<String> selectedPlayerIds) {
            playerName.setText(player.getName());
            playerEmail.setText(player.getEmail());
            
            boolean isSelected = selectedPlayerIds.contains(player.getUserId());
            selectCheckBox.setChecked(isSelected);
            
            selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedPlayerIds.add(player.getUserId());
                } else {
                    selectedPlayerIds.remove(player.getUserId());
                }
            });

            itemView.setOnClickListener(v -> selectCheckBox.toggle());
        }
    }
}
