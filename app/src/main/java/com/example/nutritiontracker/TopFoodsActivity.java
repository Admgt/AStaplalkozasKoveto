package com.example.nutritiontracker;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TopFoodsActivity extends AppCompatActivity {
    private RecyclerView recyclerViewHighCalorie;
    private RecyclerView recyclerViewHighProtein;
    private RecyclerView recyclerViewLowFat;

    private StatsFoodAdapter highCalorieAdapter;
    private StatsFoodAdapter highProteinAdapter;
    private StatsFoodAdapter lowFatAdapter;

    private List<FoodItem> highCalorieFoods;
    private List<FoodItem> highProteinFoods;
    private List<FoodItem> lowFatFoods;

    private FirebaseFirestore db;

    private TextView tvHighCalorieTitle;
    private TextView tvHighProteinTitle;
    private TextView tvLowFatTitle;
    private TextView tvActivityTitle;
    private ImageButton btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_foods);

        db = FirebaseFirestore.getInstance();

        // UI elemek inicializálása
        tvActivityTitle = findViewById(R.id.tvActivityTitle);
        tvHighCalorieTitle = findViewById(R.id.tvHighCalorieTitle);
        tvHighProteinTitle = findViewById(R.id.tvHighProteinTitle);
        tvLowFatTitle = findViewById(R.id.tvLowFatTitle);

        // Vissza gomb inicializálása
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        setupRecyclerViews();

        loadHighCalorieFoods();
        loadHighProteinFoods();
        loadLowFatFoods();
    }

    private void setupRecyclerViews() {
        // Magas kalóriatartalmú ételek beállítása
        recyclerViewHighCalorie = findViewById(R.id.recyclerViewHighCalorie);
        recyclerViewHighCalorie.setLayoutManager(new LinearLayoutManager(this));
        highCalorieFoods = new ArrayList<>();
        highCalorieAdapter = new StatsFoodAdapter(highCalorieFoods);
        recyclerViewHighCalorie.setAdapter(highCalorieAdapter);

        // Magas fehérjetartalmú ételek beállítása
        recyclerViewHighProtein = findViewById(R.id.recyclerViewHighProtein);
        recyclerViewHighProtein.setLayoutManager(new LinearLayoutManager(this));
        highProteinFoods = new ArrayList<>();
        highProteinAdapter = new StatsFoodAdapter(highProteinFoods);
        recyclerViewHighProtein.setAdapter(highProteinAdapter);

        // Alacsony zsírtartalmú ételek beállítása
        recyclerViewLowFat = findViewById(R.id.recyclerViewLowFat);
        recyclerViewLowFat.setLayoutManager(new LinearLayoutManager(this));
        lowFatFoods = new ArrayList<>();
        lowFatAdapter = new StatsFoodAdapter(lowFatFoods);
        recyclerViewLowFat.setAdapter(lowFatAdapter);
    }

    private void loadHighCalorieFoods() {
        tvHighCalorieTitle.setText("Magas kalóriatartalmú ételek betöltése...");

        Query highCalorieQuery = db.collection("FoodItems")
                .whereGreaterThan("calories", 500)
                .orderBy("calories", Query.Direction.DESCENDING)
                .limit(10);

        highCalorieQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            highCalorieFoods.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                FoodItem food = doc.toObject(FoodItem.class);
                food.setId(doc.getId());
                highCalorieFoods.add(food);
            }

            if (highCalorieFoods.isEmpty()) {
                tvHighCalorieTitle.setText("Nem található magas kalóriatartalmú étel");
            } else {
                tvHighCalorieTitle.setText("TOP 10 magas kalóriatartalmú étel");
            }

            highCalorieAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            tvHighCalorieTitle.setText("Hiba a kalóriás ételek lekérdezésénél");
            Toast.makeText(this, "Hiba a kalóriás ételek lekérdezésénél", Toast.LENGTH_SHORT).show();
            Log.e("Firestore", "High Calorie Query Error", e);
        });
    }

    private void loadHighProteinFoods() {
        tvHighProteinTitle.setText("Magas fehérjetartalmú ételek betöltése...");

        Query highProteinQuery = db.collection("FoodItems")
                .whereGreaterThan("protein", 20)
                .orderBy("protein", Query.Direction.DESCENDING)
                .orderBy("calories", Query.Direction.ASCENDING)
                .limit(10);

        highProteinQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            highProteinFoods.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                FoodItem food = doc.toObject(FoodItem.class);
                food.setId(doc.getId());
                highProteinFoods.add(food);
            }

            if (highProteinFoods.isEmpty()) {
                tvHighProteinTitle.setText("Nem található magas fehérjetartalmú étel");
            } else {
                tvHighProteinTitle.setText("TOP 10 magas fehérjetartalmú étel");
            }

            highProteinAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            tvHighProteinTitle.setText("Hiba a fehérjés ételek lekérdezésénél");
            Toast.makeText(this, "Hiba a fehérjés ételek lekérdezésénél: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("Firestore", "High Protein Query Error", e);

            if (e.getMessage() != null && e.getMessage().contains("index")) {
                Log.w("Firestore", "Ez a lekérdezés összetett indexet igényel. Ellenőrizd a Firebase Console-t.");
                Toast.makeText(this, "Index hiba: Kérjük, hozza létre a szükséges indexet a Firebase konzolon.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadLowFatFoods() {
        tvLowFatTitle.setText("Alacsony zsírtartalmú ételek betöltése...");

        final int BATCH_SIZE = 10;

        Query lowFatQuery = db.collection("FoodItems")
                .whereLessThan("fats", 10)
                .orderBy("fats", Query.Direction.ASCENDING)
                .limit(BATCH_SIZE);

        lowFatQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            lowFatFoods.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                FoodItem food = doc.toObject(FoodItem.class);
                food.setId(doc.getId());
                lowFatFoods.add(food);
            }

            if (lowFatFoods.isEmpty()) {
                tvLowFatTitle.setText("Nem található alacsony zsírtartalmú étel");
            } else {
                tvLowFatTitle.setText("TOP " + BATCH_SIZE + " alacsony zsírtartalmú étel");
            }

            lowFatAdapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> {
            tvLowFatTitle.setText("Hiba a zsírmentes ételek lekérdezésénél");
            Toast.makeText(this, "Hiba a zsírmentes ételek lekérdezésénél", Toast.LENGTH_SHORT).show();
            Log.e("Firestore", "Low Fat Query Error", e);
        });
    }
}