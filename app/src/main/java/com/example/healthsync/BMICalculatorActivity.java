package com.example.healthsync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BMICalculatorActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BMIHistoryPrefs";
    private static final String HISTORY_KEY = "bmiHistory";

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

                    String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                    String record = String.format("Date: %s\nWeight: %.2f kg, Height: %.2f m, BMI: %.2f", date, weight, height, bmi);

                    saveToHistory(record);

                    resultText.setText(String.format("Your BMI: %.2f", bmi));
                } else {
                    resultText.setText("Please enter valid weight and height.");
                }
            }
        });

        // 新增按鈕查看 BMI 歷史
        Button historyButton = new Button(this);
        historyButton.setText("查看 BMI 歷史");
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BMICalculatorActivity.this, BMIHistoryActivity.class);
                startActivity(intent);
            }
        });

        // 將歷史按鈕動態添加到佈局
        LinearLayout layout = findViewById(R.id.bmi_calculator_layout);
        layout.addView(historyButton);

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
        layout.addView(backButton);
    }

    private void saveToHistory(String record) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String existingHistory = prefs.getString(HISTORY_KEY, "");
        String updatedHistory = existingHistory.isEmpty() ? record : existingHistory + "\n\n" + record;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(HISTORY_KEY, updatedHistory);
        editor.apply();
    }
}
