package com.example.outfitchanges;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.outfitchanges.auth.LoginActivity;
import com.example.outfitchanges.auth.network.AuthNetworkClient;
import com.example.outfitchanges.utils.SharedPrefManager;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // 从 SharedPreferences 恢复 token 到所有 NetworkClient
        com.example.outfitchanges.utils.TokenManager.getInstance(this).restoreToken();
        
        // 检查是否已登录
        SharedPrefManager prefManager = new SharedPrefManager(this);
        if (prefManager.isLoggedIn() && com.example.outfitchanges.utils.TokenManager.getInstance(this).hasToken()) {
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