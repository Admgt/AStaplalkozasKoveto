package com.example.nutritiontracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FoodListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FoodAdapter adapter;
    private List<FoodItem> foodList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        foodList = new ArrayList<>();
        adapter = new FoodAdapter(foodList, new FoodAdapter.OnFoodClickListener() {
            @Override
            public void onFoodClick(FoodItem food) {
                returnFoodToMainActivity(food);
            }
        });
        recyclerView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_food_list);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    setResult(RESULT_CANCELED);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_food_list) {
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(FoodListActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                FoodItem food = doc.toObject(FoodItem.class);
                foodList.add(food);
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void returnFoodToMainActivity(FoodItem food) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("selectedFood", food);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}