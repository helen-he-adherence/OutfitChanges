package com.example.outfitchanges.auth.network;

import com.example.outfitchanges.auth.model.FavoritesResponse;
import com.example.outfitchanges.auth.model.LoginRequest;
import com.example.outfitchanges.auth.model.LoginResponse;
import com.example.outfitchanges.auth.model.PreferencesRequest;
import com.example.outfitchanges.auth.model.ProfileResponse;
import com.example.outfitchanges.auth.model.RegisterRequest;
import com.example.outfitchanges.auth.model.RegisterResponse;
import com.example.outfitchanges.auth.model.UpdatePreferencesResponse;
import com.example.outfitchanges.auth.model.UserOutfitsResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AuthApiService {
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @GET("api/profile")
    Call<ProfileResponse> getProfile();

    @PUT("api/profile/preferences")
    Call<UpdatePreferencesResponse> updatePreferences(@Body PreferencesRequest request);

    @GET("api/user/favorites")
    Call<FavoritesResponse> getFavorites();

    @GET("api/user/outfits")
    Call<UserOutfitsResponse> getUserOutfits();
}

