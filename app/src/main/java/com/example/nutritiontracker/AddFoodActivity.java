package com.example.nutritiontracker;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddFoodActivity extends AppCompatActivity {

    private EditText etFoodName, etCalories, etProtein, etCarbs, etFats;
    private Button btnSaveFood;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        etFoodName = findViewById(R.id.etFoodName);
        etCalories = findViewById(R.id.etCalories);
        etProtein = findViewById(R.id.etProtein);
        etCarbs = findViewById(R.id.etCarbs);
        etFats = findViewById(R.id.etFats);
        btnSaveFood = findViewById(R.id.btnSaveFood);

        db = FirebaseFirestore.getInstance();

        btnSaveFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFoodToFirestore();
            }
        });
    }

    private void saveFoodToFirestore() {
        String foodName = etFoodName.getText().toString().trim();
        String caloriesStr = etCalories.getText().toString().trim();
        String proteinStr = etProtein.getText().toString().trim();
        String carbsStr = etCarbs.getText().toString().trim();
        String fatsStr = etFats.getText().toString().trim();

        if (TextUtils.isEmpty(foodName) || TextUtils.isEmpty(caloriesStr) ||
                TextUtils.isEmpty(proteinStr) || TextUtils.isEmpty(carbsStr) || TextUtils.isEmpty(fatsStr)) {
            Toast.makeText(this, "Minden mezőt ki kell tölteni!", Toast.LENGTH_SHORT).show();
            return;
        }

        int calories = Integer.parseInt(caloriesStr);
        int protein = Integer.parseInt(proteinStr);
        int carbs = Integer.parseInt(carbsStr);
        int fats = Integer.parseInt(fatsStr);

        Map<String, Object> food = new HashMap<>();
        food.put("name", foodName);
        food.put("calories", calories);
        food.put("protein", protein);
        food.put("carbs", carbs);
        food.put("fats", fats);

        db.collection("FoodItems").add(food)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Étel mentve!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba történt!", Toast.LENGTH_SHORT).show();
                });
    }
}