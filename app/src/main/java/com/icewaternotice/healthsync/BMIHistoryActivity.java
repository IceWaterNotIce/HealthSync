package com.icewaternotice.healthsync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class BMIHistoryActivity extends BaseActivity {

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_bmi_history;
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_bmi;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_history);

        LineChart lineChart = findViewById(R.id.lineChartBMI);
        ListView bmiHistoryList = findViewById(R.id.bmi_history_list);

        // Read BMI history data
        SharedPreferences sharedPreferences = getSharedPreferences("BMIHistoryPrefs", MODE_PRIVATE);
        String records = sharedPreferences.getString("bmiHistory", "");

        // Log the retrieved history for debugging
        Log.d("BMIHistoryActivity", "Retrieved BMI History: " + records);

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> historyList = new ArrayList<>();

        if (!records.trim().isEmpty()) {
            String[] recordArray = records.split("\n");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            for (int i = 0; i < recordArray.length; i++) {
                String record = recordArray[i].trim();
                if (record.contains(" - ")) {
                    String[] parts = record.split(" - ");
                    if (parts.length >= 2) {
                        try {
                            Date date = sdf.parse(parts[0].trim());
                            float bmi = Float.parseFloat(parts[1].replace("BMI: ", "").trim());
                            entries.add(new Entry(i, bmi));
                            labels.add(parts[0].trim());
                            historyList.add(record); // Add record to history list
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            LineDataSet dataSet = new LineDataSet(entries, "BMI History");
            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);

            XAxis xAxis = lineChart.getXAxis();
            xAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return labels.get((int) value);
                }
            });

            lineChart.invalidate(); // Refresh the chart
        }

        // Populate the ListView with BMI history
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
        bmiHistoryList.setAdapter(adapter);

        // Add a return button to navigate back to BMIActivity
        Button returnButton = findViewById(R.id.btnReturnToBMI);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BMIHistoryActivity.this, BMIActivity.class);
                startActivity(intent);
                finish(); // Close the current activity
            }
        });
    }
}
