package com.codingtutorials.weatherapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView cityNameText, temperatureText, humidityText, descriptionText, windText;
    private ImageView weatherIcon, addLocationIcon, settingsIcon;

    private RecyclerView hourlyRecyclerView, dailyRecyclerView;
    private ForecastAdapter hourlyAdapter, dailyAdapter;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private SharedPreferences sharedPreferences;
    private boolean isMetric = true;

    private static final String API_KEY = "7be4a25466b8361c2ae28097a6aa5617";
    private static final String PREFS_NAME = "WeatherAppPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        loadSettings();

        // 1. Initialize UI Elements
        cityNameText = findViewById(R.id.cityNameText);
        temperatureText = findViewById(R.id.temperatureText);
        humidityText = findViewById(R.id.humidityText);
        windText = findViewById(R.id.windText);
        descriptionText = findViewById(R.id.descriptionText);
        weatherIcon = findViewById(R.id.weatherIcon);
        addLocationIcon = findViewById(R.id.addLocationIcon);
        settingsIcon = findViewById(R.id.settingsIcon);

        // 2. Initialize RecyclerVews
        hourlyRecyclerView = findViewById(R.id.hourlyRecyclerView);
        dailyRecyclerView = findViewById(R.id.dailyRecyclerView);
        hourlyRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        dailyRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        // 3. Initialize Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 4. Click Listeners
        addLocationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ManageLocationsActivity.class);
            startActivity(intent);
        });

        settingsIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // 5. Handle initial weather loading or search results
        handleIncomingIntent(getIntent());
    }

    private void loadSettings() {
        isMetric = sharedPreferences.getBoolean("isMetric", true);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
    }

    // COMPLETE LOGIC FOR SEARCH RESULTS AND CURRENT LOCATION
    private void handleIncomingIntent(Intent intent) {
        if (intent != null && intent.hasExtra("selected_city")) {
            String selectedCity = intent.getStringExtra("selected_city");
            if (selectedCity != null) {
                if (selectedCity.equals("Current Location")) {
                    checkLocationPermission();
                } else {
                    FetchWeatherData(selectedCity);
                }
            }
        } else {
            checkLocationPermission();
        }
    }

    // --- LOCATION HANDLING ---

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchLocationAndWeather();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndWeather();
            } else {
                FetchWeatherData("Islamabad"); // Fallback for Pakistan
            }
        }
    }

    private void fetchLocationAndWeather() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                FetchWeatherData(location.getLatitude(), location.getLongitude());
            } else {
                FetchWeatherData("Islamabad");
            }
        });
    }

    // --- API CALLS ---

    private String getUnitsQuery() {
        return isMetric ? "&units=metric" : "&units=imperial";
    }

    private void FetchWeatherData(String cityName) {
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + API_KEY + getUnitsQuery();
        executeApiCall(url);
    }

    private void FetchWeatherData(double lat, double lon) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY + getUnitsQuery();
        executeApiCall(url);
    }

    private void executeApiCall(String url) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            try {
                Response response = client.newCall(request).execute();
                String result = response.body().string();
                if (result != null) {
                    JSONObject jsonObject = new JSONObject(result);
                    double lat = jsonObject.getJSONObject("coord").getDouble("lat");
                    double lon = jsonObject.getJSONObject("coord").getDouble("lon");
                    runOnUiThread(() -> {
                        updateUI(result);
                        FetchForecastData(lat, lon);
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    private void FetchForecastData(double lat, double lon) {
        String url = "https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY + getUnitsQuery();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            try {
                Response response = client.newCall(request).execute();
                String result = response.body().string();
                runOnUiThread(() -> parseForecast(result));
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    private void parseForecast(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            List<ForecastItem> hourlyList = new ArrayList<>();
            List<ForecastItem> dailyList = new ArrayList<>();
            Set<String> processedDays = new HashSet<>();
            JSONArray forecastList = jsonObject.getJSONArray("list");

            for (int i = 0; i < forecastList.length(); i++) {
                JSONObject item = forecastList.getJSONObject(i);
                long dt = item.getLong("dt") * 1000;
                Date date = new Date(dt);
                String dayOfWeek = new SimpleDateFormat("EEEE", Locale.getDefault()).format(date);
                JSONObject main = item.getJSONObject("main");
                String iconCode = item.getJSONArray("weather").getJSONObject(0).getString("icon");

                if (i < 8) {
                    String time = new SimpleDateFormat("h a", Locale.getDefault()).format(date);
                    hourlyList.add(new ForecastItem(time, main.getDouble("temp"), 0, 0, iconCode, "", false));
                }

                String today = new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date());
                if (!processedDays.contains(dayOfWeek) && !dayOfWeek.equals(today)) {
                    dailyList.add(new ForecastItem(dayOfWeek, main.getDouble("temp"), main.getDouble("temp_min"), main.getDouble("temp_max"), iconCode, "", true));
                    processedDays.add(dayOfWeek);
                }
            }

            hourlyRecyclerView.setAdapter(new ForecastAdapter(this, hourlyList, false));
            dailyRecyclerView.setAdapter(new ForecastAdapter(this, dailyList, true));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateUI(String result) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            JSONObject main = jsonObject.getJSONObject("main");
            String tempUnit = isMetric ? "°C" : "°F";
            String speedUnit = isMetric ? " km/h" : " mph";

            cityNameText.setText(jsonObject.getString("name"));
            temperatureText.setText(String.format(Locale.getDefault(), "%.0f%s", main.getDouble("temp"), tempUnit));
            humidityText.setText(String.format(Locale.getDefault(), "%.0f%%", main.getDouble("humidity")));
            windText.setText(String.format(Locale.getDefault(), "%.0f%s", jsonObject.getJSONObject("wind").getDouble("speed"), speedUnit));
            descriptionText.setText(jsonObject.getJSONArray("weather").getJSONObject(0).getString("description").toUpperCase());

            String iconCode = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");
            int resId = getResources().getIdentifier("ic_" + iconCode, "drawable", getPackageName());
            if (resId != 0) weatherIcon.setImageResource(resId);
        } catch (Exception e) { e.printStackTrace(); }
    }
}