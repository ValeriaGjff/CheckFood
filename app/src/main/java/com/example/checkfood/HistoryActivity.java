package com.example.checkfood;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> onBackPressed());

        RecyclerView rv = findViewById(R.id.rvHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));

        HistoryDbHelper db = new HistoryDbHelper(this);
        List<HistoryItem> data = db.getAll();

        rv.setAdapter(new HistoryAdapter(data));
    }
}