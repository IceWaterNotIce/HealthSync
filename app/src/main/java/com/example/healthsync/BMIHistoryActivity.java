package com.example.healthsync;

import android.content.SharedPreferences;
import android.os.Bundle;
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

public class BMIHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi_history);

        LineChart lineChart = findViewById(R.id.lineChartBMI);

        // 讀取 BMI 歷史數據
        SharedPreferences sharedPreferences = getSharedPreferences("BMIRecords", MODE_PRIVATE);
        String records = sharedPreferences.getString("records", "");

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

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
                            float bmi = Float.parseFloat(parts[1].trim());
                            entries.add(new Entry(i, bmi));
                            labels.add(parts[0].trim());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        // 設置圖表數據
        LineDataSet dataSet = new LineDataSet(entries, "BMI");
        dataSet.setColor(getResources().getColor(R.color.purple_500));
        dataSet.setValueTextColor(getResources().getColor(R.color.black));

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // 配置 X 軸
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 0 && value < labels.size()) {
                    return labels.get((int) value);
                }
                return "";
            }
        });

        lineChart.invalidate(); // 刷新圖表
    }
}
