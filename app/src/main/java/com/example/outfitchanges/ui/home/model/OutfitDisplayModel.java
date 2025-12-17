package com.example.outfitchanges.ui.home.model;

import java.util.List;

public class OutfitDisplayModel {
    private final String imageUrl;
    private final List<String> tags;
    private final String owner;
    private int likes;
    private boolean favorite;
    private int outfitId; // 添加outfitId字段，用于收藏等功能

    public OutfitDisplayModel(String imageUrl, List<String> tags, String owner, int likes) {
        this.imageUrl = imageUrl;
        this.tags = tags;
        this.owner = owner;
        this.likes = likes;
        this.favorite = false;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getOwner() {
        return owner;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = Math.max(0, likes);
    }

    public void adjustLikes(int delta) {
        setLikes(this.likes + delta);
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public int getOutfitId() {
        return outfitId;
    }

    public void setOutfitId(int outfitId) {
        this.outfitId = outfitId;
    }
}

