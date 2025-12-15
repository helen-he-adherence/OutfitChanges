package com.example.outfitchanges.ui.home.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OutfitTags {
    @SerializedName("items")
    private List<OutfitClothingItem> items;

    @SerializedName("overall_style")
    private List<String> overallStyle;

    @SerializedName("occasion")
    private List<String> occasion;

    @SerializedName("season")
    private List<String> season;

    // 数据里可能没有天气字段，预留便于扩展
    @SerializedName("weather")
    private List<String> weather;

    public List<OutfitClothingItem> getItems() {
        return items == null ? Collections.emptyList() : items;
    }

    public List<String> getOverallStyle() {
        return overallStyle == null ? Collections.emptyList() : overallStyle;
    }

    public List<String> getOccasion() {
        return occasion == null ? Collections.emptyList() : occasion;
    }

    public List<String> getSeason() {
        return season == null ? Collections.emptyList() : season;
    }

    public List<String> getWeather() {
        return weather == null ? Collections.emptyList() : weather;
    }

    public List<String> getAllCategories() {
        List<String> result = new ArrayList<>();
        for (OutfitClothingItem item : getItems()) {
            if (!item.getCategory().isEmpty()) {
                result.add(item.getCategory());
            }
        }
        return result;
    }

    public List<String> getAllColors() {
        List<String> result = new ArrayList<>();
        for (OutfitClothingItem item : getItems()) {
            result.addAll(item.getColor());
        }
        return result;
    }

    public List<String> getAllDesignElements() {
        List<String> result = new ArrayList<>();
        for (OutfitClothingItem item : getItems()) {
            result.addAll(item.getDesignElements());
        }
        return result;
    }
}

