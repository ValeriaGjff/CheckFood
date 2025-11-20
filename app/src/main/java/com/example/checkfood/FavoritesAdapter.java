package com.example.checkfood;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.Holder> {

    public interface OnRemove {
        void onRemove(String barcode);
    }

    private final List<FavoriteItem> items;
    private final OnRemove onRemove;

    public FavoritesAdapter(List<FavoriteItem> items, OnRemove onRemove) {
        this.items = items;
        this.onRemove = onRemove;
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
        return new Holder(v);
    }

    @Override public void onBindViewHolder(@NonNull Holder h, int pos) {
        FavoriteItem it = items.get(pos);
        h.tvName.setText(it.name);

        // Открыть карточку продукта по тапу
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), ProductInfoActivity.class);
            i.putExtra("barcode", it.barcode);
            v.getContext().startActivity(i);
        });


        h.btnFavToggle.setOnClickListener(v -> {
            onRemove.onRemove(it.barcode);
            items.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos, getItemCount() - pos);
        });
    }

    @Override public int getItemCount() { return items.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvName;
        ImageButton btnFavToggle;
        Holder(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            btnFavToggle = v.findViewById(R.id.btnFavToggle);
        }
    }
}