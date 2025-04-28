package com.example.nutritiontracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvEmail;
    private EditText etGoalCalories;
    private Button btnSaveGoal, btnLogout;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private static final int REQUEST_CODE_GALLERY = 101;
    private static final int REQUEST_PERMISSION_READ_IMAGES = 102;
    private ImageView imgProfilePicture;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        storageRef = FirebaseStorage.getInstance().getReference("profile_pics");

        tvEmail = findViewById(R.id.tvEmail);
        etGoalCalories = findViewById(R.id.etDailyCalorieGoal);
        btnSaveGoal = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);
        imgProfilePicture = findViewById(R.id.imgProfilePicture);

        Button btnSelectProfilePic = findViewById(R.id.btnSelectProfilePic);
        btnSelectProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

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

                String profileImagePath  = documentSnapshot.getString("profileImagePath");
                if (profileImagePath != null && !profileImagePath.isEmpty()) {
                    loadProfileImage(profileImagePath);

                    if (profileImagePath.startsWith("file://")) {
                        String filePath = profileImagePath.substring(7);
                        File imageFile = new File(filePath);
                        if (!imageFile.exists()) {
                            Log.w("ProfileActivity", "Profile image file not found: " + filePath);
                        }
                    }
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

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImage();
        }
    }

    private void uploadImage() {
        if (imageUri != null && user != null) {
            try {
                Toast.makeText(this, "Kép mentése folyamatban...", Toast.LENGTH_SHORT).show();

                File outputFile = getProfileImageFile();

                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                FileOutputStream fos = new FileOutputStream(outputFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                fos.close();

                String imagePath = "file://" + outputFile.getAbsolutePath();

                db.collection("users").document(user.getUid())
                        .update("profileImagePath", imagePath)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(ProfileActivity.this, "Profilkép sikeresen mentve!", Toast.LENGTH_SHORT).show();

                            loadProfileImage(imagePath);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(ProfileActivity.this, "Hiba a mentéskor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });

            } catch (Exception e) {
                Toast.makeText(this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void loadProfileImage(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            Glide.with(this)
                    .load(Uri.parse(imagePath))
                    .placeholder(R.drawable.circle_background)
                    .error(R.drawable.circle_background)
                    .into(imgProfilePicture);
        }
    }

    private File getProfileImageDirectory() {
        File directory = new File(getApplicationContext().getExternalFilesDir(null), "profile_images");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    private File getProfileImageFile() {
        return new File(getProfileImageDirectory(), "profile_" + user.getUid() + ".jpg");
    }
/*
    @Override
    protected void onDestroy() {
        super.onDestroy();
        auth = null;
        db = null;
        user = null;
    }*/
}