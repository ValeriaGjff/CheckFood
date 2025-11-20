package com.example.checkfood;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.datatransport.backend.cct.BuildConfig;
import com.google.android.material.appbar.MaterialToolbar;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        MaterialToolbar toolbar = findViewById(R.id.toolbarAbout);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        TextView tvVersion = findViewById(R.id.tvVersion);
        Button btnPrivacy  = findViewById(R.id.btnPrivacy);
        Button btnRate     = findViewById(R.id.btnRate);

        // Подставляем фактическую версию из BuildConfig
        String versionText = "Версия приложения: " + BuildConfig.VERSION_NAME;
        tvVersion.setText(versionText);

        // Политика конфиденциальности
        btnPrivacy.setOnClickListener(v -> {

            String url = "https://example.com/privacy";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        // Оценить приложение
        btnRate.setOnClickListener(v -> {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {

                Uri webUri = Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName());
                startActivity(new Intent(Intent.ACTION_VIEW, webUri));
            }
        });
    }
}