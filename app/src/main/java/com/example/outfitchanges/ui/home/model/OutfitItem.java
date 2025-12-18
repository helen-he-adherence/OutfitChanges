package com.example.outfitchanges.ui.home.model;

import com.google.gson.annotations.SerializedName;

public class OutfitItem {
    private static final String BASE_IMAGE_URL = "https://luckyhe.fun/outfits/images/";

    @SerializedName("id")
    private Integer id;

    @SerializedName("image")
    private String image;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("tags")
    private OutfitTags tags;

    @SerializedName("likes")
    private Integer likes;

    @SerializedName("is_favorited")
    private Boolean isFavorited;

    public String getImage() {
        return image == null ? "" : image;
    }

    public OutfitTags getTags() {
        return tags == null ? new OutfitTags() : tags;
    }

    public String getImageUrl() {
        // 优先使用API返回的完整image_url，如果没有则使用拼接方式
        if (imageUrl != null && !imageUrl.isEmpty()) {
            return imageUrl;
        }
        String img = getImage();
        if (img != null && !img.isEmpty()) {
            return BASE_IMAGE_URL + img;
        }
        return "";
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLikes() {
        return likes != null ? likes : 0;
    }

    public void setLikes(Integer likes) {
        this.likes = likes;
    }

    public Boolean getIsFavorited() {
        return isFavorited;
    }

    public void setIsFavorited(Boolean isFavorited) {
        this.isFavorited = isFavorited;
    }
}

