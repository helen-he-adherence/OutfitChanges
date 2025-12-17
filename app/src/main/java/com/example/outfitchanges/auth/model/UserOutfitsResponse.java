package com.example.outfitchanges.auth.model;

import com.example.outfitchanges.ui.home.model.OutfitItem;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class UserOutfitsResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("outfits")
    private List<OutfitItem> outfits;

    @SerializedName("user_id")
    private int userId;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<OutfitItem> getOutfits() {
        return outfits;
    }

    public void setOutfits(List<OutfitItem> outfits) {
        this.outfits = outfits;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}

