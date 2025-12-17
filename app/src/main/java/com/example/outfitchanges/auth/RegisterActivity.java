package com.example.outfitchanges.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.outfitchanges.MainActivity;
import com.example.outfitchanges.R;
import com.example.outfitchanges.auth.model.RegisterResponse;
import com.example.outfitchanges.utils.SharedPrefManager;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText verificationCodeEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        usernameEditText = findViewById(R.id.username_edit_text);
        emailEditText = findViewById(R.id.email_edit_text);
        verificationCodeEditText = findViewById(R.id.verification_code_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        Button registerButton = findViewById(R.id.register_button);
        Button sendCodeButton = findViewById(R.id.send_code_button);

        // 获取验证码（模拟）
        sendCodeButton.setOnClickListener(v ->
                Toast.makeText(this, "验证码已发送（模拟）", Toast.LENGTH_SHORT).show()
        );

        registerButton.setOnClickListener(v -> attemptRegister());

        // 观察注册响应
        authViewModel.getRegisterResponse().observe(this, registerResponse -> {
            if (registerResponse != null && registerResponse.isSuccess()) {
                // 注册成功，保存用户信息和token
                SharedPrefManager prefManager = new SharedPrefManager(this);
                prefManager.setLoggedIn(true);
                
                // 使用 TokenManager 统一设置 token 到所有 NetworkClient
                if (registerResponse.getToken() != null) {
                    com.example.outfitchanges.utils.TokenManager.getInstance(RegisterActivity.this)
                            .setToken(registerResponse.getToken());
                }
                
                // 保存用户信息
                if (registerResponse.getUser() != null) {
                    prefManager.setUserId(String.valueOf(registerResponse.getUser().getId()));
                    prefManager.setUsername(registerResponse.getUser().getUsername());
                    prefManager.setEmail(registerResponse.getUser().getEmail());
                }
                
                Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // 观察登录状态（备用）
        authViewModel.getIsLoggedIn().observe(this, isLoggedIn -> {
            // 这个观察者主要用于兼容性，实际注册成功通过registerResponse处理
        });

        // 观察错误信息
        authViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void attemptRegister() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String code = verificationCodeEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (username.isEmpty()) {
            usernameEditText.setError("请输入用户名");
            return;
        }

        if (email.isEmpty()) {
            emailEditText.setError("请输入邮箱");
            return;
        }

        // 验证码暂时可选，因为API可能不需要
        // if (code.isEmpty()) {
        //     verificationCodeEditText.setError("请输入验证码");
        //     return;
        // }

        if (password.isEmpty()) {
            passwordEditText.setError("请输入密码");
            return;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.setError("请再次输入密码");
            return;
        }

        // 调用ViewModel的注册方法
        authViewModel.register(username, email, password, confirmPassword);
    }
}