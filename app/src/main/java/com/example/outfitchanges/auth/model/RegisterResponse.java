package com.example.outfitchanges.auth.model;

public class RegisterResponse {
    private boolean success; //是否注册成功（布尔值）
    private String message; //服务器返回的人类可读消息（如“用户名已存在”）；
    private String token; //注册成功后返回的 JWT 认证令牌；
    private UserInfo user; //包含新注册用户的详细信息（嵌套类）。

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public static class UserInfo {
        //静态内部类 UserInfo：避免创建额外文件，同时清晰表达“用户信息”是响应的一部分。
        private int id;
        private String username;
        private String email;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}








