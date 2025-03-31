package com.icewaternotice.healthsync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.view.View;
import android.widget.ProgressBar;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.auth.UserProfileChangeRequest;
import java.util.Calendar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingActivity extends BaseActivity {

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private static final int RC_SIGN_IN = 100;
    private ProgressBar progressBar;
    private UserDataSyncManager userDataSyncManager;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_setting;
    }

    @Override
    protected int getCurrentMenuItemId() {
        return R.id.nav_setting;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting); // 確保設置正確的佈局
        userDataSyncManager = new UserDataSyncManager(this);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Use your Firebase Web Client ID
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        // Set up UI elements
        Button btnLinkGoogleAccount = findViewById(R.id.btnLinkGoogleAccount);
        TextView txtAccountInfo = findViewById(R.id.txtAccountInfo);
        ImageView imgProfilePicture = findViewById(R.id.imgProfilePicture);
        progressBar = findViewById(R.id.progressBar); // 確保佈局中有 progressBar 元件
        if (progressBar == null) {
            throw new IllegalStateException("進度條 (progressBar) 未在佈局中定義，請檢查 activity_setting.xml");
        }
        progressBar.setVisibility(View.GONE); // 默認隱藏

        // Add display name editing functionality
        TextView txtDisplayName = findViewById(R.id.txtDisplayName);
        Button btnEditDisplayName = findViewById(R.id.btnEditDisplayName);

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            txtDisplayName.setText(currentUser.getDisplayName());
        }

        btnEditDisplayName.setOnClickListener(v -> {
            if (currentUser != null) {
                String newDisplayName = txtDisplayName.getText().toString().trim();
                if (!newDisplayName.isEmpty()) {
                    updateDisplayName(newDisplayName);
                } else {
                    Toast.makeText(this, "顯示名稱不能為空", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "用戶未登錄，無法更新顯示名稱", Toast.LENGTH_SHORT).show();
            }
        });

        // Listen for changes in display name
        if (currentUser != null) {
            databaseReference.child(currentUser.getUid()).child("displayName").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String updatedDisplayName = snapshot.getValue(String.class);
                    if (updatedDisplayName != null) {
                        txtDisplayName.setText(updatedDisplayName);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(SettingActivity.this, "顯示名稱更新失敗: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Check if already signed in
        if (currentUser != null) {
            displayFirebaseUserInfo(currentUser, txtAccountInfo, imgProfilePicture);
            btnLinkGoogleAccount.setText("Unlink Google Account");
        } else {
            btnLinkGoogleAccount.setText("Link Google Account");
        }

        // Handle button click for Google Sign-In/Sign-Out
        btnLinkGoogleAccount.setOnClickListener(v -> {
            if (firebaseAuth.getCurrentUser() != null) {
                // Unlink account
                firebaseAuth.signOut();
                googleSignInClient.signOut().addOnCompleteListener(task -> {
                    txtAccountInfo.setText("No account linked.");
                    imgProfilePicture.setImageResource(R.drawable.default_profile_picture); // Fallback image
                    btnLinkGoogleAccount.setText("Link Google Account");
                });
            } else {
                // Link account
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        // Add gender selection buttons
        Button btnMale = findViewById(R.id.btnMale);
        Button btnFemale = findViewById(R.id.btnFemale);

        // Load saved gender
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedGender = sharedPreferences.getString("gender", "未設定");
        TextView txtGenderInfo = findViewById(R.id.txtGenderInfo);
        txtGenderInfo.setText("目前性別: " + savedGender);

        btnMale.setOnClickListener(v -> saveGender("男"));
        btnFemale.setOnClickListener(v -> saveGender("女"));

        // Add birthday selection
        TextView txtBirthdayInfo = findViewById(R.id.txtBirthdayInfo);
        Button btnSelectBirthday = findViewById(R.id.btnSelectBirthday);

        // Load saved birthday
        String savedBirthday = sharedPreferences.getString("birthday", "未設定");
        txtBirthdayInfo.setText("生日: " + savedBirthday);

        btnSelectBirthday.setOnClickListener(v -> showDatePickerDialog(txtBirthdayInfo));

        // Listen for changes in gender and birthday
        if (currentUser != null) {
            databaseReference.child(currentUser.getUid()).child("gender").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String updatedGender = snapshot.getValue(String.class);
                    if (updatedGender != null) {
                        txtGenderInfo.setText("目前性別: " + updatedGender);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(SettingActivity.this, "性別更新失敗: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            databaseReference.child(currentUser.getUid()).child("birthday").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String updatedBirthday = snapshot.getValue(String.class);
                    if (updatedBirthday != null) {
                        txtBirthdayInfo.setText("生日: " + updatedBirthday);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(SettingActivity.this, "生日更新失敗: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            // Highlight the current menu item
            bottomNavigationView.setSelectedItemId(R.id.nav_setting);

            bottomNavigationView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(SettingActivity.this, MainActivity.class));
                    return true;
                } else if (itemId == R.id.nav_bmi) {
                    startActivity(new Intent(SettingActivity.this, BMIActivity.class));
                    return true;
                } else if (itemId == R.id.nav_food) {
                    startActivity(new Intent(SettingActivity.this, FoodActivity.class));
                    return true;
                } else if (itemId == R.id.nav_sport) {
                    startActivity(new Intent(SettingActivity.this, SportActivity.class));
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                GoogleSignInAccount account = task.getResult();
                if (account != null) {
                    firebaseAuthWithGoogle(account);
                }
            } else {
                Toast.makeText(this, "Google Sign-In 失敗: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        updateUIBeforeSave();
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    TextView txtAccountInfo = findViewById(R.id.txtAccountInfo);
                    ImageView imgProfilePicture = findViewById(R.id.imgProfilePicture);
                    Button btnLinkGoogleAccount = findViewById(R.id.btnLinkGoogleAccount);
                    displayFirebaseUserInfo(user, txtAccountInfo, imgProfilePicture);
                    btnLinkGoogleAccount.setText("Unlink Google Account");
                }
            } else {
                Toast.makeText(this, "Firebase 認證失敗: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
            updateUIAfterSave();
        });
    }

    private void displayFirebaseUserInfo(FirebaseUser user, TextView txtAccountInfo, ImageView imgProfilePicture) {
        String accountInfo = "Name: " + user.getDisplayName() + "\nEmail: " + user.getEmail();
        txtAccountInfo.setText(accountInfo);

        // Load profile image
        if (user.getPhotoUrl() != null) {
            Glide.with(this).load(user.getPhotoUrl()).into(imgProfilePicture);
        } else {
            imgProfilePicture.setImageResource(R.drawable.default_profile_picture); // Fallback image
        }
    }

    private void displayAccountInfo(GoogleSignInAccount account, TextView txtAccountInfo, ImageView imgProfilePicture) {
        if (account != null) {
            String accountInfo = "Name: " + account.getDisplayName() + "\nEmail: " + account.getEmail();
            txtAccountInfo.setText(accountInfo);

            // Load profile image
            if (account.getPhotoUrl() != null) {
                Glide.with(this).load(account.getPhotoUrl()).into(imgProfilePicture);
            } else {
                imgProfilePicture.setImageResource(R.drawable.default_profile_picture); // Fallback image
            }
        } else {
            txtAccountInfo.setText("No account info available.");
            imgProfilePicture.setImageResource(R.drawable.default_profile_picture); // Fallback image
        }
    }

    private void saveGender(String gender) {
        updateUIBeforeSave();
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("gender", gender);
        editor.apply();

        TextView txtGenderInfo = findViewById(R.id.txtGenderInfo);
        txtGenderInfo.setText("目前性別: " + gender);

        userDataSyncManager.syncData("gender", gender, "性別");
    }

    private void saveBirthday(String birthday) {
        updateUIBeforeSave();
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("birthday", birthday);
        editor.apply();

        userDataSyncManager.syncData("birthday", birthday, "生日");
    }

    private void syncWithFirebase(String key, String value, String displayName) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            databaseReference.child(currentUser.getUid()).child(key).setValue(value)
                .addOnSuccessListener(aVoid -> {
                    if (key.equals("displayName")) {
                        currentUser.updateProfile(new UserProfileChangeRequest.Builder()
                                .setDisplayName(value)
                                .build());
                    }
                    Toast.makeText(this, displayName + "已同步至 Firebase", Toast.LENGTH_SHORT).show();
                    updateUIAfterSave();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, displayName + "同步失敗: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateUIAfterSave();
                });
        } else {
            Toast.makeText(this, "用戶未登錄，無法同步 " + displayName, Toast.LENGTH_SHORT).show();
            updateUIAfterSave();
        }
    }

    private void updateUIBeforeSave() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void updateUIAfterSave() {
        progressBar.setVisibility(View.GONE);
    }

    private void showDatePickerDialog(TextView txtBirthdayInfo) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String birthday = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
            saveBirthday(birthday);
            txtBirthdayInfo.setText("生日: " + birthday);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void updateDisplayName(String newDisplayName) {
        updateUIBeforeSave();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.updateProfile(new UserProfileChangeRequest.Builder()
                    .setDisplayName(newDisplayName)
                    .build())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        TextView txtAccountInfo = findViewById(R.id.txtAccountInfo);
                        String updatedAccountInfo = "Name: " + newDisplayName + "\nEmail: " + currentUser.getEmail();
                        txtAccountInfo.setText(updatedAccountInfo);
                        Toast.makeText(this, "顯示名稱已更新", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "顯示名稱更新失敗: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    updateUIAfterSave();
                });
        }
    }
}
