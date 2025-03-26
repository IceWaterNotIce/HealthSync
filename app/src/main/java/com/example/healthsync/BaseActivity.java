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
            // Set the selected item based on the current activity
            int currentMenuItemId = getCurrentMenuItemId();
            if (currentMenuItemId != 0) {
                bottomNavigationView.setSelectedItemId(currentMenuItemId);
            }

            // Handle navigation item selection
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
