package com.example.outfitchanges.ui.home.model;

import com.google.gson.annotations.SerializedName;

public class UpdateOutfitRequest {
    @SerializedName("tags")
    private OutfitTags tags;

    @SerializedName("is_public")
    private Boolean isPublic;

    public OutfitTags getTags() {
        return tags;
    }

    public void setTags(OutfitTags tags) {
        this.tags = tags;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean aPublic) {
        isPublic = aPublic;
    }
}








