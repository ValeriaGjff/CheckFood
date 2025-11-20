package com.example.checkfood;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbarSettings);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        LinearLayout rowLanguage = findViewById(R.id.rowLanguage);
        LinearLayout rowAbout    = findViewById(R.id.rowAbout);

        rowAbout.setOnClickListener(v -> {
            startActivity(new Intent(this, AboutActivity.class));
        });

        rowLanguage.setOnClickListener(v -> showLanguageDialog());
    }


    private void showLanguageDialog() {
        String[] langs = {"Русский", "English"};

        new AlertDialog.Builder(this)
                .setTitle("Выберите язык")
                .setItems(langs, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            setLocale("ru");
                            break;
                        case 1:
                            setLocale("en");
                            break;
                    }
                })
                .show();
    }


    private void setLocale(String langCode) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        prefs.edit()
                .putString("app_lang", langCode)
                .apply();

        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        finish();
    }
}