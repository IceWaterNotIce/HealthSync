package com.icewaternotice.healthsync;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
                if (value >= 0 && value < labels.size()) { // 修正拼寫錯誤
                    return labels.get((int) value);
                }
                return "";
            }
        });

        lineChart.invalidate(); // 刷新圖表

        Button btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 返回上一個活動
            }
        });
    }

    private void loadFoodHistory(TextView historyTextView) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid()).child("FoodRecords");
            databaseReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    String records = task.getResult().getValue(String.class);
                    historyTextView.setText(records != null ? records : "尚無記錄");
                } else {
                    Toast.makeText(this, "無法讀取記錄，請檢查 Firebase 權限設置", Toast.LENGTH_SHORT).show();
                    historyTextView.setText("尚無記錄");
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "讀取失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "用戶未登錄，無法讀取記錄", Toast.LENGTH_SHORT).show();
            historyTextView.setText("尚無記錄");
        }
    }

    private void deleteFoodHistory(TextView historyTextView) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(currentUser.getUid()).child("FoodRecords");
            databaseReference.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    historyTextView.setText("尚無記錄");
                    Toast.makeText(this, "歷史記錄已刪除", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "刪除失敗，請檢查 Firebase 權限設置", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "刪除失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "用戶未登錄，無法刪除記錄", Toast.LENGTH_SHORT).show();
        }
    }
}
