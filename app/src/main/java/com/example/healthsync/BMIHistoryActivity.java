package com.example.healthsync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Arrays;

public class BMIHistoryActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BMIHistoryPrefs";
    private static final String HISTORY_KEY = "bmiHistory";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_history);

        ListView listView = findViewById(R.id.bmi_history_list);

        ArrayList<String> bmiHistory = loadHistory();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bmiHistory);
        listView.setAdapter(adapter);
    }

    private ArrayList<String> loadHistory() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String history = prefs.getString(HISTORY_KEY, "");
        if (history.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(history.split("\n\n")));
    }
}
