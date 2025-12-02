// com.example.outfitchanges.auth.ResetPasswordActivity
package com.example.outfitchanges.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.example.outfitchanges.R;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private Button resetButton;
    private TextView returnText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        newPasswordEditText = findViewById(R.id.new_password_edit_text);
        confirmPasswordEditText = findViewById(R.id.confirm_password_edit_text);
        resetButton = findViewById(R.id.reset_button);
        returnText = findViewById(R.id.return_text);

        // 重置密码按钮
        resetButton.setOnClickListener(v -> {
            String newPassword = newPasswordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (newPassword.isEmpty()) {
                newPasswordEditText.setError("请输入新密码");
                return;
            }

            if (confirmPassword.isEmpty()) {
                confirmPasswordEditText.setError("请确认密码");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                confirmPasswordEditText.setError("两次输入的密码不一致");
                return;
            }

            // 模拟成功重置
            Toast.makeText(this, "密码重置成功！", Toast.LENGTH_SHORT).show();

            // 返回登录页面
            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // 返回按钮
        returnText.setOnClickListener(v -> {
            Intent intent = new Intent(ResetPasswordActivity.this, ForgetPasswordActivity.class);
            startActivity(intent);
            finish();
        });
    }
}