package com.example.healthsync;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class BMICalculatorActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_calculator);

        EditText weightInput = findViewById(R.id.weight_input);
        EditText heightInput = findViewById(R.id.height_input);
        Button calculateButton = findViewById(R.id.calculate_button);
        TextView resultText = findViewById(R.id.result_text);

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String weightStr = weightInput.getText().toString();
                String heightStr = heightInput.getText().toString();

                if (!weightStr.isEmpty() && !heightStr.isEmpty()) {
                    double weight = Double.parseDouble(weightStr);
                    double height = Double.parseDouble(heightStr);
                    double bmi = weight / (height * height);
                    resultText.setText(String.format("Your BMI: %.2f", bmi));
                } else {
                    resultText.setText("Please enter valid weight and height.");
                }
            }
        });

        // 新增按鈕返回主頁面
        Button backButton = new Button(this);
        backButton.setText("返回主頁面");
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BMICalculatorActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Optional: Close the current activity
            }
        });

        // 將返回按鈕動態添加到佈局
        LinearLayout layout = findViewById(R.id.bmi_calculator_layout); // Ensure the root layout has an ID
        layout.addView(backButton);
    }
}
