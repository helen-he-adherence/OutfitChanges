package com.example.outfitchanges.auth.network;

import com.example.outfitchanges.auth.model.LoginRequest;
import com.example.outfitchanges.auth.model.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
}

