package com.icewaternotice.healthsync;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.widget.Toast;

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
    }
}
