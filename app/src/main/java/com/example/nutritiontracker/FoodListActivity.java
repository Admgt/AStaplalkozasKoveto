package com.example.nutritiontracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FoodListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FoodAdapter adapter;
    private List<FoodItem> foodList;
    private List<String> foodIds;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private String currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            startActivity(new Intent(FoodListActivity.this, LoginActivity.class));
            finish();
            return;
        }

        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        foodList = new ArrayList<>();
        foodIds = new ArrayList<>();
        adapter = new FoodAdapter(
                foodList,
                food -> addFoodToConsumption(food),
                (food, position) -> showEditFoodDialog(food, foodIds.get(position)),
                (food, position) -> confirmDeleteFood(foodIds.get(position))
        );
        recyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_food_list);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(FoodListActivity.this, MainActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_food_list) {
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(FoodListActivity.this, ProfileActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });

        loadFoodItems();
    }

    private void loadFoodItems() {
        db.collection("FoodItems").addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.e("Firestore", "Hiba: ", e);
                Toast.makeText(FoodListActivity.this, "Hiba az adatok lekérésekor", Toast.LENGTH_SHORT).show();
                return;
            }
            foodList.clear();
            foodIds.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                FoodItem food = doc.toObject(FoodItem.class);
                foodList.add(food);
                foodIds.add(doc.getId());
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void addFoodToConsumption(FoodItem food) {
        String userId = user.getUid();

        String foodEntryId = db.collection("users").document(userId)
                .collection("dailyConsumption").document(currentDate)
                .collection("consumedFoods").document().getId();


        db.collection("users").document(userId)
                .collection("dailyConsumption").document(currentDate)
                .collection("consumedFoods").document(foodEntryId)
                .set(food)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(FoodListActivity.this, food.getName() + " hozzáadva", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(FoodListActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Hiba az étel hozzáadásakor", e);
                    Toast.makeText(FoodListActivity.this, "Hiba az étel hozzáadásakor", Toast.LENGTH_SHORT).show();
                });
    }

    private void showEditFoodDialog(FoodItem food, String foodId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_food, null);

        EditText etName = dialogView.findViewById(R.id.etFoodName);
        EditText etCalories = dialogView.findViewById(R.id.etCalories);
        EditText etProtein = dialogView.findViewById(R.id.etProtein);
        EditText etCarbs = dialogView.findViewById(R.id.etCarbs);
        EditText etFats = dialogView.findViewById(R.id.etFats);

        etName.setText(food.getName());
        etCalories.setText(String.valueOf(food.getCalories()));
        etProtein.setText(String.valueOf(food.getProtein()));
        etCarbs.setText(String.valueOf(food.getCarbs()));
        etFats.setText(String.valueOf(food.getFats()));

        builder.setView(dialogView)
                .setTitle("Étel szerkesztése")
                .setPositiveButton("Mentés", (dialog, id) -> {
                    if (etName.getText().toString().trim().isEmpty() ||
                            etCalories.getText().toString().trim().isEmpty()) {
                        Toast.makeText(this, "Kérlek töltsd ki a kötelező mezőket", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("name", etName.getText().toString().trim());
                    updates.put("calories", Integer.parseInt(etCalories.getText().toString().trim()));

                    if (!etProtein.getText().toString().isEmpty()) {
                        updates.put("protein", Double.parseDouble(etProtein.getText().toString().trim()));
                    }

                    if (!etCarbs.getText().toString().isEmpty()) {
                        updates.put("carbs", Double.parseDouble(etCarbs.getText().toString().trim()));
                    }

                    if (!etFats.getText().toString().isEmpty()) {
                        updates.put("fats", Double.parseDouble(etFats.getText().toString().trim()));
                    }

                    db.collection("FoodItems").document(foodId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(FoodListActivity.this, "Étel sikeresen frissítve", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(FoodListActivity.this, "Hiba az étel frissítése közben", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Mégse", (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void confirmDeleteFood(String foodId) {
        new AlertDialog.Builder(this)
                .setTitle("Étel törlése")
                .setMessage("Biztosan törölni szeretnéd ezt az ételt?")
                .setPositiveButton("Igen", (dialog, which) -> {
                    db.collection("FoodItems").document(foodId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(FoodListActivity.this, "Étel sikeresen törölve", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(FoodListActivity.this, "Hiba az étel törlése közben", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Nem", null)
                .show();
    }
}