package com.example.outfitchanges.ui.virtual.network;

import com.example.outfitchanges.ui.virtual.model.VirtualTryOnResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface VirtualTryOnApiService {
    @Multipart
    @POST("api/virtual-tryon")
    Call<VirtualTryOnResponse> submitTryOnTask(
            @Header("Authorization") String authorization,
            @Part MultipartBody.Part targetImage,
            @Part("outfit_id") RequestBody outfitId
    );

    @Multipart
    @POST("api/virtual-tryon")
    Call<VirtualTryOnResponse> submitTryOnTaskWithSourceImage(
            @Header("Authorization") String authorization,
            @Part MultipartBody.Part targetImage,
            @Part MultipartBody.Part sourceImage
    );

    @GET("api/virtual-tryon/{taskId}")
    Call<VirtualTryOnResponse> getTaskStatus(
            @Header("Authorization") String authorization,
            @Path("taskId") String taskId
    );
}

