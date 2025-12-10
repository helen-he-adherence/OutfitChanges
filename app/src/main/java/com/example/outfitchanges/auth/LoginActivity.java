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
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

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

        // 观察登录状态
        authViewModel.getIsLoggedIn().observe(this, isLoggedIn -> {
            if (isLoggedIn != null && isLoggedIn) {
                // 登录成功，跳转到主页面
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
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