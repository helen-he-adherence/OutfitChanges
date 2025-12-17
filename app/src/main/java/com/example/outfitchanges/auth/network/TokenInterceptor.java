package com.example.outfitchanges.auth.network;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

/**
 * Token 拦截器，自动为请求添加 Authorization header
 */
public class TokenInterceptor implements Interceptor {
    private String token;

    public TokenInterceptor() {
        this.token = null;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void clearToken() {
        this.token = null;
    }

    public String getToken() {
        return token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        
        // 如果请求已经包含 Authorization header，则不添加
        if (original.header("Authorization") != null) {
            return chain.proceed(original);
        }

        // 如果请求包含 X-Skip-Auth header，则跳过添加 token
        if ("true".equalsIgnoreCase(original.header("X-Skip-Auth"))) {
            return chain.proceed(original);
        }

        String url = original.url().toString();
        
        // 对于公开端点（登录可选），如果服务器在处理带 token 的请求时有问题，
        // 可以选择不添加 token。但为了获取个性化内容，我们仍然尝试添加 token。
        // 如果遇到 500 错误，可以在 Repository 层进行重试（不带 token）
        
        // 如果有 token，添加到请求头
        if (token != null && !token.isEmpty()) {
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer " + token);
            return chain.proceed(requestBuilder.build());
        }

        return chain.proceed(original);
    }
}

