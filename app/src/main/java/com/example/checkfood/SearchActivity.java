package com.example.checkfood;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        MaterialToolbar toolbar = findViewById(R.id.toolbarSearch);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        EditText etBarcode = findViewById(R.id.etBarcodeSearch);
        Button btnSearch = findViewById(R.id.btnSearch);

        btnSearch.setOnClickListener(v -> {
            String code = etBarcode.getText().toString().trim();

            if (TextUtils.isEmpty(code)) {
                Toast.makeText(this, "Введите штрих-код", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent i = new Intent(this, ProductInfoActivity.class);
            i.putExtra("barcode", code);
            startActivity(i);
        });
    }
}