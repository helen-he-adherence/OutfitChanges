package com.example.outfitchanges.ui.publish.network;

import com.example.outfitchanges.ui.home.model.CreateOutfitResponse;
import com.example.outfitchanges.ui.home.model.UpdateOutfitRequest;
import com.example.outfitchanges.ui.home.model.UpdateOutfitResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface PublishApiService {
    // 创建穿搭（新的API，需要登录）
    @Multipart
    @POST("api/outfits")
    Call<CreateOutfitResponse> createOutfit(
            @Part MultipartBody.Part image,
            @Part("modified_tags") RequestBody modifiedTags
    );

    // 更新穿搭标签（需要登录）
    @PUT("api/outfits/{outfit_id}")
    Call<UpdateOutfitResponse> updateOutfit(
            @Path("outfit_id") int outfitId,
            @Body UpdateOutfitRequest request
    );
}

