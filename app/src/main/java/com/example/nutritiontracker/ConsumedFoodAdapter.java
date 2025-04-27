package com.example.nutritiontracker;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConsumedFoodAdapter extends RecyclerView.Adapter<ConsumedFoodAdapter.ConsumedFoodViewHolder> {

    private List<FoodItem> consumedFoods;

    public ConsumedFoodAdapter(List<FoodItem> consumedFoods, List<String> documentIds) {
        this.consumedFoods = consumedFoods;
    }

    @NonNull
    @Override
    public ConsumedFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_consumed_food, parent, false);
        return new ConsumedFoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConsumedFoodViewHolder holder, int position) {
        FoodItem food = consumedFoods.get(position);
        holder.bind(food);

        holder.btnDeleteFood.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                deleteFromFirebase(consumedFoods.get(adapterPosition), v);
            }
        });
    }

    private void deleteFromFirebase(FoodItem food, View view) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null || food.getId() == null) {
            Log.e("FoodAdapter", "User is null or food ID is null");
            return;
        }

        String userId = user.getUid();
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Log.d("FoodAdapter", "Deleting food: " + food.getName() + " with ID: " + food.getId());

        db.collection("users")
                .document(userId)
                .collection("dailyConsumption")
                .document(currentDate)
                .collection("consumedFoods")
                .document(food.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("FoodAdapter", "Document successfully deleted");
                    Toast.makeText(view.getContext(), "Étel törölve", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("FoodAdapter", "Error deleting document", e);
                    Toast.makeText(view.getContext(), "Hiba a törlés során: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return consumedFoods.size();
    }

    class ConsumedFoodViewHolder extends RecyclerView.ViewHolder {

        TextView tvConsumedFoodName;
        ImageButton btnDeleteFood;
        public ConsumedFoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvConsumedFoodName = itemView.findViewById(R.id.tvConsumedFoodName);
            btnDeleteFood = itemView.findViewById(R.id.btnDeleteFood);
        }

        public void bind(FoodItem food) {
            tvConsumedFoodName.setText(food.getName());
        }
    }
}

