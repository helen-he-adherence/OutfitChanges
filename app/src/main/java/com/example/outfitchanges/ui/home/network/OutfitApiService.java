package com.example.outfitchanges.ui.home.network;

import com.example.outfitchanges.ui.home.model.CreateOutfitResponse;
import com.example.outfitchanges.ui.home.model.DeleteOutfitResponse;
import com.example.outfitchanges.ui.home.model.FavoriteActionResponse;
import com.example.outfitchanges.ui.home.model.OutfitDetailResponse;
import com.example.outfitchanges.ui.home.model.OutfitListResponse;
import com.example.outfitchanges.ui.home.model.UpdateOutfitRequest;
import com.example.outfitchanges.ui.home.model.UpdateOutfitResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface OutfitApiService {
    // 获取穿搭列表（需要登录）
    @GET("api/outfits")
    Call<OutfitListResponse> getOutfits(
            @Query("limit") Integer limit,
            @Query("offset") Integer offset
    );

    // 创建穿搭（需要登录，multipart/form-data）
    @Multipart
    @POST("api/outfits")
    Call<CreateOutfitResponse> createOutfit(
            @Part MultipartBody.Part image,
            @Part("modified_tags") RequestBody modifiedTags
    );

    // 获取单个穿搭
    @GET("api/outfits/{outfit_id}")
    Call<OutfitDetailResponse> getOutfitDetail(@Path("outfit_id") int outfitId);

    // 更新穿搭（需要登录）
    @PUT("api/outfits/{outfit_id}")
    Call<UpdateOutfitResponse> updateOutfit(
            @Path("outfit_id") int outfitId,
            @Body UpdateOutfitRequest request
    );

    // 删除穿搭（需要登录）
    @DELETE("api/outfits/{outfit_id}")
    Call<DeleteOutfitResponse> deleteOutfit(@Path("outfit_id") int outfitId);

    // 收藏穿搭（需要登录）
    @POST("api/outfits/{outfit_id}/favorite")
    Call<FavoriteActionResponse> favoriteOutfit(@Path("outfit_id") int outfitId);

    // 取消收藏穿搭（需要登录）
    @DELETE("api/outfits/{outfit_id}/favorite")
    Call<FavoriteActionResponse> unfavoriteOutfit(@Path("outfit_id") int outfitId);

    // 穿搭广场（可筛选，登录可选）
    @GET("api/discover/outfits")
    Call<OutfitListResponse> discoverOutfits(
            @Query("season") String season,
            @Query("weather") String weather,
            @Query("occasion") String occasion,
            @Query("style") String style,
            @Query("category") String category,
            @Query("color") String color,
            @Query("sex") String sex,
            @Query("limit") Integer limit,
            @Query("offset") Integer offset,
            @Header("X-Skip-Auth") String skipAuth
    );

    // 个性化推荐（需要登录）
    @GET("api/discover/personalized")
    Call<OutfitListResponse> getPersonalizedOutfits(
            @Query("limit") Integer limit,
            @Query("offset") Integer offset
    );
}

