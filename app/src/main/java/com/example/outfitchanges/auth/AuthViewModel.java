package com.example.outfitchanges.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.outfitchanges.auth.model.LoginRequest;
import com.example.outfitchanges.auth.model.LoginResponse;
import com.example.outfitchanges.auth.model.RegisterRequest;
import com.example.outfitchanges.auth.model.RegisterResponse;
import com.example.outfitchanges.auth.network.AuthApiService;
import com.example.outfitchanges.auth.network.AuthNetworkClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends ViewModel {

    // 用于存储登录状态  LiveData 用于通知UI
    private MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);
    private MutableLiveData<String> currentUser = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<LoginResponse> loginResponse = new MutableLiveData<>();
    private MutableLiveData<RegisterResponse> registerResponse = new MutableLiveData<>();

    //网络客户端（单例）
    private final AuthApiService apiService;
    private final AuthNetworkClient networkClient;

    public AuthViewModel() {
        networkClient = AuthNetworkClient.getInstance();  //单例
        //apiService 是 Retrofit 动态生成的，底层使用 OkHttpClient（带 TokenInterceptor 和日志）。
        apiService = networkClient.getApiService();       //获取Retrofit接口
    }

    // 登录方法
    public void login(String email, String password) {
        //1.输入校验（邮箱格式、密码长度、一致性）
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("请输入邮箱");
            return;
        }

        if (password == null || password.isEmpty()) {
            errorMessage.setValue("请输入密码");
            return;
        }

        // 确保密码没有前后空格（但保留中间空格，虽然密码通常不应该有空格）
        String trimmedPassword = password.trim();
        LoginRequest request = new LoginRequest(email.trim(), trimmedPassword);
        
        // 添加调试日志
        android.util.Log.d("AuthViewModel", "尝试登录 - 邮箱: " + email.trim() + ", 密码长度: " + trimmedPassword.length());
        
        Call<LoginResponse> call = apiService.login(request);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                android.util.Log.d("AuthViewModel", "登录响应 - HTTP Code: " + response.code() + ", Success: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    android.util.Log.d("AuthViewModel", "登录响应体 - Success: " + loginResponse.isSuccess() + ", Message: " + loginResponse.getMessage());
                    if (loginResponse.isSuccess()) {
                        // 登录成功，token 会由 LoginActivity 通过 TokenManager 统一管理
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
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("AuthViewModel", "登录失败 - HTTP Code: " + response.code() + ", Error response: " + errorBody);
                            
                            // 检查是否是 HTML 错误页面（通常是服务器错误）
                            if (errorBody.contains("<!doctype html>") || errorBody.contains("<html")) {
                                if (response.code() == 500) {
                                    errorMsg = "服务器内部错误，请稍后重试";
                                } else {
                                    errorMsg = "服务器错误: HTTP " + response.code();
                                }
                            } else {
                                // 尝试解析 JSON 错误消息
                                try {
                                    com.google.gson.JsonObject jsonObject = new com.google.gson.JsonParser().parse(errorBody).getAsJsonObject();
                                    if (jsonObject.has("error")) {
                                        errorMsg = jsonObject.get("error").getAsString();
                                    } else if (jsonObject.has("message")) {
                                        errorMsg = jsonObject.get("message").getAsString();
                                    } else {
                                        // 根据状态码设置默认错误消息
                                        if (response.code() == 401) {
                                            errorMsg = "邮箱或密码错误";
                                        } else if (response.code() == 400) {
                                            errorMsg = "邮箱和密码不能为空";
                                        } else {
                                            errorMsg = "登录失败: HTTP " + response.code();
                                        }
                                    }
                                } catch (Exception e) {
                                    // 如果解析失败，使用状态码判断
                                    if (response.code() == 401) {
                                        errorMsg = "邮箱或密码错误";
                                    } else if (response.code() == 400) {
                                        errorMsg = "邮箱和密码不能为空";
                                    } else if (response.code() == 500) {
                                        errorMsg = "服务器内部错误，请稍后重试";
                                    } else {
                                        errorMsg = "登录失败: HTTP " + response.code();
                                    }
                                }
                            }
                        } else {
                            if (response.code() == 401) {
                                errorMsg = "邮箱或密码错误";
                            } else if (response.code() == 400) {
                                errorMsg = "邮箱和密码不能为空";
                            } else if (response.code() == 500) {
                                errorMsg = "服务器内部错误，请稍后重试";
                            } else {
                                errorMsg = "登录失败: HTTP " + response.code();
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("AuthViewModel", "Error reading error body", e);
                        if (response.code() == 500) {
                            errorMsg = "服务器内部错误，请稍后重试";
                        } else {
                            errorMsg = "登录失败: HTTP " + response.code();
                        }
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
        // 1.输入校验（邮箱格式、密码长度、一致性等）
        if (username == null || username.trim().isEmpty()) {
            errorMessage.setValue("请输入用户名");
            return;
        }

        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("请输入邮箱");
            return;
        }

        if (password == null || password.isEmpty()) {
            errorMessage.setValue("请输入密码");
            return;
        }

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

        // 调用注册 API
        // 确保密码没有前后空格
        String trimmedPassword = password.trim();
        //4️⃣ 2.构造RegisterRequest
        RegisterRequest request = new RegisterRequest(username.trim(), email.trim(), trimmedPassword);
        
        // 添加调试日志
        android.util.Log.d("AuthViewModel", "尝试注册 - 用户名: " + username.trim() + ", 邮箱: " + email.trim() + ", 密码长度: " + trimmedPassword.length());
        //3.调用Retrofit接口
        Call<RegisterResponse> call = apiService.register(request);
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                android.util.Log.d("AuthViewModel", "注册响应 - HTTP Code: " + response.code() + ", Success: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();
                    android.util.Log.d("AuthViewModel", "注册响应体 - Success: " + registerResponse.isSuccess() + ", Message: " + registerResponse.getMessage());
                    if (registerResponse.isSuccess()) {
                        // 注册成功，token 会由 RegisterActivity 通过 TokenManager 统一管理
                        //成功更新LiveData
                        AuthViewModel.this.registerResponse.setValue(registerResponse);
                        currentUser.setValue(email);
                        isLoggedIn.setValue(true);
                        errorMessage.setValue(null);
                    } else {
                        //处理HTTP错误（400.500等），解析errorBody
                        errorMessage.setValue(registerResponse.getMessage() != null ? registerResponse.getMessage() : "注册失败");
                    }
                } else {
                    String errorMsg = "注册失败";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("AuthViewModel", "注册失败 - HTTP Code: " + response.code() + ", Error response: " + errorBody);
                            
                            // 检查是否是 HTML 错误页面（通常是服务器错误）
                            if (errorBody.contains("<!doctype html>") || errorBody.contains("<html")) {
                                if (response.code() == 500) {
                                    errorMsg = "服务器内部错误，请稍后重试";
                                } else {
                                    errorMsg = "服务器错误: HTTP " + response.code();
                                }
                            } else {
                                // 尝试解析 JSON 错误消息
                                try {
                                    com.google.gson.JsonObject jsonObject = new com.google.gson.JsonParser().parse(errorBody).getAsJsonObject();
                                    if (jsonObject.has("error")) {
                                        errorMsg = jsonObject.get("error").getAsString();
                                    } else if (jsonObject.has("message")) {
                                        errorMsg = jsonObject.get("message").getAsString();
                                    } else {
                                        // 根据状态码设置默认错误消息
                                        errorMsg = getErrorMessageByCode(response.code());
                                    }
                                } catch (Exception e) {
                                    // 如果解析失败，使用状态码判断
                                    errorMsg = getErrorMessageByCode(response.code());
                                }
                            }
                        } else {
                            errorMsg = getErrorMessageByCode(response.code());
                        }
                    } catch (Exception e) {
                        android.util.Log.e("AuthViewModel", "Error reading error body", e);
                        errorMsg = getErrorMessageByCode(response.code());
                    }
                    errorMessage.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                errorMessage.setValue("网络错误: " + t.getMessage());
                android.util.Log.e("AuthViewModel", "Register failed", t);
            }
        });
    }

    // 登出方法
    public void logout() {
        // Token 清除会由 ProfileFragment 通过 TokenManager 统一管理
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
    
    // 根据 HTTP 状态码返回友好的错误消息
    private String getErrorMessageByCode(int code) {
        switch (code) {
            case 400:
                return "注册信息不完整或格式错误";
            case 401:
                return "未授权，请检查您的凭据";
            case 403:
                return "禁止访问";
            case 409:
                return "用户名或邮箱已存在";
            case 500:
                return "服务器内部错误，请稍后重试";
            case 502:
                return "服务器网关错误";
            case 503:
                return "服务暂时不可用，请稍后重试";
            default:
                return "注册失败: HTTP " + code;
        }
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

    public LiveData<RegisterResponse> getRegisterResponse() {
        return registerResponse;
    }

    // 清理资源（ViewModel被销毁时调用）
    @Override
    protected void onCleared() {
        super.onCleared();
        // 可以在这里清理资源
    }
}