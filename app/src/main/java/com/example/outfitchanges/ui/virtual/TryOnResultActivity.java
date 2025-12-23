package com.example.outfitchanges.ui.virtual;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.outfitchanges.R;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TryOnResultActivity extends AppCompatActivity {
    private ImageView imageResult;
    private LinearLayout btnRetry;
    private LinearLayout btnSave;
    private String resultUrl;
    private String personImageFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tryon_result);

        resultUrl = getIntent().getStringExtra("result_url");
        personImageFilePath = getIntent().getStringExtra("person_image_file");

        initViews();
        loadResultImage();
        setupClickListeners();
    }

    private void initViews() {
        imageResult = findViewById(R.id.image_result);
        btnRetry = findViewById(R.id.btn_retry);
        btnSave = findViewById(R.id.btn_save);
    }

    private void loadResultImage() {
        if (resultUrl != null && !resultUrl.isEmpty()) {
            Glide.with(this)
                    .load(resultUrl)
                    .into(imageResult);
        }
    }

    private void setupClickListeners() {
        btnRetry.setOnClickListener(v -> {
            // 返回上一页
            finish();
        });

        btnSave.setOnClickListener(v -> {
            saveImageToGallery();
        });
    }

    private void saveImageToGallery() {
        try {
            BitmapDrawable drawable = (BitmapDrawable) imageResult.getDrawable();
            if (drawable == null) {
                Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
                return;
            }

            Bitmap bitmap = drawable.getBitmap();
            if (bitmap == null) {
                Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
                return;
            }

            // 保存到相册
            String fileName = "tryon_result_" + System.currentTimeMillis() + ".jpg";
            File file = new File(getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES), fileName);
            
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            // 通知媒体库更新
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(android.net.Uri.fromFile(file));
            sendBroadcast(mediaScanIntent);

            Toast.makeText(this, "图片已保存到相册", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            android.util.Log.e("TryOnResultActivity", "Error saving image", e);
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}








