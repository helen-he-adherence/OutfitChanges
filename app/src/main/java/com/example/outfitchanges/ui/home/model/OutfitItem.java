package com.example.outfitchanges.ui.home.model;

import com.google.gson.annotations.SerializedName;

public class OutfitItem {
    private static final String BASE_IMAGE_URL = "https://luckyhe.fun/outfits/images/";

    @SerializedName("image")
    private String image;

    @SerializedName("tags")
    private OutfitTags tags;

    public String getImage() {
        return image == null ? "" : image;
    }

    public OutfitTags getTags() {
        return tags == null ? new OutfitTags() : tags;
    }

    public String getImageUrl() {
        return BASE_IMAGE_URL + getImage();
    }
}

