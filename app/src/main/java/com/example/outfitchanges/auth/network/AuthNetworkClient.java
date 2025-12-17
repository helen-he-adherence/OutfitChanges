package com.example.outfitchanges.auth.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class AuthNetworkClient {
    private static final String BASE_URL = "https://luckyhe.fun/";
    private static AuthNetworkClient instance;
    private final AuthApiService apiService;
    private final TokenInterceptor tokenInterceptor;
    private Retrofit retrofit;

    private AuthNetworkClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // 创建 Token 拦截器
        tokenInterceptor = new TokenInterceptor();

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(tokenInterceptor) // 先添加 token 拦截器
                .addInterceptor(logging) // 再添加日志拦截器
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(AuthApiService.class);
    }

    public static synchronized AuthNetworkClient getInstance() {
        if (instance == null) {
            instance = new AuthNetworkClient();
        }
        return instance;
    }

    public AuthApiService getApiService() {
        return apiService;
    }

    /**
     * 设置 token，后续请求会自动添加 Authorization header
     */
    public void setToken(String token) {
        tokenInterceptor.setToken(token);
    }

    /**
     * 清除 token
     */
    public void clearToken() {
        tokenInterceptor.clearToken();
    }
}

