package com.icewaternotice.healthsync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BMICalculatorActivity extends BaseActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_bmi_calculator;
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_bmi;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Firebase Auth and Database Reference
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        EditText etHeight = findViewById(R.id.etHeight);
        EditText etWeight = findViewById(R.id.etWeight);
        TextView tvBMIResult = findViewById(R.id.tvBMIResult);
        Button btnCalculateBMI = findViewById(R.id.btnCalculateBMI);
        Button returnButton = findViewById(R.id.btnReturnToBMI);

        // Load saved height and weight
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedHeight = sharedPreferences.getString("height", "");
        String savedWeight = sharedPreferences.getString("weight", "");

        if (!savedHeight.isEmpty()) {
            etHeight.setText(savedHeight);
        }
        if (!savedWeight.isEmpty()) {
            etWeight.setText(savedWeight);
        }

        // Load data from Firebase
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            databaseReference.child(currentUser.getUid()).child("height").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String height = snapshot.getValue(String.class);
                    if (height != null) {
                        etHeight.setText(height);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("BMICalculatorActivity", "Failed to load height: " + error.getMessage());
                }
            });

            databaseReference.child(currentUser.getUid()).child("weight").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String weight = snapshot.getValue(String.class);
                    if (weight != null) {
                        etWeight.setText(weight);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("BMICalculatorActivity", "Failed to load weight: " + error.getMessage());
                }
            });

            databaseReference.child(currentUser.getUid()).child("bmi").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String bmi = snapshot.getValue(String.class);
                    if (bmi != null) {
                        tvBMIResult.setText("Your BMI: " + bmi);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("BMICalculatorActivity", "Failed to load BMI: " + error.getMessage());
                }
            });

            databaseReference.child(currentUser.getUid()).child("bmiHistory").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String bmiHistory = snapshot.getValue(String.class);
                    if (bmiHistory != null) {
                        Log.d("BMICalculatorActivity", "Loaded BMI History: " + bmiHistory);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("BMICalculatorActivity", "Failed to load BMI history: " + error.getMessage());
                }
            });
        } else {
            Toast.makeText(this, "User not logged in. Cannot load data.", Toast.LENGTH_SHORT).show();
        }

        // Handle BMI calculation
        btnCalculateBMI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String heightStr = etHeight.getText().toString().trim();
                String weightStr = etWeight.getText().toString().trim();

                if (heightStr.isEmpty() || weightStr.isEmpty()) {
                    Toast.makeText(BMICalculatorActivity.this, "Please enter both height and weight.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    float height = Float.parseFloat(heightStr) / 100; // Convert cm to meters
                    float weight = Float.parseFloat(weightStr);
                    float bmi = weight / (height * height);

                    saveHeightAndWeight(heightStr, weightStr);

                    String bmiCategory;
                    if (bmi < 18.5) {
                        bmiCategory = "Underweight";
                    } else if (bmi >= 18.5 && bmi < 24.9) {
                        bmiCategory = "Normal weight";
                    } else if (bmi >= 25 && bmi < 29.9) {
                        bmiCategory = "Overweight";
                    } else {
                        bmiCategory = "Obesity";
                    }

                    String result = String.format("Your BMI: %.2f (%s)", bmi, bmiCategory);
                    tvBMIResult.setText(result);

                    // Save BMI to history
                    saveBMIToHistory(bmi);

                } catch (NumberFormatException e) {
                    Toast.makeText(BMICalculatorActivity.this, "Invalid input. Please enter valid numbers.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add a return button to navigate back to BMIActivity
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BMICalculatorActivity.this, BMIActivity.class);
                startActivity(intent);
                finish(); // Close the current activity
            }
        });
    }

    private void saveHeightAndWeight(String height, String weight) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("height", height);
        editor.putString("weight", weight);
        editor.apply();
        Toast.makeText(this, "Height and weight saved.", Toast.LENGTH_SHORT).show();

        // Sync height and weight to Firebase
        syncWithFirebase("height", height, "Height");
        syncWithFirebase("weight", weight, "Weight");
    }

    private void saveBMIToHistory(float bmi) {
        SharedPreferences sharedPreferences = getSharedPreferences("BMIHistoryPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Get the current date
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Append the new BMI record to the existing history
        String existingHistory = sharedPreferences.getString("bmiHistory", "");
        String newRecord = currentDate + " - BMI: " + String.format("%.2f", bmi);
        String updatedHistory = existingHistory.isEmpty() ? newRecord : existingHistory + "\n" + newRecord;

        editor.putString("bmiHistory", updatedHistory);
        editor.apply();

        // Sync BMI and BMI history to Firebase
        syncWithFirebase("bmi", String.format("%.2f", bmi), "BMI");
        syncWithFirebase("bmiHistory", updatedHistory, "BMI History");

        // Log the updated history for debugging
        Log.d("BMICalculatorActivity", "Updated BMI History: " + updatedHistory);

        Toast.makeText(this, "BMI saved to history.", Toast.LENGTH_SHORT).show();
    }

    private void syncWithFirebase(String key, String value, String displayName) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            databaseReference.child(currentUser.getUid()).child(key).setValue(value)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, displayName + " synced to Firebase.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, displayName + " sync failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        } else {
            Toast.makeText(this, "User not logged in. Cannot sync " + displayName, Toast.LENGTH_SHORT).show();
        }
    }
}
