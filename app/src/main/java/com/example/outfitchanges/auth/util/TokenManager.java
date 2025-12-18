package com.example.outfitchanges.auth.util;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    
    private static TokenManager instance;
    private SharedPreferences sharedPreferences;

    private TokenManager(Context context) {
        sharedPreferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    /**
     * 保存 token
     */
    public void saveToken(String token) {
        sharedPreferences.edit()
                .putString(KEY_TOKEN, token)
                .apply();
    }

    /**
     * 获取 token
     */
    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    /**
     * 保存用户信息
     */
    public void saveUserInfo(int userId, String username, String email) {
        sharedPreferences.edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_EMAIL, email)
                .apply();
    }

    /**
     * 获取用户 ID
     */
    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    /**
     * 获取用户名
     */
    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }

    /**
     * 获取邮箱
     */
    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    /**
     * 检查是否已登录
     */
    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    /**
     * 清除所有登录信息（登出时使用）
     */
    public void clear() {
        sharedPreferences.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_USER_ID)
                .remove(KEY_USERNAME)
                .remove(KEY_EMAIL)
                .apply();
    }
}


