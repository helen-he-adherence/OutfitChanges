package com.example.outfitchanges.ui.virtual.model;

public class VirtualTryOnRequest {
    private String targetImage; // 人像图片（base64或URL）
    private String sourceImage; // 服装图片（base64或URL，可选）
    private Integer outfitId; // 穿搭ID（可选）

    public VirtualTryOnRequest() {
    }

    public VirtualTryOnRequest(String targetImage, String sourceImage) {
        this.targetImage = targetImage;
        this.sourceImage = sourceImage;
    }

    public VirtualTryOnRequest(String targetImage, Integer outfitId) {
        this.targetImage = targetImage;
        this.outfitId = outfitId;
    }

    public String getTargetImage() {
        return targetImage;
    }

    public void setTargetImage(String targetImage) {
        this.targetImage = targetImage;
    }

    public String getSourceImage() {
        return sourceImage;
    }

    public void setSourceImage(String sourceImage) {
        this.sourceImage = sourceImage;
    }

    public Integer getOutfitId() {
        return outfitId;
    }

    public void setOutfitId(Integer outfitId) {
        this.outfitId = outfitId;
    }
}








