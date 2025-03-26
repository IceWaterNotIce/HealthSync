package com.example.healthsync;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            int currentMenuItemId = getCurrentMenuItemId();
            if (currentMenuItemId != 0) {
                bottomNavigationView.setSelectedItemId(currentMenuItemId); // 確保高亮當前活動
            }

            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_food && !(this instanceof FoodActivity)) {
                    startActivity(new Intent(this, FoodActivity.class));
                    return true;
                } else if (itemId == R.id.nav_home && !(this instanceof MainActivity)) {
                    startActivity(new Intent(this, MainActivity.class));
                    return true;
                } else if (itemId == R.id.nav_bmi && !(this instanceof BMIActivity)) {
                    startActivity(new Intent(this, BMIActivity.class));
                    return true;
                } else if (itemId == R.id.nav_sport && !(this instanceof SportActivity)) {
                    startActivity(new Intent(this, SportActivity.class));
                    return true;
                } else if (itemId == R.id.nav_setting && !(this instanceof SettingActivity)) {
                    startActivity(new Intent(this, SettingActivity.class));
                    return true;
                }
                return false;
            });
        }
    }

    /**
     * Override this method in child activities to specify the current menu item ID.
     */
    protected abstract int getCurrentMenuItemId();

    protected abstract int getLayoutResourceId();
}
