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


//MVVM架构：Activity只负责UI和用户交互，不直接处理网络或业务逻辑
//ViewModel解耦 ：通过ViewModelProvider获取AuthViewModel,生命周期安全
//LiveData观察：
        //registerResponse：成功后保存token、用户信息、跳转主页
        //errorMessage：失败时弹出提示

public class RegisterActivity extends AppCompatActivity {

    //UI控件引用
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

        // 初始化ViewModel（MVVM 架构核心）
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        //绑定UI控件
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

        //1️⃣ 注册按钮点击 -> 尝试注册
        registerButton.setOnClickListener(v -> attemptRegister());

        // 关键：观察注册结果（LiveData）
        authViewModel.getRegisterResponse().observe(this, registerResponse -> {
            if (registerResponse != null && registerResponse.isSuccess()) {
                // 1.保存登录状态 注册成功，保存用户信息和token
                SharedPrefManager prefManager = new SharedPrefManager(this);
                prefManager.setLoggedIn(true);
                
                // 2.使用 TokenManager 统一设置 token 到所有 NetworkClient
                if (registerResponse.getToken() != null) {
                    com.example.outfitchanges.utils.TokenManager.getInstance(RegisterActivity.this)
                            .setToken(registerResponse.getToken());
                }
                
                // 3.保存用户基本信息到 SharedPreferences
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

        // 观察错误信息（用于 Toast 提示）
        authViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //2️⃣
    private void attemptRegister() {
        //1.获取输入
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String code = verificationCodeEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        //2.基础校验
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

        //3️⃣ 3. 调用 ViewModel 的注册方法（不直接操作网络！）
        authViewModel.register(username, email, password, confirmPassword);
    }
}