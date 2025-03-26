package com.icewaternotice.healthsync;

import android.Manifest;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PhotoKcalActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textViewResponse;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_kcal);

        imageView = findViewById(R.id.imageView);
        textViewResponse = findViewById(R.id.textViewResponse);
        Button btnCaptureOrSelect = findViewById(R.id.btnCaptureOrSelect);

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result) {
                        imageView.setImageURI(photoUri);
                        callSiliconFlowChatAPI(photoUri);
                    }
                }
        );

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageView.setImageURI(uri);
                        callSiliconFlowChatAPI(uri);
                    }
                }
        );

        btnCaptureOrSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PhotoKcalActivity.this);
                builder.setTitle("選擇操作")
                        .setItems(new String[]{"拍照", "從圖庫中選擇"}, (dialog, which) -> {
                            if (which == 0) {
                                ContentValues values = new ContentValues();
                                values.put(MediaStore.Images.Media.DISPLAY_NAME, "new_image.jpg");
                                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                                photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                                if (photoUri != null) {
                                    takePictureLauncher.launch(photoUri);
                                }
                            } else if (which == 1) {
                                pickImageLauncher.launch("image/*");
                            }
                        });
                builder.create().show();
            }
        });

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        Button btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 返回上一個活動
            }
        });
    }

    private void callSiliconFlowChatAPI(Uri imageUri) {
        String base64Image = convertImageToBase64(imageUri);
        if (base64Image == null) return;

        new Thread(() -> {
            try {
                URL url = new URL("https://api.siliconflow.cn/v1/chat/completions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer sk-xnvitykopqdsmersqggkjagqhptdxgavumyurswpxxaeqzbp");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject jsonParam = new JSONObject();
                jsonParam.put("model", "Qwen/QwQ-32B");
                jsonParam.put("stream", false);
                jsonParam.put("max_tokens", 512);
                jsonParam.put("temperature", 0.7);
                jsonParam.put("top_p", 0.7);
                jsonParam.put("top_k", 50);
                jsonParam.put("frequency_penalty", 0.5);
                jsonParam.put("n", 1);

                JSONArray messages = new JSONArray();
                JSONObject message = new JSONObject();
                message.put("content", "What opportunities and challenges will the Chinese large model industry face in 2025?");
                message.put("role", "user");
                messages.put(message);
                jsonParam.put("messages", messages);
                jsonParam.put("stop", new JSONArray());

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonParam.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                runOnUiThread(() -> textViewResponse.setText("API 呼叫成功"));

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private String convertImageToBase64(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
