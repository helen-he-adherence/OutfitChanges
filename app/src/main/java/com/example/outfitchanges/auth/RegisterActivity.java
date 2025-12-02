package com.example.outfitchanges.auth;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;
import com.example.outfitchanges.R;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private TextInputEditText usernameEditText;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        emailEditText = findViewById(R.id.email_edit_text);
        passwordEditText = findViewById(R.id.password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        usernameEditText = findViewById(R.id.username_edit_text);
        Button registerButton = findViewById(R.id.register_button);

        registerButton.setOnClickListener(v -> attemptRegister());

        // 观察登录状态
        authViewModel.getIsLoggedIn().observe(this, isLoggedIn -> {
            if (isLoggedIn != null && isLoggedIn) {
                // 注册并登录成功，返回登录页面或直接跳转主页面
                finish(); // 返回登录页面
            }
        });

        // 观察错误信息
        authViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                // 可以在这里显示错误信息
                // 例如：confirmPasswordEditText.setError(errorMessage);
            }
        });
    }

    private void attemptRegister() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("请输入邮箱");
            return;
        }

        if (username.isEmpty()) {
            usernameEditText.setError("请输入用户名");
            return;
        }

        if (password.isEmpty()) {
            passwordEditText.setError("请输入密码");
            return;
        }

        // 调用ViewModel的注册方法
        authViewModel.register(username, email, password, confirmPassword);
    }
}