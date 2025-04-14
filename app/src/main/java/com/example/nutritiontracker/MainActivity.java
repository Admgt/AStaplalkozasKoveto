package com.example.nutritiontracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_FOOD = 1;
    private TextView tvCalories, tvProtein, tvCarbs, tvFats;
    private TextView tvWarning;
    private ProgressBar progressCalories;
    private FloatingActionButton btnAddFood;
    private FirebaseFirestore db;

    private int dailyCaloriesGoal = 2000;
    private int consumedCalories = 0;
    private double consumedProtein = 0.0;
    private double consumedCarbs = 0.0;
    private double consumedFats = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSharedPreferences("nutrition_data", MODE_PRIVATE).edit().clear().apply();

        db = FirebaseFirestore.getInstance();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    return true;
                } else if (itemId == R.id.nav_food_list) {
                    Intent intent = new Intent(MainActivity.this, FoodListActivity.class);
                    startActivityForResult(intent, REQUEST_CODE_FOOD);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });

        tvCalories = findViewById(R.id.tvCalories);
        tvProtein = findViewById(R.id.tvProtein);
        tvCarbs = findViewById(R.id.tvCarbs);
        tvFats = findViewById(R.id.tvFats);
        progressCalories = findViewById(R.id.progressCalories);
        btnAddFood = findViewById(R.id.btnAddFood);
        tvWarning = findViewById(R.id.tvWarning);

        btnAddFood.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddFoodActivity.class);
            startActivityForResult(intent, REQUEST_CODE_FOOD);
        });

        loadUserData();
        loadSavedNutritionData();
        updateNutritionDisplay();
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = db.collection("users").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Long goalCaloriesLong = documentSnapshot.getLong("goalCalories");
                    if (goalCaloriesLong != null) {
                        dailyCaloriesGoal = goalCaloriesLong.intValue();
                    } else {
                        userRef.update("goalCalories", dailyCaloriesGoal)
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Alapértelmezett goalCalories beállítva"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Hiba a goalCalories beállításakor", e));
                    }

                    double proteinGoal = (dailyCaloriesGoal * 0.25) / 4.0;
                    double carbsGoal = (dailyCaloriesGoal * 0.50) / 4.0;
                    double fatsGoal = (dailyCaloriesGoal * 0.25) / 9.0;

                    tvCalories.setText("Cél: " + dailyCaloriesGoal + " kcal");
                    progressCalories.setMax(dailyCaloriesGoal);
                    updateNutritionDisplay();
                }
            }).addOnFailureListener(e -> {
                Log.e("Firestore", "Hiba a felhasználói adatok lekérésekor", e);
            });

        } else {
            Log.e("FirebaseAuth", "Nincs bejelentkezett felhasználó!");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);

        if (requestCode == REQUEST_CODE_FOOD && resultCode == RESULT_OK && data != null) {
            FoodItem selectedFood = (FoodItem) data.getSerializableExtra("selectedFood");
            if (selectedFood != null) {
                addFoodToTotals(selectedFood);
                Toast.makeText(this, selectedFood.getName() + " hozzáadva", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addFoodToTotals(FoodItem food) {
        consumedCalories += food.getCalories();
        consumedProtein += food.getProtein();
        consumedCarbs += food.getCarbs();
        consumedFats += food.getFats();

        saveNutritionData();
        updateNutritionDisplay();
    }

    private void saveNutritionData() {
        getSharedPreferences("nutrition_data", MODE_PRIVATE)
                .edit()
                .putInt("calories", consumedCalories)
                .putFloat("protein", (float) consumedProtein)
                .putFloat("carbs", (float) consumedCarbs)
                .putFloat("fats", (float) consumedFats)
                .apply();
    }

    private void loadSavedNutritionData() {
        var prefs = getSharedPreferences("nutrition_data", MODE_PRIVATE);
        consumedCalories = prefs.getInt("calories", 0);
        consumedProtein = prefs.getFloat("protein", 0f);
        consumedCarbs = prefs.getFloat("carbs", 0f);
        consumedFats = prefs.getFloat("fats", 0f);
    }

    private void updateNutritionDisplay() {
        tvCalories.setText("Fogyasztott: " + consumedCalories + " kcal");
        tvProtein.setText("Fehérje: " + String.format("%.1f", consumedProtein) + "g");
        tvCarbs.setText("Szénhidrát: " + String.format("%.1f", consumedCarbs) + "g");
        tvFats.setText("Zsír: " + String.format("%.1f", consumedFats) + "g");

        progressCalories.setProgress(consumedCalories);

        if (consumedCalories > dailyCaloriesGoal) {
            tvWarning.setVisibility(View.VISIBLE);
        } else {
            tvWarning.setVisibility(View.GONE);
        }
    }
}