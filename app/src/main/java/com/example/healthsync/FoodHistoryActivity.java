package com.example.healthsync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class FoodHistoryActivity extends AppCompatActivity {

    private TextView textViewHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_history);

        textViewHistory = findViewById(R.id.textViewHistory);
        loadFoodHistory();
    }

    private void loadFoodHistory() {
        SharedPreferences sharedPreferences = getSharedPreferences("FoodRecords", MODE_PRIVATE);
        String records = sharedPreferences.getString("records", "尚無記錄");
        textViewHistory.setText(records);
    }
}
