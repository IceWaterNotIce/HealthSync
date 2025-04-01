package com.icewaternotice.healthsync.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.icewaternotice.healthsync.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CalculationUtils {
    private static final long MILLIS_IN_A_DAY = 1000L * 60 * 60 * 24;

    public static String calculateTargetBMI(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("BMIHistoryPrefs", Context.MODE_PRIVATE);
        String bmiHistory = sharedPreferences.getString("bmiHistory", "");

        if (bmiHistory.isEmpty()) {
            return context.getString(R.string.cannot_suggest_no_history);
        }

        try {
            String[] records = bmiHistory.split("\n");
            String lastRecord = records[records.length - 1].trim();
            if (!lastRecord.contains("BMI: ")) {
                return context.getString(R.string.cannot_suggest_invalid_format);
            }

            String bmiValue = lastRecord.split("BMI: ")[1].trim();
            double lastBMI = Double.parseDouble(bmiValue);

            if (lastBMI < 18.5) {
                return context.getString(R.string.suggest_bmi_increase);
            } else if (lastBMI > 24.9) {
                return context.getString(R.string.suggest_bmi_decrease);
            } else {
                return context.getString(R.string.suggest_bmi_maintain);
            }
        } catch (Exception e) {
            return context.getString(R.string.cannot_suggest_invalid_format);
        }
    }

    public static String calculateBMR(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String heightStr = sharedPreferences.getString("height", "");
        String weightStr = sharedPreferences.getString("weight", "");
        String birthdayStr = sharedPreferences.getString("birthday", "");
        String gender = sharedPreferences.getString("gender", "male");

        if (heightStr.isEmpty() || weightStr.isEmpty() || birthdayStr.isEmpty()) {
            return context.getString(R.string.cannot_calculate_missing_data);
        }

        try {
            double height = Double.parseDouble(heightStr);
            double weight = Double.parseDouble(weightStr);
            int age = calculateAge(birthdayStr);

            double bmr = gender.equalsIgnoreCase("male")
                    ? 88.362 + (13.397 * weight) + (4.799 * height) - (5.677 * age)
                    : 447.593 + (9.247 * weight) + (3.098 * height) - (4.330 * age);

            return String.format(Locale.getDefault(), "%.2f", bmr);
        } catch (Exception e) {
            return context.getString(R.string.cannot_calculate_invalid_data);
        }
    }

    public static int calculateAge(String birthdayStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date birthday = sdf.parse(birthdayStr);
            Date today = new Date();
            long ageInMillis = today.getTime() - birthday.getTime();
            return (int) (ageInMillis / (1000L * 60 * 60 * 24 * 365));
        } catch (Exception e) {
            return 0;
        }
    }

    public static String suggestNextMeal(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("FoodRecords", Context.MODE_PRIVATE);
        String records = sharedPreferences.getString("records", "").trim();

        if (records.isEmpty()) {
            return context.getString(R.string.no_food_records_suggestion);
        }

        String[] recordArray = records.split("\n");
        String lastRecord = recordArray[recordArray.length - 1].trim();

        if (!lastRecord.contains(" - ")) {
            return context.getString(R.string.invalid_record_format);
        }

        String[] lastRecordParts = lastRecord.split(" - ");
        if (lastRecordParts.length < 3) {
            return context.getString(R.string.invalid_record_format);
        }

        String lastTime = lastRecordParts[0].trim();
        String lastCalories = lastRecordParts[2].replaceAll("[^0-9]", "").trim();

        if (lastTime.isEmpty() || lastCalories.isEmpty()) {
            return context.getString(R.string.invalid_record_format);
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date lastDate = sdf.parse(lastTime);
            long timeSinceLastMeal = (new Date().getTime() - lastDate.getTime()) / (1000 * 60 * 60);

            int suggestedCalories = 600;
            if (timeSinceLastMeal >= 4) {
                suggestedCalories += 100;
            }

            return context.getString(R.string.next_meal_suggestion, timeSinceLastMeal, suggestedCalories, Math.max(0, 4 - timeSinceLastMeal));
        } catch (Exception e) {
            return context.getString(R.string.cannot_parse_record_time);
        }
    }

    public static String calculateUsageDays(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("AppUsagePrefs", Context.MODE_PRIVATE);
        String firstLaunchDateStr = sharedPreferences.getString("firstLaunchDate", "");
        if (TextUtils.isEmpty(firstLaunchDateStr)) {
            return "0";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date firstLaunchDate = sdf.parse(firstLaunchDateStr);
            Date today = new Date();
            long differenceInMillis = today.getTime() - firstLaunchDate.getTime();
            long days = differenceInMillis / MILLIS_IN_A_DAY;
            return String.valueOf(days);
        } catch (Exception e) {
            Log.e("CalculationUtils", "Error parsing firstLaunchDate: " + firstLaunchDateStr, e);
            return "0";
        }
    }

    public static void calculateAndDisplayCaloriesToEat(Context context, TextView caloriesToEatTextView) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("SportPrefs", Context.MODE_PRIVATE);
        int targetSportKcal = sharedPreferences.getInt("targetKcal", 0);

        if (targetSportKcal < 0) {
            Toast.makeText(context, context.getString(R.string.invalid_target_sport_calories), Toast.LENGTH_SHORT).show();
            return;
        }

        String bmrStr = calculateBMR(context);
        if (bmrStr.equals(context.getString(R.string.cannot_calculate_missing_data)) ||
            bmrStr.equals(context.getString(R.string.cannot_calculate_invalid_data))) {
            Toast.makeText(context, context.getString(R.string.unable_to_calculate_bmr), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double bmr = Double.parseDouble(bmrStr);
            double caloriesToEat = bmr + targetSportKcal;

            if (caloriesToEatTextView != null) {
                caloriesToEatTextView.setText(String.format(Locale.getDefault(), context.getString(R.string.calories_to_eat_message), caloriesToEat));
            }
        } catch (NumberFormatException e) {
            Log.e("CalculationUtils", "Error parsing BMR value: " + bmrStr, e);
            Toast.makeText(context, context.getString(R.string.error_calculating_calories), Toast.LENGTH_SHORT).show();
        }
    }

    public static float calculateBMI(String heightStr, String weightStr) throws NumberFormatException {
        float height = Float.parseFloat(heightStr) / 100; // Convert cm to meters
        float weight = Float.parseFloat(weightStr);
        return weight / (height * height);
    }

    public static String getBMICategory(float bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi >= 18.5 && bmi < 24.9) {
            return "Normal weight";
        } else if (bmi >= 25 && bmi < 29.9) {
            return "Overweight";
        } else {
            return "Obesity";
        }
    }
}
