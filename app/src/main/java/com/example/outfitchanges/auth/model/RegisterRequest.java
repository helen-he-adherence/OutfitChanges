package com.example.outfitchanges.auth.model;
//包声明：属于 auth.model，表示这是认证相关的数据传输对象（DTO）。
//封装注册时前端需要提交的数据，符合后端 API 的期望格式。

public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    //字段：username, email, password —— 这是向后端注册接口发送的数据结构。
    //构造函数：方便在 ViewModel 中快速创建请求体。
    public RegisterRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}








