package com.example.nutritiontracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StatsFoodAdapter extends RecyclerView.Adapter<StatsFoodAdapter.ViewHolder> {

    private List<FoodItem> foodItems;

    public StatsFoodAdapter(List<FoodItem> foodItems) {
        this.foodItems = foodItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stats_food, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodItem foodItem = foodItems.get(position);

        holder.tvFoodName.setText(foodItem.getName());

        StringBuilder details = new StringBuilder();
        details.append("Kalória: ").append(foodItem.getCalories()).append(" kcal");

        if (foodItem.getProtein() > 0) {
            details.append(" | Fehérje: ").append(foodItem.getProtein()).append("g");
        }

        if (foodItem.getFats() > 0) {
            details.append(" | Zsír: ").append(foodItem.getFats()).append("g");
        }

        if (foodItem.getCarbs() > 0) {
            details.append(" | Szénhidrát: ").append(foodItem.getCarbs()).append("g");
        }

        holder.tvFoodDetails.setText(details.toString());
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName;
        TextView tvFoodDetails;

        ViewHolder(View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodDetails = itemView.findViewById(R.id.tvFoodDetails);
        }
    }
}