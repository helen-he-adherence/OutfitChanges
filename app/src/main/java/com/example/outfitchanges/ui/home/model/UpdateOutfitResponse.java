package com.example.outfitchanges.ui.home.model;

import com.google.gson.annotations.SerializedName;

public class UpdateOutfitResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("outfit")
    private OutfitDetail outfit;

    @SerializedName("message")
    private String message;

    @SerializedName("is_modified")
    private Object isModified; // 可能是Boolean、Integer或null

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
}

