package com.codingtutorials.weatherapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Switch unitSwitch = findViewById(R.id.unitSwitch);
        SharedPreferences prefs = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE);

        unitSwitch.setChecked(prefs.getBoolean("isMetric", true));

        unitSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("isMetric", isChecked).apply();
        });
    }
}