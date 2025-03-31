package com.icewaternotice.healthsync;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;

public class SettingActivity extends BaseActivity {

    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 100;

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
        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set up UI elements
        Button btnLinkGoogleAccount = findViewById(R.id.btnLinkGoogleAccount);
        TextView txtAccountInfo = findViewById(R.id.txtAccountInfo);
        ImageView imgProfilePicture = findViewById(R.id.imgProfilePicture);

        // Check if already signed in
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            displayAccountInfo(account, txtAccountInfo, imgProfilePicture);
            btnLinkGoogleAccount.setText("Unlink Google Account");
        } else {
            btnLinkGoogleAccount.setText("Link Google Account");
        }

        // Handle button click for Google Sign-In/Sign-Out
        btnLinkGoogleAccount.setOnClickListener(v -> {
            GoogleSignInAccount currentAccount = GoogleSignIn.getLastSignedInAccount(this);
            if (currentAccount != null) {
                // Unlink account
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            task.addOnCompleteListener(this, completedTask -> {
                TextView txtAccountInfo = findViewById(R.id.txtAccountInfo);
                ImageView imgProfilePicture = findViewById(R.id.imgProfilePicture);
                Button btnLinkGoogleAccount = findViewById(R.id.btnLinkGoogleAccount);
                if (completedTask.isSuccessful()) {
                    GoogleSignInAccount account = completedTask.getResult();
                    if (account != null) {
                        displayAccountInfo(account, txtAccountInfo, imgProfilePicture);
                        btnLinkGoogleAccount.setText("Unlink Google Account");
                    } else {
                        txtAccountInfo.setText("Account retrieval failed: Account is null.");
                    }
                } else {
                    txtAccountInfo.setText("Account retrieval failed: " + completedTask.getException().getMessage());
                }
            });
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
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("gender", gender);
        editor.apply();
        Toast.makeText(this, "性別已保存: " + gender, Toast.LENGTH_SHORT).show();

        TextView txtGenderInfo = findViewById(R.id.txtGenderInfo);
        txtGenderInfo.setText("目前性別: " + gender);
    }
}
