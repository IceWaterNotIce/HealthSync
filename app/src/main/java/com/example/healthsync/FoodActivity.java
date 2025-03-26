package com.example.healthsync;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FoodActivity extends BaseActivity {

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_food;
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_food;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ...existing code for FoodActivity logic (if any)...

        Button btnAddFood = findViewById(R.id.btnAddFood);
        btnAddFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodActivity.this, AddFoodActivity.class);
                String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                intent.putExtra("dateTime", currentDateTime);
                startActivity(intent);
            }
        });

        Button btnViewHistory = findViewById(R.id.btnViewHistory);
        btnViewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodActivity.this, FoodHistoryActivity.class);
                startActivity(intent);
            }
        });

        Button btnPhotoKcal = findViewById(R.id.btnPhotoKcal);
        btnPhotoKcal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodActivity.this, PhotoKcalActivity.class);
                startActivity(intent);
            }
        });
    }
}
