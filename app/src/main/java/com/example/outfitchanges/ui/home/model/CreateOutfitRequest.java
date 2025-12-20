package com.example.outfitchanges.ui.home.model;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class CreateOutfitRequest {
    private MultipartBody.Part imagePart;
    private RequestBody modifiedTagsBody;

    public CreateOutfitRequest(MultipartBody.Part imagePart, RequestBody modifiedTagsBody) {
        this.imagePart = imagePart;
        this.modifiedTagsBody = modifiedTagsBody;
    }

    public MultipartBody.Part getImagePart() {
        return imagePart;
    }

    public RequestBody getModifiedTagsBody() {
        return modifiedTagsBody;
    }
}





