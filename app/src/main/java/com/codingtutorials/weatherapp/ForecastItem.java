package com.codingtutorials.weatherapp;

public class ForecastItem {
    private String timeOrDay;
    private double temp;
    private double minTemp;
    private double maxTemp;
    private String iconCode;
    private String description;
    private boolean isDaily; // true for daily, false for hourly

    public ForecastItem(String timeOrDay, double temp, double minTemp, double maxTemp, String iconCode, String description, boolean isDaily) {
        this.timeOrDay = timeOrDay;
        this.temp = temp;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.iconCode = iconCode;
        this.description = description;
        this.isDaily = isDaily;
    }

    // --- Getters ---
    public String getTimeOrDay() {
        return timeOrDay;
    }

    public double getTemp() {
        return temp;
    }

    public double getMinTemp() {
        return minTemp;
    }

    public double getMaxTemp() {
        return maxTemp;
    }

    public String getIconCode() {
        return iconCode;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDaily() {
        return isDaily;
    }
}