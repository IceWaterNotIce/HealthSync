package com.example.healthsync;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;

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
    }
}
