package com.example.checkfood;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Date;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.Holder> {

    private final List<HistoryItem> items;
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd.MM.yy, HH:mm", Locale.getDefault());

    public HistoryAdapter(List<HistoryItem> items) {
        this.items = items;
    }

    @NonNull @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new Holder(v);
    }

    @Override public void onBindViewHolder(@NonNull Holder h, int pos) {
        HistoryItem it = items.get(pos);
        h.tvName.setText(it.name);
        h.tvDate.setText(fmt.format(new Date(it.timestamp)));

        if (it.safe) {
            h.badgeContainer.setBackgroundResource(R.drawable.bg_badge_safe);
            h.tvBadge.setText("Безопасно");
            h.tvBadge.setTextColor(h.itemView.getResources().getColor(R.color.cf_green));
            h.ivBadge.setImageResource(R.drawable.ic_check_24);
            h.ivBadge.setColorFilter(h.itemView.getResources().getColor(R.color.cf_green));
        } else {
            h.badgeContainer.setBackgroundResource(R.drawable.bg_badge_danger);
            h.tvBadge.setText("Обнаружены\nвредные вещества");
            h.tvBadge.setTextColor(h.itemView.getResources().getColor(R.color.cf_orange));
            h.ivBadge.setImageResource(R.drawable.ic_warning_24);
            h.ivBadge.setColorFilter(h.itemView.getResources().getColor(R.color.cf_orange));
        }
    }

    @Override public int getItemCount() { return items.size(); }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvName, tvDate, tvBadge;
        ImageView ivBadge;
        LinearLayout badgeContainer;
        Holder(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvDate = v.findViewById(R.id.tvDate);
            tvBadge = v.findViewById(R.id.tvBadge);
            ivBadge = v.findViewById(R.id.ivBadgeIcon);
            badgeContainer = v.findViewById(R.id.badgeContainer);
        }
    }
}