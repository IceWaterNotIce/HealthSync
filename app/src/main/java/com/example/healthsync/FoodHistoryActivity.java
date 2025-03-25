package com.example.healthsync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FoodHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_history);

        // 修正 ID 為 historyTextView
        TextView historyTextView = findViewById(R.id.historyTextView);
        loadFoodHistory(historyTextView);
    }

    private void loadFoodHistory(TextView historyTextView) {
        SharedPreferences sharedPreferences = getSharedPreferences("FoodRecords", MODE_PRIVATE);
        String records = sharedPreferences.getString("records", "尚無記錄");
        historyTextView.setText(records);
    }
}
