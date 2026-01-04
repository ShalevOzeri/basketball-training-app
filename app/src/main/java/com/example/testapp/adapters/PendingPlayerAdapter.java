package com.example.testapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapp.R;
import com.example.testapp.models.PendingRequest;
import com.example.testapp.models.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PendingPlayerAdapter extends RecyclerView.Adapter<PendingPlayerAdapter.PendingPlayerViewHolder> {

    private List<PendingRequest> pendingRequests = new ArrayList<>();
    private Map<String, String> teamNamesMap; // teamId -> teamName
    private final OnPlayerActionListener listener;

    public interface OnPlayerActionListener {
        void onApprove(PendingRequest request);
        void onReject(PendingRequest request);
    }

    public PendingPlayerAdapter(Map<String, String> teamNamesMap, OnPlayerActionListener listener) {
        this.teamNamesMap = teamNamesMap;
        this.listener = listener;
    }

    public void setPendingRequests(List<PendingRequest> requests) {
        this.pendingRequests = requests;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PendingPlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_player, parent, false);
        return new PendingPlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingPlayerViewHolder holder, int position) {
        PendingRequest request = pendingRequests.get(position);
        holder.bind(request, teamNamesMap, listener);
    }

    @Override
    public int getItemCount() {
        return pendingRequests.size();
    }

    static class PendingPlayerViewHolder extends RecyclerView.ViewHolder {
        TextView playerName, playerEmail, playerPhone, pendingTeamName;
        Button approveButton, rejectButton;

        PendingPlayerViewHolder(View itemView) {
            super(itemView);
            playerName = itemView.findViewById(R.id.playerName);
            playerEmail = itemView.findViewById(R.id.playerEmail);
            playerPhone = itemView.findViewById(R.id.playerPhone);
            pendingTeamName = itemView.findViewById(R.id.pendingTeamName);
            approveButton = itemView.findViewById(R.id.approveButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }

        void bind(PendingRequest request, Map<String, String> teamNamesMap, OnPlayerActionListener listener) {
            User player = request.getUser();
            playerName.setText(player.getName());
            playerEmail.setText(player.getEmail());
            playerPhone.setText(player.getPhone() != null ? player.getPhone() : "אין טלפון");
            
            // Show only the specific team for this request
            String teamName = teamNamesMap.get(request.getTeamId());
            pendingTeamName.setText("מבקש להירשם ל: " + 
                (teamName != null ? teamName : "קבוצה לא ידועה"));

            approveButton.setOnClickListener(v -> listener.onApprove(request));
            rejectButton.setOnClickListener(v -> listener.onReject(request));
        }
    }
}
