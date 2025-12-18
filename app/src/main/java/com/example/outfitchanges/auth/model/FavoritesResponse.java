package com.example.outfitchanges.auth.model;

import com.example.outfitchanges.ui.home.model.OutfitTags;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FavoritesResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("favorites")
    private List<FavoriteItem> favorites;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<FavoriteItem> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<FavoriteItem> favorites) {
        this.favorites = favorites;
    }

    public static class FavoriteItem {
        @SerializedName("id")
        private int id;

        @SerializedName("image_url")
        private String imageUrl;

        @SerializedName("tags")
        private OutfitTags tags;

        @SerializedName("raw_tags")
        private Object rawTags; // 可以是任意JSON对象

        @SerializedName("is_modified")
        private Object isModified; // 可能是Boolean、Integer或null

        @SerializedName("likes")
        private int likes;

        @SerializedName("views")
        private int views;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("favorited_at")
        private String favoritedAt;

        @SerializedName("is_favorited")
        private boolean isFavorited;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
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

        public Object getRawTags() {
            return rawTags;
        }

        public void setRawTags(Object rawTags) {
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

        public String getFavoritedAt() {
            return favoritedAt;
        }

        public void setFavoritedAt(String favoritedAt) {
            this.favoritedAt = favoritedAt;
        }

        public boolean isFavorited() {
            return isFavorited;
        }

        public void setFavorited(boolean favorited) {
            isFavorited = favorited;
        }
    }
}

