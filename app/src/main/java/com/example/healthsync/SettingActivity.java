package com.example.healthsync;

import android.os.Bundle;

public class SettingActivity extends BaseActivity {

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_setting;
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_setting;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ...新增 SettingActivity 的邏輯...
    }
}
