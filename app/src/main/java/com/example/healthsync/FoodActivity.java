package com.example.healthsync;

import android.os.Bundle;

public class FoodActivity extends BaseActivity {

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_food;
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_food;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ...existing code for FoodActivity logic (if any)...
    }
}
