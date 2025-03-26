package com.example.healthsync;

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

    // 改進建議文字的表達
    private String suggestNextMeal() {
        SharedPreferences sharedPreferences = getSharedPreferences("FoodRecords", MODE_PRIVATE);
        String records = sharedPreferences.getString("records", "");

        if (records.trim().isEmpty()) {
            return "尚無飲食記錄。建議每餐攝取 500-700 kcal，並保持 4 小時的間隔。";
        }

        // 修正記錄中可能存在的多餘空格
        String[] recordArray = records.split("\n");
        for (int i = 0; i < recordArray.length; i++) {
            recordArray[i] = recordArray[i].trim().replaceAll("\\s{2,}", " "); // 移除多餘空格
        }

        String lastRecord = recordArray[recordArray.length - 1];

        if (lastRecord.isEmpty() || !lastRecord.contains(" - ")) {
            return "記錄格式錯誤或記錄為空，無法建議";
        }

        String[] lastRecordParts = lastRecord.split(" - ");
        if (lastRecordParts.length < 3) {
            return "記錄格式錯誤，無法建議";
        }

        String lastTime = lastRecordParts[0].trim();
        String lastCalories = lastRecordParts[2].replaceAll("[^0-9]", ""); // 僅提取數字部分

        if (lastTime.isEmpty() || lastCalories.isEmpty()) {
            return "記錄格式錯誤，無法建議";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date lastDate = sdf.parse(lastTime);
            long timeSinceLastMeal = (new Date().getTime() - lastDate.getTime()) / (1000 * 60 * 60);

            int suggestedCalories = 600; // 預設建議熱量
            if (timeSinceLastMeal >= 4) {
                suggestedCalories += 100; // 如果超過 4 小時，增加熱量建議
            }

            return "距離上次進食已過 " + timeSinceLastMeal + " 小時。建議下一餐攝取 " + suggestedCalories + " kcal，並在 " + Math.max(0, 4 - timeSinceLastMeal) + " 小時內進食。";
        } catch (Exception e) {
            return "無法解析記錄時間，請檢查記錄格式。";
        }
    }
}