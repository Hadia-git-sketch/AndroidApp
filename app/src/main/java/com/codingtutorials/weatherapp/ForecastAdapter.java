package com.codingtutorials.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private final Context context;
    private final List<ForecastItem> forecastList;
    private final boolean isDaily;

    public ForecastAdapter(Context context, List<ForecastItem> forecastList, boolean isDaily) {
        this.context = context;
        this.forecastList = forecastList;
        this.isDaily = isDaily;
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isDaily ? R.layout.item_daily_forecast : R.layout.item_hourly_forecast;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        ForecastItem item = forecastList.get(position);

        holder.timeOrDayText.setText(item.getTimeOrDay());

        // Set the appropriate layout data (Temp vs Min/Max)
        if (isDaily) {
            // Daily forecast shows Min/Max temperature
            holder.dailyTempRange.setText(String.format(Locale.getDefault(), "%.0f°/%.0f°", item.getMaxTemp(), item.getMinTemp()));
            holder.hourlyTemp.setVisibility(View.GONE); // Hide the hourly temp view
            holder.dailyTempRange.setVisibility(View.VISIBLE); // Show the daily range
        } else {
            // Hourly forecast shows a single temperature
            holder.hourlyTemp.setText(String.format(Locale.getDefault(), "%.0f°", item.getTemp()));
            holder.hourlyTemp.setVisibility(View.VISIBLE); // Show the hourly temp view
            holder.dailyTempRange.setVisibility(View.GONE); // Hide the daily range view
        }

        // Set weather icon
        String resourceName = "ic_" + item.getIconCode();
        int resId = context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());

        if (resId != 0) {
            holder.icon.setImageResource(resId);
        } else {
            // Fallback icon if the specific one is not found
            holder.icon.setImageResource(R.drawable.ic_01d);
        }
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }

    public static class ForecastViewHolder extends RecyclerView.ViewHolder {
        TextView timeOrDayText;
        ImageView icon;
        TextView hourlyTemp; // Used for hourly (single temp)
        TextView dailyTempRange; // Used for daily (min/max temp)

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            timeOrDayText = itemView.findViewById(R.id.timeOrDayText);
            icon = itemView.findViewById(R.id.forecastIcon);
            hourlyTemp = itemView.findViewById(R.id.hourlyTempText);
            dailyTempRange = itemView.findViewById(R.id.dailyTempRangeText);
        }
    }
}