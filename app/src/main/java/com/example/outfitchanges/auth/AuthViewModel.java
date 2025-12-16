package com.example.outfitchanges.auth;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AuthViewModel extends ViewModel {

    // 用于存储登录状态
    private MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);
    private MutableLiveData<String> currentUser = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // 登录方法
    public void login(String email, String password) {
        // 简单的登录验证：账号为admin，密码为admin
        if (email != null && email.trim().equalsIgnoreCase("admin") 
            && password != null && password.equals("admin")) {
            // 登录成功
            currentUser.setValue(email);
            isLoggedIn.setValue(true);
            errorMessage.setValue(null);
        } else {
            errorMessage.setValue("账号或密码错误");
        }
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

    // 清理资源（ViewModel被销毁时调用）
    @Override
    protected void onCleared() {
        super.onCleared();
        // 可以在这里清理资源
    }
}