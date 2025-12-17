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

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        
        // 如果请求已经包含 Authorization header，则不添加
        if (original.header("Authorization") != null) {
            return chain.proceed(original);
        }

        // 如果有 token，添加到请求头
        if (token != null && !token.isEmpty()) {
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer " + token);
            return chain.proceed(requestBuilder.build());
        }

        return chain.proceed(original);
    }
}

