package com.example.outfitchanges.auth;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import android.widget.EditText;
import com.example.outfitchanges.MainActivity;
import com.example.outfitchanges.R;
import com.example.outfitchanges.utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private AuthViewModel authViewModel;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        // 如果已经登录，直接跳转到主界面
        prefManager = new SharedPrefManager(this);
        com.example.outfitchanges.utils.TokenManager tokenManager = com.example.outfitchanges.utils.TokenManager.getInstance(this);
        if (prefManager.isLoggedIn() && tokenManager.hasToken()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        Button loginButton = findViewById(R.id.login_button);
        Button guestLoginButton = findViewById(R.id.guest_login_button);
        TextView registerText = findViewById(R.id.register_text);

        // 设置游客登录按钮的背景颜色和文字颜色
        // 使用 post() 确保在视图完全渲染后设置
        guestLoginButton.post(() -> {
            ColorStateList grayColorStateList = ColorStateList.valueOf(Color.parseColor("#E0E0E0"));
            
            // 方法1: 直接设置背景 drawable（确保圆角保留）
            float cornerRadius = 28f * getResources().getDisplayMetrics().density;
            GradientDrawable guestButtonBg = new GradientDrawable();
            guestButtonBg.setShape(GradientDrawable.RECTANGLE);
            guestButtonBg.setColor(Color.parseColor("#E0E0E0"));
            guestButtonBg.setCornerRadius(cornerRadius);
            guestLoginButton.setBackground(guestButtonBg);
            
            // 方法2: 使用 ViewCompat 设置 tint
            ViewCompat.setBackgroundTintList(guestLoginButton, grayColorStateList);
            ViewCompat.setBackgroundTintMode(guestLoginButton, null);
            
            // 方法3: 如果是 MaterialButton，直接设置
            if (guestLoginButton instanceof MaterialButton) {
                MaterialButton mb = (MaterialButton) guestLoginButton;
                mb.setBackgroundTintList(grayColorStateList);
                mb.setBackgroundColor(Color.parseColor("#E0E0E0"));
            }
            
            // 设置文字颜色
            guestLoginButton.setTextColor(Color.parseColor("#000000"));
        });

        loginButton.setOnClickListener(v -> attemptLogin());

        // 游客登录按钮点击事件
        guestLoginButton.setOnClickListener(v -> {
            // 设置游客模式标记
            prefManager.setGuestMode(true);
            prefManager.setLoggedIn(false);
            // 清除token（如果有）
            com.example.outfitchanges.utils.TokenManager.getInstance(this).clearToken();
            // 直接跳转到主界面
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // 找回密码文本点击事件
        findViewById(R.id.forgot_password_text).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
            startActivity(intent);
        });

        // 观察登录响应
        authViewModel.getLoginResponse().observe(this, loginResponse -> {
            if (loginResponse != null && loginResponse.isSuccess()) {
                // 登录成功，保存用户信息和token
                prefManager.setLoggedIn(true);
                // 清除游客模式标记
                prefManager.setGuestMode(false);
                
                // 使用 TokenManager 统一设置 token 到所有 NetworkClient
                if (loginResponse.getToken() != null) {
                    com.example.outfitchanges.utils.TokenManager.getInstance(LoginActivity.this)
                            .setToken(loginResponse.getToken());
                }
                
                // 保存用户信息
                if (loginResponse.getUser() != null) {
                    prefManager.setUserId(String.valueOf(loginResponse.getUser().getId()));
                    prefManager.setUsername(loginResponse.getUser().getUsername());
                    prefManager.setEmail(loginResponse.getUser().getEmail());
                }
                
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // 观察登录状态（备用）
        authViewModel.getIsLoggedIn().observe(this, isLoggedIn -> {
            // 这个观察者主要用于兼容性，实际登录成功通过loginResponse处理
        });

        // 观察错误信息
        authViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("请输入邮箱");
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("请输入密码");
            return;
        }

        // 调用ViewModel的登录方法
        authViewModel.login(email, password);
    }
}