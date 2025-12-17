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

    private AuthNetworkClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
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
}

