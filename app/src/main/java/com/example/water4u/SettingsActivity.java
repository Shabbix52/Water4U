package com.example.water4u;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.material.chip.Chip;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {
    Chip englishChip,urduChip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        englishChip = findViewById(R.id.chipEnglish);
        urduChip = findViewById(R.id.chipUrdu);

        loadLocale();

        englishChip.setOnClickListener(view -> {
            urduChip.setChecked(false);
            setLocale("en");
            restartActivity();
        });
        urduChip.setOnClickListener(view -> {
            englishChip.setChecked(false);
            setLocale("ur");
            restartActivity();
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

        if(language.equals("en")){
            englishChip.setChecked(true);
        } else {
            urduChip.setChecked(true);
        }
    }
    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}