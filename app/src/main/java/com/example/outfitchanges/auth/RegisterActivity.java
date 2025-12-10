package com.example.outfitchanges.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.outfitchanges.R;

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

        // 观察登录状态
        authViewModel.getIsLoggedIn().observe(this, isLoggedIn -> {
            if (isLoggedIn != null && isLoggedIn) {
                // 注册成功后跳转回登录页面
                Toast.makeText(this, "注册成功，请登录", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
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

        if (code.isEmpty()) {
            verificationCodeEditText.setError("请输入验证码");
            return;
        }

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