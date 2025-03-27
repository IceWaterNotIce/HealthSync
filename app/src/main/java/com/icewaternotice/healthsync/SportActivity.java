package com.icewaternotice.healthsync;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

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
                // Do nothing
            }
        });
    }
}
