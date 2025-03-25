package com.example.healthsync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddFoodActivity extends AppCompatActivity {

    private EditText editTextFoodName, editTextCalories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_food);

        editTextFoodName = findViewById(R.id.editTextFoodName);
        editTextCalories = findViewById(R.id.editTextCalories);
        Button btnSave = findViewById(R.id.btnSave);

        // 接收日期和時間
        String dateTime = getIntent().getStringExtra("dateTime");

        // 顯示日期和時間
        TextView dateTimeTextView = findViewById(R.id.dateTimeTextView);
        dateTimeTextView.setText(dateTime);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String foodName = editTextFoodName.getText().toString();
                String calories = editTextCalories.getText().toString();

                if (foodName.isEmpty() || calories.isEmpty()) {
                    Toast.makeText(AddFoodActivity.this, "請填寫所有欄位", Toast.LENGTH_SHORT).show();
                    return;
                }

                saveFoodRecord(foodName, calories);
                Toast.makeText(AddFoodActivity.this, "記錄已保存", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void saveFoodRecord(String foodName, String calories) {
        SharedPreferences sharedPreferences = getSharedPreferences("FoodRecords", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        String existingRecords = sharedPreferences.getString("records", "");
        String dateTime = getIntent().getStringExtra("dateTime");
        String newRecord = dateTime + " - " + foodName + " - " + calories + " kcal\n";
        editor.putString("records", existingRecords + newRecord);
        editor.apply();
    }
}
