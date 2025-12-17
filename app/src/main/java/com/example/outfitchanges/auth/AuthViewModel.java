package com.example.outfitchanges.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.outfitchanges.auth.model.LoginRequest;
import com.example.outfitchanges.auth.model.LoginResponse;
import com.example.outfitchanges.auth.network.AuthApiService;
import com.example.outfitchanges.auth.network.AuthNetworkClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends ViewModel {

    // 用于存储登录状态
    private MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);
    private MutableLiveData<String> currentUser = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<LoginResponse> loginResponse = new MutableLiveData<>();
    private final AuthApiService apiService;

    public AuthViewModel() {
        apiService = AuthNetworkClient.getInstance().getApiService();
    }

    // 登录方法
    public void login(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("请输入邮箱");
            return;
        }

        if (password == null || password.isEmpty()) {
            errorMessage.setValue("请输入密码");
            return;
        }

        LoginRequest request = new LoginRequest(email.trim(), password);
        Call<LoginResponse> call = apiService.login(request);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.isSuccess()) {
                        // 登录成功
                        AuthViewModel.this.loginResponse.setValue(loginResponse);
                        currentUser.setValue(email);
                        isLoggedIn.setValue(true);
                        errorMessage.setValue(null);
                    } else {
                        errorMessage.setValue(loginResponse.getMessage() != null ? loginResponse.getMessage() : "登录失败");
                    }
                } else {
                    String errorMsg = "登录失败";
                    try {
                        if (response.code() == 401) {
                            errorMsg = "邮箱或密码错误";
                        } else if (response.code() == 400) {
                            errorMsg = "邮箱和密码不能为空";
                        } else if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("AuthViewModel", "Error response: " + errorBody);
                            errorMsg = "登录失败: HTTP " + response.code();
                        } else {
                            errorMsg = "登录失败: HTTP " + response.code();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("AuthViewModel", "Error reading error body", e);
                        errorMsg = "登录失败: HTTP " + response.code();
                    }
                    errorMessage.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                errorMessage.setValue("网络错误: " + t.getMessage());
                android.util.Log.e("AuthViewModel", "Login failed", t);
            }
        });
    }

    // 注册方法
    public void register(String username, String email, String password, String confirmPassword) {
        // TODO: 这里应该调用实际的注册API
        // 暂时模拟注册逻辑
        if (!password.equals(confirmPassword)) {
            errorMessage.setValue("两次输入的密码不一致");
            return;
        }

        if (!isValidEmail(email)) {
            errorMessage.setValue("邮箱格式不正确");
            return;
        }

        if (!isValidPassword(password)) {
            errorMessage.setValue("密码必须至少6位");
            return;
        }

        // 模拟注册成功
        currentUser.setValue(email);
        isLoggedIn.setValue(true);
        errorMessage.setValue(null);
    }

    // 登出方法
    public void logout() {
        currentUser.setValue(null);
        isLoggedIn.setValue(false);
        errorMessage.setValue(null);
    }

    // 验证邮箱格式
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    // 验证密码长度
    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    // Getter方法供Activity/Fragment观察
    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }

    public LiveData<String> getCurrentUser() {
        return currentUser;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<LoginResponse> getLoginResponse() {
        return loginResponse;
    }

    // 清理资源（ViewModel被销毁时调用）
    @Override
    protected void onCleared() {
        super.onCleared();
        // 可以在这里清理资源
    }
}