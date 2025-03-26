package com.example.healthsync;

import android.os.Bundle;

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
    }
}
