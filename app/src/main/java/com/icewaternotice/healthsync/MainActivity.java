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

import com.icewaternotice.healthsync.utils.CalculationUtils; // Import the new utility class

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

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        UserDataSyncManager userDataSyncManager = new UserDataSyncManager(this, firebaseAuth, databaseReference);

        showProgressDialog();
        syncUserData(userDataSyncManager); // Sync user data when the app opens
        hideProgressDialog();

        // 顯示目標 BMI
        TextView targetBMITextView = findViewById(R.id.targetBMITextView);
        String targetBMI = CalculationUtils.calculateTargetBMI(this);
        targetBMITextView.setText(getString(R.string.target_bmi_message, targetBMI));

        // 顯示 BMR
        TextView bmrTextView = findViewById(R.id.bmrTextView);
        String bmr = CalculationUtils.calculateBMR(this);
        bmrTextView.setText(getString(R.string.bmr_message, bmr));

        // 請求權限
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        // 建議下一餐
        TextView suggestionTextView = findViewById(R.id.suggestionTextView);
        String suggestion = CalculationUtils.suggestNextMeal(this);
        suggestionTextView.setText(suggestion);

        // 確保在應用啟動時正確顯示建議
        suggestionTextView.post(() -> suggestionTextView.setText(CalculationUtils.suggestNextMeal(this)));

        // 保存首次啟動日期
        SharedPreferences sharedPreferences = getSharedPreferences("AppUsagePrefs", MODE_PRIVATE);
        if (!sharedPreferences.contains("firstLaunchDate")) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            sharedPreferences.edit().putString("firstLaunchDate", currentDate).apply();
        }

        // 計算並顯示使用天數
        TextView usageDaysTextView = findViewById(R.id.usageDaysTextView);
        String usageDays = CalculationUtils.calculateUsageDays(this);
        usageDaysTextView.setText(getString(R.string.usage_days_message, usageDays));

        // 從 Firebase 獲取運動目標卡路里
        fetchSportTargetKcal();

        // 計算並顯示今日所需卡路里
        CalculationUtils.calculateAndDisplayCaloriesToEat(this, findViewById(R.id.caloriesToEatTextView));

        // 載入步數
        loadStepCount();

        // 監聽步數變化
        listenToStepCountChanges();
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

    private void syncUserData(UserDataSyncManager userDataSyncManager) {
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
        String suggestion = CalculationUtils.suggestNextMeal(this);
        suggestionTextView.setText(suggestion);
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

    private void loadStepCount() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid()).child("StepCount");
            databaseReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    int steps = task.getResult().getValue(Integer.class);
                    TextView stepCountTextView = findViewById(R.id.stepCountTextView);
                    stepCountTextView.setText(getString(R.string.steps_display, steps));
                } else {
                    Toast.makeText(this, "無法載入步數，請檢查 Firebase 設定", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "載入步數失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "用戶未登錄，無法載入步數", Toast.LENGTH_SHORT).show();
        }
    }

    private void listenToStepCountChanges() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid()).child("StepCount");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        int steps = snapshot.getValue(Integer.class);
                        TextView stepCountTextView = findViewById(R.id.stepCountTextView);
                        stepCountTextView.setText(getString(R.string.steps_display, steps));
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(MainActivity.this, "步數監聽失敗: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}