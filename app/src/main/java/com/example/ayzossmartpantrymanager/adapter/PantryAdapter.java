package com.example.ayzossmartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ayzossmartpantrymanager.R;
import com.example.ayzossmartpantrymanager.model.PantryItem;

import java.util.List;

public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {
    public interface Listener {
        void onEdit(PantryItem item);

        void onDelete(PantryItem item);
    }
    private final List<PantryItem> items;
    private final Listener listener;

    public PantryAdapter(List<PantryItem> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry, parent, false);
        return new ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PantryItem item = items.get(position);
        holder.name.setText(item.getName());

        String qtyText = trimTrailingZero(item.getQuantity())+ (item.getUnit() != null && !item.getUnit().isEmpty()? " " + item.getUnit() : "");
        holder.quantity.setText(qtyText);
        if (item.getExpiryDate() != null && !item.getExpiryDate().isEmpty()) {
            holder.expiry.setText ("Expires: " + item.getExpiryDate());
            holder.expiry.setVisibility(View.VISIBLE);
        } else {
            holder.expiry.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> listener.onEdit(item));
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(item));
    }
    private String trimTrailingZero(double d) {
        if (d == Math.floor(d)) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }
    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, quantity, expiry, deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textItemName);
            quantity = itemView.findViewById(R.id.textItemQuantity);
            expiry = itemView.findViewById(R.id.textItemExpiry);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
        }
    }
}