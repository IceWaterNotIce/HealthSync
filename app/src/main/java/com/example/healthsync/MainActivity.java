package com.example.healthsync;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View; // 修正導入 View 的問題
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 新增按鈕導航到 BMI 計算器
        Button btnBMI = findViewById(R.id.btnBMI);
        btnBMI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BMICalculatorActivity.class);
                startActivity(intent);
            }
        });

        // 新增按鈕導航到新增飲食記錄
        Button btnAddFood = findViewById(R.id.btnAddFood);
        btnAddFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddFoodActivity.class);
                // 新增當前日期和時間
                String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                intent.putExtra("dateTime", currentDateTime);
                startActivity(intent);
            }
        });

        // 新增按鈕導航到查看歷史記錄
        Button btnViewHistory = findViewById(R.id.btnViewHistory);
        btnViewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FoodHistoryActivity.class);
                startActivity(intent);
            }
        });

        // 新增按鈕導航到 PhotoKcalActivity
        Button btnPhotoKcal = findViewById(R.id.btnPhotoKcal);
        btnPhotoKcal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PhotoKcalActivity.class);
                startActivity(intent);
            }
        });

        // 請求權限
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
    }
}