package com.icewaternotice.healthsync;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class SportActivity extends BaseActivity {

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_sport;
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_sport;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ...新增 SportActivity 的邏輯...

        Button stepCounterButton = findViewById(R.id.btnStepCounter);
        stepCounterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SportActivity.this, StepCounterActivity.class));
            }
        });

        TextView tvSportTarget = findViewById(R.id.tvSportTarget);
        SeekBar seekBarSportTarget = findViewById(R.id.seekBarSportTarget);

        // 載入儲存的運動目標卡路里
        SharedPreferences sharedPreferences = getSharedPreferences("SportPrefs", MODE_PRIVATE);
        int savedTargetKcal = sharedPreferences.getInt("targetKcal", 0);
        tvSportTarget.setText("運動目標卡路里: " + savedTargetKcal + " kcal");
        seekBarSportTarget.setProgress(savedTargetKcal);

        // 從 Firebase 獲取最新的運動目標卡路里
        fetchSportTargetKcal(tvSportTarget, seekBarSportTarget);

        seekBarSportTarget.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvSportTarget.setText("運動目標卡路里: " + progress + " kcal");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                saveTargetKcal(seekBar.getProgress());
            }
        });
    }

    private void saveTargetKcal(int targetKcal) {
        SharedPreferences sharedPreferences = getSharedPreferences("SportPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("targetKcal", targetKcal);
        editor.apply();
        Toast.makeText(this, "運動目標卡路里已保存: " + targetKcal + " kcal", Toast.LENGTH_SHORT).show();

        // 同步到 Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid()).child("SportTargetKcal");
            databaseReference.setValue(targetKcal).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "運動目標卡路里已同步到雲端，並將更新到其他設備", Toast.LENGTH_SHORT).show();
                    notifyOtherDevices(targetKcal);
                } else {
                    Toast.makeText(this, "同步到雲端失敗", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "同步失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "用戶未登錄，無法同步到雲端", Toast.LENGTH_SHORT).show();
        }
    }

    private void notifyOtherDevices(int targetKcal) {
        // 此方法可以擴展為使用 Firebase Cloud Messaging (FCM) 通知其他設備
        // 或者在 Firebase Database 中設置標誌以觸發其他設備的更新
        Toast.makeText(this, "通知其他設備更新運動目標卡路里: " + targetKcal + " kcal", Toast.LENGTH_SHORT).show();
    }

    private void fetchSportTargetKcal(TextView tvSportTarget, SeekBar seekBarSportTarget) {
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

                        tvSportTarget.setText("運動目標卡路里: " + targetKcal + " kcal");
                        seekBarSportTarget.setProgress(targetKcal);
                        Toast.makeText(SportActivity.this, "運動目標卡路里已從雲端同步", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SportActivity.this, "雲端數據不存在", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(SportActivity.this, "同步失敗: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "用戶未登錄，無法從雲端獲取數據", Toast.LENGTH_SHORT).show();
        }
    }
}
