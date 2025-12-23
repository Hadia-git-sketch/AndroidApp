package com.codingtutorials.weatherapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ManageLocationsActivity extends AppCompatActivity {
    private RecyclerView locationsRecyclerView;
    private LocationAdapter adapter;
    private ArrayList<String> savedCities;
    private AutoCompleteTextView cityNameInput;
    private ImageView searchIcon;
    private ArrayAdapter<String> suggestionAdapter;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final String API_KEY = "7be4a25466b8361c2ae28097a6aa5617";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_locations);

        cityNameInput = findViewById(R.id.cityNameInput);
        searchIcon = findViewById(R.id.searchIcon);
        locationsRecyclerView = findViewById(R.id.locationsRecyclerView);

        loadCities();

        adapter = new LocationAdapter(this, savedCities);
        locationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        locationsRecyclerView.setAdapter(adapter);

        // FIX: DELETE & SELECTION
        adapter.setOnItemClickListener(new LocationAdapter.OnItemClickListener() {
            @Override
            public void onCitySelected(String cityName) {
                returnToMain(cityName);
            }

            @Override
            public void onCityDeleted(int position) {
                if (position > 0) { // Keep Current Location
                    savedCities.remove(position);
                    adapter.notifyItemRemoved(position);
                    saveCitiesToPrefs();
                }
            }
        });

        suggestionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        cityNameInput.setAdapter(suggestionAdapter);

        // FIX: PAKISTAN SUGGESTIONS
        cityNameInput.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 2) fetchPakistanCities(s.toString());
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        // FIX: SEARCH ICON CRASH PROTECTION
        searchIcon.setOnClickListener(v -> {
            String city = cityNameInput.getText().toString().trim();
            if (!city.isEmpty()) {
                saveAndGoHome(city);
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            }
        });

        cityNameInput.setOnItemClickListener((parent, view, position, id) -> {
            saveAndGoHome(suggestionAdapter.getItem(position));
        });
    }

    private void fetchPakistanCities(String query) {
        String url = "https://api.openweathermap.org/geo/1.0/direct?q=" + query + ",PK&limit=5&appid=" + API_KEY;
        executorService.execute(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    JSONArray jsonArray = new JSONArray(response.body().string());
                    ArrayList<String> list = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        list.add(obj.getString("name") + ", " + obj.optString("state", "PK"));
                    }
                    runOnUiThread(() -> {
                        suggestionAdapter.clear();
                        suggestionAdapter.addAll(list);
                        suggestionAdapter.notifyDataSetChanged();
                    });
                }
            } catch (Exception e) { e.printStackTrace(); }
        });
    }

    private void saveAndGoHome(String city) {
        if (!savedCities.contains(city)) {
            savedCities.add(city);
            saveCitiesToPrefs();
        }
        returnToMain(city);
    }

    private void returnToMain(String city) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("selected_city", city);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void loadCities() {
        SharedPreferences prefs = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE);
        Set<String> set = prefs.getStringSet("SavedCities", new HashSet<>());
        savedCities = new ArrayList<>(set);
        if (!savedCities.contains("Current Location")) savedCities.add(0, "Current Location");
    }

    private void saveCitiesToPrefs() {
        SharedPreferences prefs = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE);
        Set<String> set = new HashSet<>(savedCities);
        set.remove("Current Location");
        prefs.edit().putStringSet("SavedCities", set).apply();
    }
}