// com.example.outfitchanges.auth.ForgetPasswordActivity
package com.example.outfitchanges.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.outfitchanges.R;

public class ForgetPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText verifyCodeEditText;
    private Button sendCodeButton;
    private Button confirmButton;
    private TextView returnLoginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        emailEditText = findViewById(R.id.email_edit_text);
        verifyCodeEditText = findViewById(R.id.verify_code_edit_text);
        sendCodeButton = findViewById(R.id.send_code_button);
        confirmButton = findViewById(R.id.confirm_button);
        returnLoginText = findViewById(R.id.return_login_text);

        // 发送验证码按钮
        sendCodeButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            if (email.isEmpty()) {
                emailEditText.setError("请输入邮箱");
                return;
            }
            // 模拟发送验证码逻辑
            Toast.makeText(this, "验证码已发送至 " + email, Toast.LENGTH_SHORT).show();
        });

        // 确认按钮：跳转到重置密码页
        confirmButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String code = verifyCodeEditText.getText().toString().trim();

            if (email.isEmpty()) {
                emailEditText.setError("请输入邮箱");
                return;
            }

            if (code.isEmpty()) {
                verifyCodeEditText.setError("请输入验证码");
                return;
            }

            // 跳转到重置密码页面
            Intent intent = new Intent(ForgetPasswordActivity.this, ResetPasswordActivity.class);
            intent.putExtra("email", email); // 传邮箱给下一页面
            startActivity(intent);
            finish();
        });

        // 返回登录页
        returnLoginText.setOnClickListener(v -> {
            Intent intent = new Intent(ForgetPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}