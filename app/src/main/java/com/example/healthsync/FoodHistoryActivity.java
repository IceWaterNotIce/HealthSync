package com.example.healthsync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class FoodHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_history);

        // 修正 ID 為 historyTextView
        TextView historyTextView = findViewById(R.id.historyTextView);
        loadFoodHistory(historyTextView);

        // 新增刪除歷史記錄按鈕
        Button btnDeleteHistory = findViewById(R.id.btnDeleteHistory);
        btnDeleteHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFoodHistory(historyTextView);
            }
        });
    }

    private void loadFoodHistory(TextView historyTextView) {
        SharedPreferences sharedPreferences = getSharedPreferences("FoodRecords", MODE_PRIVATE);
        String records = sharedPreferences.getString("records", "尚無記錄");
        historyTextView.setText(records);
    }

    private void deleteFoodHistory(TextView historyTextView) {
        SharedPreferences sharedPreferences = getSharedPreferences("FoodRecords", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("records");
        editor.apply();
        historyTextView.setText("尚無記錄");
        Toast.makeText(this, "歷史記錄已刪除", Toast.LENGTH_SHORT).show();
    }
}
