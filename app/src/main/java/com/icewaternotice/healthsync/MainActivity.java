package com.icewaternotice.healthsync;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View; // 修正導入 View 的問題
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends BaseActivity {

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_home; // Ensure this matches the "Home" menu item ID
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 顯示目標 BMI
        TextView targetBMITextView = findViewById(R.id.targetBMITextView);
        String targetBMI = calculateTargetBMI();
        targetBMITextView.setText("建議目標 BMI: " + targetBMI);

        // 顯示 BMR
        TextView bmrTextView = findViewById(R.id.bmrTextView); // 確保在 XML 中新增此 TextView
        String bmr = calculateBMR();
        bmrTextView.setText("您的 BMR: " + bmr + " kcal");

        // 請求權限
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        // 建議下一餐
        TextView suggestionTextView = findViewById(R.id.suggestionTextView);
        String suggestion = suggestNextMeal();
        suggestionTextView.setText(suggestion);

        // 確保在應用啟動時正確顯示建議
        suggestionTextView.post(new Runnable() {
            @Override
            public void run() {
                String suggestion = suggestNextMeal();
                suggestionTextView.setText(suggestion);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在返回主畫面時刷新建議文字
        TextView suggestionTextView = findViewById(R.id.suggestionTextView);
        String suggestion = suggestNextMeal();
        suggestionTextView.setText(suggestion);
    }

    // 計算目標 BMI
    private String calculateTargetBMI() {
        SharedPreferences sharedPreferences = getSharedPreferences("BMIHistoryPrefs", MODE_PRIVATE);
        // 修正鍵名，確保正確讀取 BMI 歷史記錄
        String bmiHistory = sharedPreferences.getString("bmiHistory", "");

        if (bmiHistory.isEmpty()) {
            return "無法建議 (尚無歷史記錄)";
        }

        try {
            String[] records = bmiHistory.split("\n");
            String lastRecord = records[records.length - 1].trim();
            if (!lastRecord.contains("BMI: ")) {
                return "無法建議 (記錄格式錯誤)";
            }

            String bmiValue = lastRecord.split("BMI: ")[1].trim();
            double lastBMI = Double.parseDouble(bmiValue);

            if (lastBMI < 18.5) {
                return "18.5 (增重建議)";
            } else if (lastBMI > 24.9) {
                return "22.0 (減重建議)";
            } else {
                return "保持現狀";
            }
        } catch (Exception e) {
            return "無法建議 (記錄格式錯誤)";
        }
    }

    private String calculateBMR() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String heightStr = sharedPreferences.getString("height", "");
        String weightStr = sharedPreferences.getString("weight", "");
        String birthdayStr = sharedPreferences.getString("birthday", ""); // 假設生日格式為 "yyyy-MM-dd"
        String ageStr = "";

        if (!birthdayStr.isEmpty()) {
            try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date birthday = sdf.parse(birthdayStr);
            Date today = new Date();

            long ageInMillis = today.getTime() - birthday.getTime();
            int age = (int) (ageInMillis / (1000L * 60 * 60 * 24 * 365));
            ageStr = String.valueOf(age);
            } catch (Exception e) {
            ageStr = ""; // 如果生日格式錯誤，保持 ageStr 為空
            }
        }
        String gender = sharedPreferences.getString("gender", "male"); // 預設為男性

        if (heightStr.isEmpty() || weightStr.isEmpty() || ageStr.isEmpty()) {
            return "無法計算 (缺少資料)";
        }

        try {
            double height = Double.parseDouble(heightStr);
            double weight = Double.parseDouble(weightStr);
            int age = Integer.parseInt(ageStr);

            double bmr;
            if (gender.equalsIgnoreCase("male")) {
                bmr = 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age);
            } else {
                bmr = 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);
            }

            return String.format(Locale.getDefault(), "%.2f", bmr);
        } catch (NumberFormatException e) {
            return "無法計算 (資料格式錯誤)";
        }
    }

    // 改進建議文字的表達
    private String suggestNextMeal() {
        SharedPreferences sharedPreferences = getSharedPreferences("FoodRecords", MODE_PRIVATE);
        String records = sharedPreferences.getString("records", "").trim();

        if (records.isEmpty()) {
            return "尚無飲食記錄。建議每餐攝取 500-700 kcal，並保持 4 小時的間隔。";
        }

        String[] recordArray = records.split("\n");
        String lastRecord = recordArray[recordArray.length - 1].trim();

        if (!lastRecord.contains(" - ")) {
            return "記錄格式錯誤或記錄為空，無法建議";
        }

        String[] lastRecordParts = lastRecord.split(" - ");
        if (lastRecordParts.length < 3) {
            return "記錄格式錯誤，無法建議";
        }

        String lastTime = lastRecordParts[0].trim();
        String lastCalories = lastRecordParts[2].replaceAll("[^0-9]", "").trim();

        if (lastTime.isEmpty() || lastCalories.isEmpty()) {
            return "記錄格式錯誤，無法建議";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date lastDate = sdf.parse(lastTime);
            long timeSinceLastMeal = (new Date().getTime() - lastDate.getTime()) / (1000 * 60 * 60);

            int suggestedCalories = 600;
            if (timeSinceLastMeal >= 4) {
                suggestedCalories += 100;
            }

            return "距離上次進食已過 " + timeSinceLastMeal + " 小時。建議下一餐攝取 " + suggestedCalories + " kcal，並在 " + Math.max(0, 4 - timeSinceLastMeal) + " 小時內進食。";
        } catch (Exception e) {
            return "無法解析記錄時間，請檢查記錄格式。";
        }
    }
}