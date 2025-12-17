package com.example.outfitchanges.ui.home.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OutfitListResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("outfits")
    private List<OutfitListItem> outfits;

    @SerializedName("pagination")
    private Pagination pagination;

    @SerializedName("filters_applied")
    private FiltersApplied filtersApplied;

    @SerializedName("is_logged_in")
    private Boolean isLoggedIn;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<OutfitListItem> getOutfits() {
        return outfits;
    }

    public void setOutfits(List<OutfitListItem> outfits) {
        this.outfits = outfits;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public FiltersApplied getFiltersApplied() {
        return filtersApplied;
    }

    public void setFiltersApplied(FiltersApplied filtersApplied) {
        this.filtersApplied = filtersApplied;
    }

    public Boolean getIsLoggedIn() {
        return isLoggedIn;
    }

    public void setIsLoggedIn(Boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public static class OutfitListItem {
        @SerializedName("id")
        private int id;

        @SerializedName("user_id")
        private String userId;

        @SerializedName("image_url")
        private String imageUrl;

        @SerializedName("tags")
        private OutfitTags tags;

        @SerializedName("raw_tags")
        private OutfitTags rawTags;

        @SerializedName("is_modified")
        private Object isModified; // 可能是Boolean、Integer或null

        @SerializedName("likes")
        private int likes;

        @SerializedName("views")
        private int views;

        @SerializedName("created_at")
        private String createdAt;

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

    public static class Pagination {
        @SerializedName("total")
        private int total;

        @SerializedName("limit")
        private int limit;

        @SerializedName("offset")
        private int offset;

        @SerializedName("has_more")
        private boolean hasMore;

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        public boolean isHasMore() {
            return hasMore;
        }

        public void setHasMore(boolean hasMore) {
            this.hasMore = hasMore;
        }
    }

    public static class FiltersApplied {
        @SerializedName("season")
        private List<String> season;

        @SerializedName("weather")
        private List<String> weather;

        @SerializedName("occasion")
        private List<String> occasion;

        @SerializedName("style")
        private List<String> style;

        @SerializedName("category")
        private List<String> category;

        @SerializedName("color")
        private List<String> color;

        public List<String> getSeason() {
            return season;
        }

        public void setSeason(List<String> season) {
            this.season = season;
        }

        public List<String> getWeather() {
            return weather;
        }

        public void setWeather(List<String> weather) {
            this.weather = weather;
        }

        public List<String> getOccasion() {
            return occasion;
        }

        public void setOccasion(List<String> occasion) {
            this.occasion = occasion;
        }

        public List<String> getStyle() {
            return style;
        }

        public void setStyle(List<String> style) {
            this.style = style;
        }

        public List<String> getCategory() {
            return category;
        }

        public void setCategory(List<String> category) {
            this.category = category;
        }

        public List<String> getColor() {
            return color;
        }

        public void setColor(List<String> color) {
            this.color = color;
        }
    }
}

