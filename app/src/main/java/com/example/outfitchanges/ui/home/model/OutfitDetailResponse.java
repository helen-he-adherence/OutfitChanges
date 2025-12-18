package com.example.outfitchanges.ui.home.model;

import com.google.gson.annotations.SerializedName;

public class OutfitDetailResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("outfit")
    private OutfitDetail outfit;

    @SerializedName("is_logged_in")
    private Boolean isLoggedIn;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public OutfitDetail getOutfit() {
        return outfit;
    }

    public void setOutfit(OutfitDetail outfit) {
        this.outfit = outfit;
    }

    public Boolean getIsLoggedIn() {
        return isLoggedIn;
    }

    public void setIsLoggedIn(Boolean loggedIn) {
        isLoggedIn = loggedIn;
    }
}


