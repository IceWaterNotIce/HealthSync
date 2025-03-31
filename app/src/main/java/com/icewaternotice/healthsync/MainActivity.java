package com.icewaternotice.healthsync;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends BaseActivity {
    private ProgressDialog progressDialog;
    private static final long MILLIS_IN_A_DAY = 1000L * 60 * 60 * 24;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_main;
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化進度條
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_message));
        progressDialog.setCancelable(false);

        showProgressDialog();
        syncUserData(); // Sync user data when the app opens
        hideProgressDialog();

        // 顯示目標 BMI
        TextView targetBMITextView = findViewById(R.id.targetBMITextView);
        String targetBMI = calculateTargetBMI();
        targetBMITextView.setText(getString(R.string.target_bmi_message, targetBMI));

        // 顯示 BMR
        TextView bmrTextView = findViewById(R.id.bmrTextView);
        String bmr = calculateBMR();
        bmrTextView.setText(getString(R.string.bmr_message, bmr));

        // 請求權限
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        // 建議下一餐
        TextView suggestionTextView = findViewById(R.id.suggestionTextView);
        String suggestion = suggestNextMeal();
        suggestionTextView.setText(suggestion);

        // 確保在應用啟動時正確顯示建議
        suggestionTextView.post(() -> suggestionTextView.setText(suggestNextMeal()));

        // 保存首次啟動日期
        SharedPreferences sharedPreferences = getSharedPreferences("AppUsagePrefs", MODE_PRIVATE);
        if (!sharedPreferences.contains("firstLaunchDate")) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            sharedPreferences.edit().putString("firstLaunchDate", currentDate).apply();
        }

        // 計算並顯示使用天數
        TextView usageDaysTextView = findViewById(R.id.usageDaysTextView);
        String usageDays = calculateUsageDays();
        usageDaysTextView.setText(getString(R.string.usage_days_message, usageDays));

        // 從 Firebase 獲取運動目標卡路里
        fetchSportTargetKcal();

        // 計算並顯示今日所需卡路里
        calculateAndDisplayCaloriesToEat();
    }

    private void showProgressDialog() {
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private String getSharedPreferenceValue(String key, String defaultValue) {
        try {
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            return sharedPreferences.getString(key, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private void syncUserData() {
        UserDataSyncManager userDataSyncManager = new UserDataSyncManager(this);

        String gender = getSharedPreferenceValue("gender", getString(R.string.not_set));
        userDataSyncManager.syncData("gender", gender, getString(R.string.gender));

        String birthday = getSharedPreferenceValue("birthday", getString(R.string.not_set));
        userDataSyncManager.syncData("birthday", birthday, getString(R.string.birthday));

        String height = getSharedPreferenceValue("height", getString(R.string.not_set));
        String weight = getSharedPreferenceValue("weight", getString(R.string.not_set));
        userDataSyncManager.syncData("height", height, getString(R.string.height));
        userDataSyncManager.syncData("weight", weight, getString(R.string.weight));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在返回主畫面時刷新建議文字
        TextView suggestionTextView = findViewById(R.id.suggestionTextView);
        String suggestion = suggestNextMeal();
        suggestionTextView.setText(suggestion);
    }

    private String calculateTargetBMI() {
        SharedPreferences sharedPreferences = getSharedPreferences("BMIHistoryPrefs", MODE_PRIVATE);
        String bmiHistory = sharedPreferences.getString("bmiHistory", "");

        if (bmiHistory.isEmpty()) {
            return getString(R.string.cannot_suggest_no_history);
        }

        try {
            String[] records = bmiHistory.split("\n");
            String lastRecord = records[records.length - 1].trim();
            if (!lastRecord.contains("BMI: ")) {
                return getString(R.string.cannot_suggest_invalid_format);
            }

            String bmiValue = lastRecord.split("BMI: ")[1].trim();
            double lastBMI = Double.parseDouble(bmiValue);

            if (lastBMI < 18.5) {
                return getString(R.string.suggest_bmi_increase);
            } else if (lastBMI > 24.9) {
                return getString(R.string.suggest_bmi_decrease);
            } else {
                return getString(R.string.suggest_bmi_maintain);
            }
        } catch (Exception e) {
            return getString(R.string.cannot_suggest_invalid_format);
        }
    }

    private String calculateBMR() {
        String heightStr = getSharedPreferenceValue("height", "");
        String weightStr = getSharedPreferenceValue("weight", "");
        String birthdayStr = getSharedPreferenceValue("birthday", "");
        String gender = getSharedPreferenceValue("gender", "male");

        if (heightStr.isEmpty() || weightStr.isEmpty() || birthdayStr.isEmpty()) {
            return getString(R.string.cannot_calculate_missing_data);
        }

        try {
            double height = Double.parseDouble(heightStr);
            double weight = Double.parseDouble(weightStr);
            int age = calculateAge(birthdayStr);

            double bmr = gender.equalsIgnoreCase("male")
                    ? 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
                    : 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);

            return String.format(Locale.getDefault(), "%.2f", bmr);
        } catch (Exception e) {
            return getString(R.string.cannot_calculate_invalid_data);
        }
    }

    private int calculateAge(String birthdayStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date birthday = sdf.parse(birthdayStr);
            Date today = new Date();
            long ageInMillis = today.getTime() - birthday.getTime();
            return (int) (ageInMillis / (1000L * 60 * 60 * 24 * 365));
        } catch (Exception e) {
            return 0;
        }
    }

    private String suggestNextMeal() {
        SharedPreferences sharedPreferences = getSharedPreferences("FoodRecords", MODE_PRIVATE);
        String records = sharedPreferences.getString("records", "").trim();

        if (records.isEmpty()) {
            return getString(R.string.no_food_records_suggestion);
        }

        String[] recordArray = records.split("\n");
        String lastRecord = recordArray[recordArray.length - 1].trim();

        if (!lastRecord.contains(" - ")) {
            return getString(R.string.invalid_record_format);
        }

        String[] lastRecordParts = lastRecord.split(" - ");
        if (lastRecordParts.length < 3) {
            return getString(R.string.invalid_record_format);
        }

        String lastTime = lastRecordParts[0].trim();
        String lastCalories = lastRecordParts[2].replaceAll("[^0-9]", "").trim();

        if (lastTime.isEmpty() || lastCalories.isEmpty()) {
            return getString(R.string.invalid_record_format);
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date lastDate = sdf.parse(lastTime);
            long timeSinceLastMeal = (new Date().getTime() - lastDate.getTime()) / (1000 * 60 * 60);

            int suggestedCalories = 600;
            if (timeSinceLastMeal >= 4) {
                suggestedCalories += 100;
            }

            return getString(R.string.next_meal_suggestion, timeSinceLastMeal, suggestedCalories, Math.max(0, 4 - timeSinceLastMeal));
        } catch (Exception e) {
            return getString(R.string.cannot_parse_record_time);
        }
    }

    private String calculateUsageDays() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppUsagePrefs", MODE_PRIVATE);
        String firstLaunchDateStr = sharedPreferences.getString("firstLaunchDate", "");
        if (TextUtils.isEmpty(firstLaunchDateStr)) {
            return "0";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date firstLaunchDate = sdf.parse(firstLaunchDateStr);
            Date today = new Date();
            long differenceInMillis = today.getTime() - firstLaunchDate.getTime();
            long days = differenceInMillis / MILLIS_IN_A_DAY;
            return String.valueOf(days);
        } catch (Exception e) {
            Log.e("MainActivity", "Error parsing firstLaunchDate: " + firstLaunchDateStr, e);
            return "0";
        }
    }

    private void calculateAndDisplayCaloriesToEat() {
        SharedPreferences sharedPreferences = getSharedPreferences("SportPrefs", MODE_PRIVATE);
        int targetSportKcal = sharedPreferences.getInt("targetKcal", 0);

        if (targetSportKcal < 0) {
            Toast.makeText(this, getString(R.string.invalid_target_sport_calories), Toast.LENGTH_SHORT).show();
            return;
        }

        String bmrStr = calculateBMR();
        if (bmrStr.equals(getString(R.string.cannot_calculate_missing_data)) || 
            bmrStr.equals(getString(R.string.cannot_calculate_invalid_data))) {
            Toast.makeText(this, getString(R.string.unable_to_calculate_bmr), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double bmr = Double.parseDouble(bmrStr);
            double caloriesToEat = bmr + targetSportKcal;

            TextView caloriesToEatTextView = findViewById(R.id.caloriesToEatTextView);
            if (caloriesToEatTextView != null) {
                caloriesToEatTextView.setText(String.format(Locale.getDefault(), getString(R.string.calories_to_eat_message), caloriesToEat));
            }
        } catch (NumberFormatException e) {
            Log.e("MainActivity", "Error parsing BMR value: " + bmrStr, e);
            Toast.makeText(this, getString(R.string.error_calculating_calories), Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchSportTargetKcal() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid()).child("SportTargetKcal");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int targetKcal = snapshot.getValue(Integer.class);
                        SharedPreferences sharedPreferences = getSharedPreferences("SportPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("targetKcal", targetKcal);
                        editor.apply();
                        Toast.makeText(MainActivity.this, "運動目標卡路里已從雲端同步", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "雲端數據不存在", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(MainActivity.this, "同步失敗: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "用戶未登錄，無法從雲端獲取數據", Toast.LENGTH_SHORT).show();
        }
    }
}