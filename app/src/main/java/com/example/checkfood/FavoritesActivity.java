package com.example.checkfood;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private FavoritesDbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        MaterialToolbar tb = findViewById(R.id.toolbar);
        tb.setNavigationOnClickListener(v -> onBackPressed());

        RecyclerView rv = findViewById(R.id.rvFavorites);
        rv.setLayoutManager(new LinearLayoutManager(this));

        db = new FavoritesDbHelper(this);
        List<FavoriteItem> data = db.getAll();

        FavoritesAdapter adapter = new FavoritesAdapter(data, barcode -> db.remove(barcode));
        rv.setAdapter(adapter);
    }
}