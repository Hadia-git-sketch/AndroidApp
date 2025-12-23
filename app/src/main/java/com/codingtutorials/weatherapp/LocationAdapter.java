package com.codingtutorials.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
    private final ArrayList<String> cities;
    private final Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onCitySelected(String cityName);
        void onCityDeleted(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public LocationAdapter(Context context, ArrayList<String> cities) {
        this.context = context;
        this.cities = cities;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        String cityName = cities.get(position);
        holder.cityName.setText(cityName);
        holder.deleteIcon.setVisibility(position == 0 ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCitySelected(cityName);
        });

        holder.deleteIcon.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != 0) {
                listener.onCityDeleted(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() { return cities.size(); }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView cityName;
        ImageView deleteIcon;
        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            cityName = itemView.findViewById(R.id.locationName);
            deleteIcon = itemView.findViewById(R.id.deleteIcon);
        }
    }
}