package com.example.nutritiontracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvEmail;
    private EditText etGoalCalories;
    private Button btnSaveGoal, btnLogout;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvEmail = findViewById(R.id.tvEmail);
        etGoalCalories = findViewById(R.id.etDailyCalorieGoal);
        btnSaveGoal = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_profile);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_food_list) {
                    startActivity(new Intent(ProfileActivity.this, FoodListActivity.class));
                    finish();
                    return true;
                } else return itemId == R.id.nav_profile;
            }
        });

        if (user != null) {
            tvEmail.setText(user.getEmail());
            loadUserData();
        } else {
            Toast.makeText(this, "Hiba: nincs bejelentkezett felhasználó!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        btnSaveGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveGoalCalories();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void loadUserData() {
        DocumentReference userRef = db.collection("users").document(user.getUid());

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Long goalCalories = documentSnapshot.getLong("goalCalories");
                if (goalCalories != null) {
                    etGoalCalories.setText(String.valueOf(goalCalories));
                }
            } else {
                Toast.makeText(this, "Nincsenek mentett adatok!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Hiba történt az adatok betöltésekor!", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveGoalCalories() {
        String goalStr = etGoalCalories.getText().toString().trim();
        if (goalStr.isEmpty()) {
            Toast.makeText(this, "Adj meg egy kalória célt!", Toast.LENGTH_SHORT).show();
            return;
        }

        int goalCalories = Integer.parseInt(goalStr);
        DocumentReference userRef = db.collection("users").document(user.getUid());
        userRef.update("goalCalories", goalCalories).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Kalóriacél mentve!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Hiba történt a mentéskor!", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        auth = null;
        db = null;
        user = null;
    }
}