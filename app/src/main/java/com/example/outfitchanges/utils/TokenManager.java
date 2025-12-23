package com.example.outfitchanges.utils;

import android.content.Context;
import com.example.outfitchanges.auth.network.AuthNetworkClient;
import com.example.outfitchanges.ui.home.network.OutfitNetworkClient;
import com.example.outfitchanges.ui.publish.network.PublishNetworkClient;
import com.example.outfitchanges.ui.virtual.network.VirtualTryOnNetworkClient;

/**
 * 全局 Token 管理器
 * 统一管理所有 NetworkClient 的 token，确保登录后所有网络请求都能使用正确的 token
 */
public class TokenManager {
    private static TokenManager instance;
    private SharedPrefManager prefManager;

    private TokenManager(Context context) {
        this.prefManager = new SharedPrefManager(context);
    }

    /**
     * 获取单例实例
     */
    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * 设置 token 到所有 NetworkClient
     * 同时保存到 SharedPreferences
     */
    public void setToken(String token) {
        if (token == null || token.isEmpty()) {
            clearToken();
            return;
        }

        // 保存到 SharedPreferences
        prefManager.setToken(token);

        // 同步到所有 NetworkClient
        AuthNetworkClient.getInstance().setToken(token);
        OutfitNetworkClient.setToken(token);
        PublishNetworkClient.setToken(token);
        VirtualTryOnNetworkClient.setToken(token);

        android.util.Log.d("TokenManager", "Token set to all NetworkClients");
    }

    /**
     * 清除所有 NetworkClient 的 token
     * 同时清除 SharedPreferences 中的 token
     */
    public void clearToken() {
        // 清除 SharedPreferences
        prefManager.setToken("");

        // 清除所有 NetworkClient 的 token
        AuthNetworkClient.getInstance().clearToken();
        OutfitNetworkClient.clearToken();
        PublishNetworkClient.clearToken();
        VirtualTryOnNetworkClient.clearToken();

        android.util.Log.d("TokenManager", "Token cleared from all NetworkClients");
    }

    /**
     * 从 SharedPreferences 恢复 token 到所有 NetworkClient
     * 应用启动时调用
     */
    public void restoreToken() {
        String token = prefManager.getToken();
        if (token != null && !token.isEmpty()) {
            setToken(token);
            android.util.Log.d("TokenManager", "Token restored from SharedPreferences");
        } else {
            android.util.Log.d("TokenManager", "No token found in SharedPreferences");
        }
    }

    /**
     * 获取当前 token
     */
    public String getToken() {
        return prefManager.getToken();
    }

    /**
     * 检查是否有有效的 token
     */
    public boolean hasToken() {
        String token = getToken();
        return token != null && !token.isEmpty();
    }
}








