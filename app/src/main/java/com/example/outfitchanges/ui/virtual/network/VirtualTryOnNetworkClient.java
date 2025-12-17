package com.example.outfitchanges.ui.virtual.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class VirtualTryOnNetworkClient {
    private static final String BASE_URL = "https://luckyhe.fun/";
    private static VirtualTryOnNetworkClient instance;
    private final VirtualTryOnApiService apiService;

    private VirtualTryOnNetworkClient() {
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
}

