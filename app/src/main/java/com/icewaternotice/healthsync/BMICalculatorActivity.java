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
import com.icewaternotice.healthsync.utils.CalculationUtils; // Import the utility class

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BMICalculatorActivity extends BaseActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;
    private UserDataSyncManager userDataSyncManager;

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
        userDataSyncManager = new UserDataSyncManager(this, firebaseAuth, databaseReference);

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

        // Load data from Firebase using UserDataSyncManager
        userDataSyncManager.addDatabaseValueEventListener("height", R.id.etHeight, "", this, errorMessage -> 
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        );
        userDataSyncManager.addDatabaseValueEventListener("weight", R.id.etWeight, "", this, errorMessage -> 
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        );

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
                    float bmi = CalculationUtils.calculateBMI(heightStr, weightStr);
                    String bmiCategory = CalculationUtils.getBMICategory(bmi);
                    String result = String.format("Your BMI: %.2f (%s)", bmi, bmiCategory);
                    tvBMIResult.setText(result);
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

        // Sync only height and weight to Firebase
        userDataSyncManager.syncData("height", height, "Height");
        userDataSyncManager.syncData("weight", weight, "Weight");
    }
}
