package com.example.water4u;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainMenu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        loadLocale();

        ImageView locationIMG = findViewById(R.id.locationIMG);

        ImageView feedbackIMG = findViewById(R.id.feedbackIMG);
        ImageView addLocationIMG = findViewById(R.id.addLocationIMG);
        ImageView settingsIMG = findViewById(R.id.imageSettings);

        locationIMG.setOnClickListener(view -> {
            Intent mapIntent = new Intent(MainMenu.this, MapsActivity.class);
            mapIntent.putExtra("Add Location",false);
            startActivity(mapIntent);
        });

        feedbackIMG.setOnClickListener(view -> {
            Intent feedbackIntent = new Intent(MainMenu.this, FeedBack.class);
            startActivity(feedbackIntent);
        });

        addLocationIMG.setOnClickListener(view -> {
            Intent mapIntent = new Intent(MainMenu.this, MapsActivity.class);
            mapIntent.putExtra("Add Location",true);
            startActivity(mapIntent);
        });

        settingsIMG.setOnClickListener(view -> {
            Intent settingsIntent = new Intent(MainMenu.this, SettingsActivity.class);
            startActivity(settingsIntent);
        });
    }
    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        Context context = getBaseContext();
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).edit();
        editor.putString("language", languageCode);
        editor.apply();
    }
    private void loadLocale() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String language = prefs.getString("language", "");
        setLocale(language);
    }
}