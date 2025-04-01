package com.icewaternotice.healthsync;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class UserDataSyncManager {
    private final DatabaseReference databaseReference;
    private final FirebaseAuth firebaseAuth;
    private final Context context;

    public UserDataSyncManager(Context context, FirebaseAuth firebaseAuth, DatabaseReference databaseReference) {
        this.context = context;
        this.firebaseAuth = firebaseAuth;
        this.databaseReference = databaseReference;
    }

    public void syncData(String key, String value, String displayName) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser != null) {
            performDataSync(currentUser.getUid(), key, value, displayName);
        }
    }

    public void syncAllData(Map<String, String> dataMap) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser != null) {
            performBatchDataSync(currentUser.getUid(), dataMap);
        }
    }

    public void saveUserPreference(String key, String value, int textViewId, String displayPrefix, SettingActivity activity) {
        activity.updateUIBeforeSave();
        SharedPreferences sharedPreferences = activity.getSharedPreferences("UserPrefs", SettingActivity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();

        TextView textView = activity.findViewById(textViewId);
        textView.setText(displayPrefix + value);

        syncData(key, value, displayPrefix.replace("目前", ""));
        activity.updateUIAfterSave();
    }

    public void addDatabaseValueEventListener(String childKey, int textViewId, String prefix, SettingActivity activity) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            databaseReference.child(currentUser.getUid()).child(childKey)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String updatedValue = snapshot.getValue(String.class);
                        if (updatedValue != null) {
                            TextView textView = activity.findViewById(textViewId);
                            textView.setText(prefix + updatedValue);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        activity.showError(prefix + "更新失敗: " + error.getMessage());
                    }
                });
        }
    }

    private FirebaseUser getCurrentUser() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            notifyUser(context.getString(R.string.error_user_not_logged_in));
        }
        return currentUser;
    }

    private void performDataSync(String userId, String key, String value, String displayName) {
        databaseReference.child(userId).child(key).setValue(value)
            .addOnSuccessListener(aVoid -> 
                notifyUser(displayName + context.getString(R.string.sync_success))
            )
            .addOnFailureListener(e -> {
                logError("Data sync failed for key: " + key, e);
                notifyUser(displayName + context.getString(R.string.sync_failure));
            });
    }

    private void performBatchDataSync(String userId, Map<String, String> dataMap) {
        databaseReference.child(userId).setValue(dataMap)
            .addOnSuccessListener(aVoid -> 
                notifyUser(context.getString(R.string.sync_all_success))
            )
            .addOnFailureListener(e -> {
                logError("Batch data sync failed", e);
                notifyUser(context.getString(R.string.sync_all_failure));
            });
    }

    private void notifyUser(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void logError(String message, Exception e) {
        // Log the error (use your preferred logging library)
        // Example: Log.e("UserDataSyncManager", message, e);
    }
}
