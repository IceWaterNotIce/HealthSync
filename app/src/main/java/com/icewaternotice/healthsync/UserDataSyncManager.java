package com.icewaternotice.healthsync;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
            databaseReference.child(currentUser.getUid()).child(key).setValue(value)
                .addOnSuccessListener(aVoid -> 
                    Toast.makeText(context, displayName + " 已同步至 Firebase", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e -> 
                    Toast.makeText(context, displayName + " 同步失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
        } else {
            Toast.makeText(context, "用戶未登錄，無法同步 " + displayName, Toast.LENGTH_SHORT).show();
        }
    }
}
