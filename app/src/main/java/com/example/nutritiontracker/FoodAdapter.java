package com.example.nutritiontracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    private List<FoodItem> foodList;
    private OnFoodClickListener listener;
    private OnFoodEditListener editListener;
    private OnFoodDeleteListener deleteListener;

    public interface OnFoodClickListener {
        void onFoodClick(FoodItem food);
    }

    public interface OnFoodEditListener {
        void onFoodEdit(FoodItem food, int position);
    }

    public interface OnFoodDeleteListener {
        void onFoodDelete(FoodItem food, int position);
    }

    public FoodAdapter(List<FoodItem> foodList, OnFoodClickListener listener, OnFoodEditListener editListener, OnFoodDeleteListener deleteListener) {
        this.foodList = foodList;
        this.listener = listener;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem food = foodList.get(position);
        holder.bind(food, position);
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvFoodName, tvCalories, tvNutrients;
        ImageButton btnEdit, btnDelete;
        Button btnAdd;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvCalories = itemView.findViewById(R.id.tvCalories);
            tvNutrients = itemView.findViewById(R.id.tvNutrients);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }

        void bind(final FoodItem food, final int position) {
            tvFoodName.setText(food.getName());
            tvCalories.setText(food.getCalories() + " kcal");
            tvNutrients.setText(String.format("P: %.1fg | C: %.1fg | F: %.1fg",
                    food.getProtein(), food.getCarbs(), food.getFats()));

            btnEdit.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onFoodEdit(food, position);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onFoodDelete(food, position);
                }
            });

            btnAdd.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFoodClick(food);
                }
            });
        }
    }
}
