package com.icewaternotice.healthsync;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class BMIActivity extends BaseActivity {

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_bmi;
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_bmi;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Button to navigate to BMICalculatorActivity
        Button btnGoToBMICalculator = findViewById(R.id.btnGoToBMICalculator);
        btnGoToBMICalculator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BMIActivity.this, BMICalculatorActivity.class);
                startActivity(intent);
            }
        });

        // Button to navigate to BMIHistoryActivity
        Button btnGoToBMIHistory = findViewById(R.id.btnGoToBMIHistory);
        btnGoToBMIHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BMIActivity.this, BMIHistoryActivity.class);
                startActivity(intent);
            }
        });

        // ...existing code for BMIActivity logic (if any)...
    }
}
