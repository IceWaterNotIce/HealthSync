package com.example.healthsync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BMICalculatorActivity extends BaseActivity {

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

        EditText etHeight = findViewById(R.id.etHeight);
        EditText etWeight = findViewById(R.id.etWeight);
        TextView tvBMIResult = findViewById(R.id.tvBMIResult);
        Button btnCalculateBMI = findViewById(R.id.btnCalculateBMI);
        Button returnButton = findViewById(R.id.btnReturnToBMI);

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

        // Log the updated history for debugging
        Log.d("BMICalculatorActivity", "Updated BMI History: " + updatedHistory);

        Toast.makeText(this, "BMI saved to history.", Toast.LENGTH_SHORT).show();
    }
}
