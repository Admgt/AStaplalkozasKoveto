package com.example.nutritiontracker;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private TextView tvCalories, tvProtein, tvCarbs, tvFats;
    private TextView tvWarning;
    private ProgressBar progressCalories;
    private FloatingActionButton btnAddFood;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private RecyclerView recyclerView;
    private ConsumedFoodAdapter adapter;
    private List<FoodItem> consumedFoods;
    private List<String> documentIds;
    private int dailyCaloriesGoal = 2000;
    private int consumedCalories = 0;
    private double consumedProtein = 0.0;
    private double consumedCarbs = 0.0;
    private double consumedFats = 0.0;
    private String currentDate;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        createNotificationChannel();
        askNotificationPermission();
        scheduleReminderNotification();

        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        recyclerView = findViewById(R.id.recyclerViewConsumedFoods);
        consumedFoods = new ArrayList<>();
        documentIds = new ArrayList<>();
        adapter = new ConsumedFoodAdapter(consumedFoods, documentIds);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        Button btnOpenStats = findViewById(R.id.btnOpenStats);
        btnOpenStats.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TopFoodsActivity.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
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
        tvWarning = findViewById(R.id.tvWarning);

        btnAddFood.setOnClickListener(v -> showAddFoodDialog());

        loadUserData();
        loadConsumedFoods();
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Értesítések engedélyezve", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Értesítések le vannak tiltva", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadUserData() {
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

                progressCalories.setMax(dailyCaloriesGoal);
                loadDailyConsumption();
            }
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Hiba a felhasználói adatok lekérésekor", e);
            Toast.makeText(MainActivity.this, "Hiba a felhasználói adatok lekérésekor", Toast.LENGTH_SHORT).show();
        });
    }

    private void showAddFoodDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_food, null);

        EditText etName = dialogView.findViewById(R.id.etFoodName);
        EditText etCalories = dialogView.findViewById(R.id.etCalories);
        EditText etProtein = dialogView.findViewById(R.id.etProtein);
        EditText etCarbs = dialogView.findViewById(R.id.etCarbs);
        EditText etFats = dialogView.findViewById(R.id.etFats);

        builder.setView(dialogView)
                .setTitle("Új étel hozzáadása")
                .setPositiveButton("Mentés", (dialog, id) -> {
                    if (etName.getText().toString().trim().isEmpty() ||
                            etCalories.getText().toString().trim().isEmpty()) {
                        Toast.makeText(this, "Kérlek töltsd ki a kötelező mezőket", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FoodItem newFood = new FoodItem();
                    newFood.setName(etName.getText().toString().trim());
                    newFood.setCalories(Integer.parseInt(etCalories.getText().toString().trim()));

                    if (!etProtein.getText().toString().isEmpty()) {
                        newFood.setProtein(Double.parseDouble(etProtein.getText().toString().trim()));
                    }

                    if (!etCarbs.getText().toString().isEmpty()) {
                        newFood.setCarbs(Double.parseDouble(etCarbs.getText().toString().trim()));
                    }

                    if (!etFats.getText().toString().isEmpty()) {
                        newFood.setFats(Double.parseDouble(etFats.getText().toString().trim()));
                    }

                    db.collection("FoodItems")
                            .add(newFood)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(MainActivity.this, "Étel sikeresen hozzáadva", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(MainActivity.this, "Hiba az étel mentése közben", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Mégse", (dialog, id) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void loadDailyConsumption() {
        consumedCalories = 0;
        consumedProtein = 0.0;
        consumedCarbs = 0.0;
        consumedFats = 0.0;

        String userId = user.getUid();

        db.collection("users").document(userId)
                .collection("dailyConsumption").document(currentDate)
                .collection("consumedFoods")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Error getting daily consumption", error);
                        return;
                    }

                    if (value != null) {
                        consumedCalories = 0;
                        consumedProtein = 0.0;
                        consumedCarbs = 0.0;
                        consumedFats = 0.0;

                        for (QueryDocumentSnapshot doc : value) {
                            FoodItem food = doc.toObject(FoodItem.class);
                            consumedCalories += food.getCalories();
                            consumedProtein += food.getProtein();
                            consumedCarbs += food.getCarbs();
                            consumedFats += food.getFats();
                        }

                        updateNutritionDisplay();
                    }
                });
    }

    private void loadConsumedFoods() {
        String userId = user.getUid();

        db.collection("users")
                .document(userId)
                .collection("dailyConsumption")
                .document(currentDate)
                .collection("consumedFoods")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e("Firestore", "Hiba: ", e);
                        Toast.makeText(MainActivity.this, "Hiba az adatok lekérésekor", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    consumedFoods.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        FoodItem food = doc.toObject(FoodItem.class);
                        food.setId(doc.getId());
                        consumedFoods.add(food);
                    }

                    adapter.notifyDataSetChanged();
                });
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Éves Értesítő Csatorna";
            String description = "Napi emlékeztető értesítések";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("eves_ertesito", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void scheduleReminderNotification() {
        if (!canScheduleExactAlarms()) {
            Toast.makeText(this, "Pontos értesítések engedélyezése szükséges", Toast.LENGTH_LONG).show();
            requestExactAlarmPermission();
            return;
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 44);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }

            Toast.makeText(this, "Emlékeztető beállítva " + calendar.getTime().toString(), Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, "Értesítés jogosultság hiányzik. Kérlek engedélyezd a beállításokban.", Toast.LENGTH_LONG).show();
            Log.e("AlarmScheduler", "SecurityException when scheduling alarm", e);
            requestExactAlarmPermission();
        }
    }

    private boolean canScheduleExactAlarms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            return alarmManager.canScheduleExactAlarms();
        }
        return true;
    }

    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (user != null) {
            loadUserData();
        }
    }
}