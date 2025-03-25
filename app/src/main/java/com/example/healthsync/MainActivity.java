package com.example.healthsync;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View; // 修正導入 View 的問題
import android.widget.Button;
import android.widget.TextView;
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

        // 顯示目標 BMI
        TextView targetBMITextView = findViewById(R.id.targetBMITextView);
        String targetBMI = calculateTargetBMI();
        targetBMITextView.setText("建議目標 BMI: " + targetBMI);

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

        // 建議下一餐
        TextView suggestionTextView = findViewById(R.id.suggestionTextView);
        String suggestion = suggestNextMeal();
        suggestionTextView.setText(suggestion);
    }

    // 計算目標 BMI
    private String calculateTargetBMI() {
        SharedPreferences sharedPreferences = getSharedPreferences("BMIRecords", MODE_PRIVATE);
        // 修正鍵名，確保正確讀取 BMI 記錄
        String lastBMIRecord = sharedPreferences.getString("lastBMI", "");

        if (lastBMIRecord.isEmpty()) {
            return "無法建議 (尚無歷史記錄)";
        }

        try {
            double lastBMI = Double.parseDouble(lastBMIRecord);
            if (lastBMI < 18.5) {
                return "18.5 (增重建議)";
            } else if (lastBMI > 24.9) {
                return "22.0 (減重建議)";
            } else {
                return "保持現狀";
            }
        } catch (NumberFormatException e) {
            return "無法建議 (記錄格式錯誤)";
        }
    }

    private String suggestNextMeal() {
        SharedPreferences sharedPreferences = getSharedPreferences("FoodRecords", MODE_PRIVATE);
        String records = sharedPreferences.getString("records", "");

        if (records.isEmpty()) {
            return "尚無飲食記錄，建議每餐攝取 500-700 kcal，間隔約 4 小時";
        }

        String[] recordArray = records.split("\n");
        String lastRecord = recordArray[recordArray.length - 1];
        String[] lastRecordParts = lastRecord.split(" - ");

        if (lastRecordParts.length < 3) {
            return "記錄格式錯誤，無法建議";
        }

        String lastTime = lastRecordParts[0].trim();
        String lastCalories = lastRecordParts[2].replace(" kcal", "").trim();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date lastDate = sdf.parse(lastTime);
            long timeSinceLastMeal = (new Date().getTime() - lastDate.getTime()) / (1000 * 60 * 60);

            int suggestedCalories = 600; // 預設建議熱量
            if (timeSinceLastMeal >= 4) {
                suggestedCalories += 100; // 如果超過 4 小時，增加熱量建議
            }

            return "建議下一餐攝取 " + suggestedCalories + " kcal，並在 " + Math.max(0, 4 - timeSinceLastMeal) + " 小時內進食";
        } catch (Exception e) {
            return "無法解析記錄時間，無法建議";
        }
    }
}