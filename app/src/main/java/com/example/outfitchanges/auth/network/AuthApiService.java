//API接口定义：声明所有后端接口（路径、方法、参数、返回类型）
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
//这些事数据模型类，用于：请求体（Request Body）：如LoginRequest 包含用户名和密码
//                    响应体（Response Body）：如LoginResponse 包含token、用户信息
//它们通常用 Gson 或 Moshi 自动将 JSON 转为 Java 对象（反之亦然）。

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface AuthApiService {
//这是一个 接口（不是类），由 Retrofit 在运行时自动生成实现。
//所有方法都返回 Call<响应类型>，用于发起网络请求。
    @POST("api/auth/login")
//@GET, @POST, @PUT：HTTP 方法注解。
    Call<LoginResponse> login(@Body LoginRequest request);
//  Call<T>：Retrofit 的异步/同步调用封装，代表一次 HTTP 请求。
//  @Body：表示该参数作为 JSON 请求体 发送。

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

