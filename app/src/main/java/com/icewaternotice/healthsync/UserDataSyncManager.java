package com.icewaternotice.healthsync;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class UserDataSyncManager {
    private final DatabaseReference databaseReference;
    private final FirebaseAuth firebaseAuth;
    private final Context context;

    public UserDataSyncManager(Context context) {
        this.context = context;
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    public void syncData(String key, String value, String displayName) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            performDataSync(currentUser.getUid(), key, value, displayName);
        } else {
            notifyUser("用戶未登錄，無法同步 " + displayName);
        }
    }

    public void syncAllData(Map<String, String> dataMap) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            performBatchDataSync(currentUser.getUid(), dataMap);
        } else {
            notifyUser("用戶未登錄，無法同步所有數據");
        }
    }

    private void performDataSync(String userId, String key, String value, String displayName) {
        databaseReference.child(userId).child(key).setValue(value)
            .addOnSuccessListener(aVoid -> 
                notifyUser(displayName + " 已同步至 Firebase")
            )
            .addOnFailureListener(e -> 
                notifyUser(displayName + " 同步失敗: " + e.getMessage())
            );
    }

    private void performBatchDataSync(String userId, Map<String, String> dataMap) {
        databaseReference.child(userId).setValue(dataMap)
            .addOnSuccessListener(aVoid -> 
                notifyUser("所有數據已同步至 Firebase")
            )
            .addOnFailureListener(e -> 
                notifyUser("批量同步失敗: " + e.getMessage())
            );
    }

    private void notifyUser(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
