package com.example.outfitchanges.ui.virtual.network;

import com.example.outfitchanges.auth.network.TokenInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class VirtualTryOnNetworkClient {
    private static final String BASE_URL = "https://luckyhe.fun/";
    private static VirtualTryOnNetworkClient instance;
    private final VirtualTryOnApiService apiService;
    private static final TokenInterceptor tokenInterceptor = new TokenInterceptor();

    private VirtualTryOnNetworkClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(tokenInterceptor) // 先添加 token 拦截器
                .addInterceptor(logging) // 再添加日志拦截器
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(VirtualTryOnApiService.class);
    }

    public static synchronized VirtualTryOnNetworkClient getInstance() {
        if (instance == null) {
            instance = new VirtualTryOnNetworkClient();
        }
        return instance;
    }

    public VirtualTryOnApiService getApiService() {
        return apiService;
    }

    /**
     * 设置 token，后续请求会自动添加 Authorization header
     */
    public static void setToken(String token) {
        tokenInterceptor.setToken(token);
    }

    /**
     * 清除 token
     */
    public static void clearToken() {
        tokenInterceptor.clearToken();
    }
}

