package com.icewaternotice.healthsync;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StepCounterActivity extends BaseActivity implements SensorEventListener {

        @Override
        protected int getLayoutResourceId() {
            return R.layout.activity_step_counter; // 修正為正確的佈局資源 ID
        }
    
        @Override
        protected int getCurrentMenuItemId() {
            return R.id.nav_sport; // 修正為正確的菜單項目 ID
        }
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;
    private TextView stepCountTextView;
    private int initialStepCount = -1; // 用於儲存初始步數

    private static final String PREFS_NAME = "StepCounterPrefs";
    private static final String PREF_INITIAL_STEPS = "initialSteps";
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    

        stepCountTextView = findViewById(R.id.stepCountTextView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
            if (stepCounterSensor == null) {
                Toast.makeText(this, "Step Counter Sensor not available!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        // 檢查並請求 ACTIVITY_RECOGNITION 權限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 1);
            }
        }

        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        initialStepCount = sharedPreferences.getInt(PREF_INITIAL_STEPS, -1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied. Step counter may not work.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        sharedPreferences.edit().putInt(PREF_INITIAL_STEPS, initialStepCount).apply(); // 儲存初始步數
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (event.values != null && event.values.length > 0) {
                int totalSteps = (int) event.values[0];
                if (initialStepCount == -1) {
                    initialStepCount = totalSteps; // 儲存初始步數
                }
                int stepsSinceStart = totalSteps - initialStepCount;

                // 使用資源檔案中的字串資源來顯示步數
                String stepText = getString(R.string.steps_display, stepsSinceStart);
                stepCountTextView.setText(stepText); // 顯示步數

                // 儲存步數到本地
                sharedPreferences.edit().putInt(PREF_INITIAL_STEPS, initialStepCount).apply();

                // 儲存步數到雲端
                saveStepsToCloud(stepsSinceStart);
            } else {
                // 處理無效數據的情況
                Toast.makeText(this, R.string.sensor_data_invalid, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveStepsToCloud(int steps) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid()).child("StepCount");
            databaseReference.setValue(steps).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "步數已同步到雲端", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "步數同步失敗", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "步數同步失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "用戶未登錄，無法同步步數", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }
}
