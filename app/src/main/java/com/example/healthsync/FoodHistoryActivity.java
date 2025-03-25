package com.example.healthsync;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class FoodHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_history);

        // 修正 ID 為 historyTextView
        TextView historyTextView = findViewById(R.id.historyTextView);
        loadFoodHistory(historyTextView);

        // 新增刪除歷史記錄按鈕
        Button btnDeleteHistory = findViewById(R.id.btnDeleteHistory);
        btnDeleteHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFoodHistory(historyTextView);
            }
        });

        LineChart lineChart = findViewById(R.id.lineChart);

        // 讀取歷史飲食數據
        SharedPreferences sharedPreferences = getSharedPreferences("FoodRecords", MODE_PRIVATE);
        String records = sharedPreferences.getString("records", "");

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        if (!records.trim().isEmpty()) {
            String[] recordArray = records.split("\n");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            for (int i = 0; i < recordArray.length; i++) {
                String record = recordArray[i].trim();
                if (record.contains(" - ")) {
                    String[] parts = record.split(" - ");
                    if (parts.length >= 3) {
                        try {
                            Date date = sdf.parse(parts[0].trim());
                            float kcal = Float.parseFloat(parts[2].replace(" kcal", "").trim());
                            entries.add(new Entry(i, kcal));
                            labels.add(parts[0].trim());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        // 設置圖表數據
        LineDataSet dataSet = new LineDataSet(entries, "熱量 (kcal)");
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
                if (value >= 0 && value < labels.sa  ize()) {
                    return labels.get((int) value);
                }
                return "";
            }
        });

        lineChart.invalidate(); // 刷新圖表
    }

    private void loadFoodHistory(TextView historyTextView) {
        SharedPreferences sharedPreferences = getSharedPreferences("FoodRecords", MODE_PRIVATE);
        String records = sharedPreferences.getString("records", "尚無記錄");
        historyTextView.setText(records);
    }

    private void deleteFoodHistory(TextView historyTextView) {
        SharedPreferences sharedPreferences = getSharedPreferences("FoodRecords", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("records");
        editor.apply();
        historyTextView.setText("尚無記錄");
        Toast.makeText(this, "歷史記錄已刪除", Toast.LENGTH_SHORT).show();
    }
}
