package com.example.checkfood;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String lang = prefs.getString("app_lang", "ru");

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}