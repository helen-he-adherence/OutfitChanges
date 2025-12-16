package com.example.outfitchanges.ui.publish.network;

import com.example.outfitchanges.ui.publish.model.UploadResponse;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface PublishApiService {
    @Multipart
    @POST("api/upload")
    Call<UploadResponse> uploadImage(
            @Part MultipartBody.Part image
    );

    @Multipart
    @POST("api/upload")
    Call<UploadResponse> uploadImageWithTags(
            @Part MultipartBody.Part image,
            @Part("modified_tags") RequestBody modifiedTags
    );

    @Multipart
    @POST("api/upload")
    Call<UploadResponse> uploadImageWithUserId(
            @Part MultipartBody.Part image,
            @Part("user_id") RequestBody userId
    );
}

