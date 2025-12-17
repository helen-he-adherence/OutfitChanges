package com.example.outfitchanges.ui.home.model;

import com.google.gson.annotations.SerializedName;

public class CreateOutfitResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("outfit_id")
    private int outfitId;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("tags")
    private OutfitTags tags;

    @SerializedName("raw_tags")
    private OutfitTags rawTags;

    @SerializedName("is_modified")
    private Object isModified; // 可能是Boolean、Integer或null

    @SerializedName("message")
    private String message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getOutfitId() {
        return outfitId;
    }

    public void setOutfitId(int outfitId) {
        this.outfitId = outfitId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public OutfitTags getTags() {
        return tags;
    }

    public void setTags(OutfitTags tags) {
        this.tags = tags;
    }

    public OutfitTags getRawTags() {
        return rawTags;
    }

    public void setRawTags(OutfitTags rawTags) {
        this.rawTags = rawTags;
    }

    /**
     * 获取是否已修改（处理数字和布尔值两种情况）
     */
    public boolean isModified() {
        if (isModified == null) {
            return false;
        }
        if (isModified instanceof Boolean) {
            return (Boolean) isModified;
        }
        if (isModified instanceof Number) {
            return ((Number) isModified).intValue() != 0;
        }
        return false;
    }

    public void setModified(Object modified) {
        isModified = modified;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

