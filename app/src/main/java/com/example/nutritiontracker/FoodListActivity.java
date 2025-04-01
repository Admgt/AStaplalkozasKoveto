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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
        adapter = new FoodAdapter(foodList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(FoodListActivity.this, MainActivity.class));
                    return true;
                } else if (itemId == R.id.nav_food_list) {
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(FoodListActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });

        loadFoodItems();
    }

    private void loadFoodItems() {
        db.collection("FoodItems").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
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
            }
        });
    }
}