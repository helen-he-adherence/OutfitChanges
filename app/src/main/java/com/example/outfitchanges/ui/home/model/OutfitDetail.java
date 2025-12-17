package com.example.outfitchanges.ui.home.model;

import com.google.gson.annotations.SerializedName;

public class OutfitDetail {
    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("image_url")
    private String imageUrl;

    @SerializedName("raw_tags")
    private OutfitTags rawTags;

    @SerializedName("modified_tags")
    private OutfitTags modifiedTags;

    @SerializedName("tags")
    private OutfitTags tags;

    @SerializedName("is_modified")
    private Object isModified; // 可能是Boolean、Integer或null

    @SerializedName("modified_at")
    private String modifiedAt;

    @SerializedName("likes")
    private int likes;

    @SerializedName("views")
    private int views;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("is_public")
    private Object isPublic; // 可能是Boolean、Integer或null

    @SerializedName("is_favorited")
    private Object isFavorited; // 可能是Boolean、Integer或null（未登录用户）

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public OutfitTags getRawTags() {
        return rawTags;
    }

    public void setRawTags(OutfitTags rawTags) {
        this.rawTags = rawTags;
    }

    public OutfitTags getModifiedTags() {
        return modifiedTags;
    }

    public void setModifiedTags(OutfitTags modifiedTags) {
        this.modifiedTags = modifiedTags;
    }

    public OutfitTags getTags() {
        return tags;
    }

    public void setTags(OutfitTags tags) {
        this.tags = tags;
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

    public String getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(String modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 获取是否公开（处理数字和布尔值两种情况）
     */
    public boolean isPublic() {
        if (isPublic == null) {
            return false;
        }
        if (isPublic instanceof Boolean) {
            return (Boolean) isPublic;
        }
        if (isPublic instanceof Number) {
            return ((Number) isPublic).intValue() != 0;
        }
        return false;
    }

    public void setPublic(Object aPublic) {
        isPublic = aPublic;
    }

    /**
     * 获取是否收藏（处理数字和布尔值两种情况）
     */
    public Boolean getIsFavorited() {
        if (isFavorited == null) {
            return null;
        }
        if (isFavorited instanceof Boolean) {
            return (Boolean) isFavorited;
        }
        if (isFavorited instanceof Number) {
            return ((Number) isFavorited).intValue() != 0;
        }
        return false;
    }

    public void setIsFavorited(Object favorited) {
        isFavorited = favorited;
    }
}

