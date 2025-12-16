package com.example.outfitchanges.ui.publish.model;

import com.google.gson.annotations.SerializedName;

public class UploadResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("outfit_id")
    private int outfitId;

    @SerializedName("tags")
    private OutfitAnalysisTags tags;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message == null ? "" : message;
    }

    public String getImageUrl() {
        return imageUrl == null ? "" : imageUrl;
    }

    public int getOutfitId() {
        return outfitId;
    }

    public OutfitAnalysisTags getTags() {
        return tags;
    }
}

