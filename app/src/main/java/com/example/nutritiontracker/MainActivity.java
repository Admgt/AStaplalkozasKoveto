package com.example.nutritiontracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    private TextView tvCalories, tvProtein, tvCarbs, tvFats;
    private ProgressBar progressCalories;
    private FloatingActionButton btnAddFood;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    return true;
                } else if (itemId == R.id.nav_food_list) {
                    startActivity(new Intent(MainActivity.this, FoodListActivity.class));
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

        btnAddFood.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddFoodActivity.class);
            startActivity(intent);
        });

        loadUserData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId = user.getUid();
            DocumentReference userRef = db.collection("users").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    int goalCalories = documentSnapshot.getLong("goalCalories").intValue();

                    int protein = (int) ((goalCalories * 0.25) / 4);
                    int carbs = (int) ((goalCalories * 0.50) / 4);
                    int fats = (int) ((goalCalories * 0.25) / 9);

                    tvCalories.setText("Cél: " + goalCalories + " kcal");
                    tvProtein.setText("Fehérje: " + protein + "g");
                    tvCarbs.setText("Szénhidrát: " + carbs + "g");
                    tvFats.setText("Zsír: " + fats + "g");

                    progressCalories.setMax(goalCalories);
                    progressCalories.setProgress(0);
                }
            }).addOnFailureListener(e -> {
                Log.e("Firestore", "Hiba a felhasználói adatok lekérésekor", e);
            });

        } else {
            Log.e("FirebaseAuth", "Nincs bejelentkezett felhasználó!");
        }
    }
}