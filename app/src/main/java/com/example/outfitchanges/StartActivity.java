package com.example.outfitchanges;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.outfitchanges.auth.LoginActivity;
import com.example.outfitchanges.utils.SharedPrefManager;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // 检查是否已登录
        SharedPrefManager prefManager = new SharedPrefManager(this);
        if (prefManager.isLoggedIn() && !prefManager.getToken().isEmpty()) {
            // 已登录，直接跳转到主界面
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        Button startButton = findViewById(R.id.splashStartButton);
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}