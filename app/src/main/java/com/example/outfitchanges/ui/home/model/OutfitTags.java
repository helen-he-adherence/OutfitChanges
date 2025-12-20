package com.example.outfitchanges.ui.home.model;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OutfitTags {
    @SerializedName("items")
    private List<OutfitClothingItem> items;

    @SerializedName("overall_style")
    @JsonAdapter(StringListTypeAdapter.class)
    private List<String> overallStyle;

    @SerializedName("occasion")
    @JsonAdapter(StringListTypeAdapter.class)
    private List<String> occasion;

    @SerializedName("season")
    @JsonAdapter(StringListTypeAdapter.class)
    private List<String> season;

    // 数据里可能没有天气字段，预留便于扩展
    @SerializedName("weather")
    @JsonAdapter(StringListTypeAdapter.class)
    private List<String> weather;

    @SerializedName("sex")
    @JsonAdapter(SexTypeAdapter.class)
    private List<String> sex;

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

    public List<String> getSex() {
        return sex == null ? Collections.emptyList() : sex;
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

    // Setters
    public void setItems(List<OutfitClothingItem> items) {
        this.items = items;
    }

    public void setOverallStyle(List<String> overallStyle) {
        this.overallStyle = overallStyle;
    }

    public void setOccasion(List<String> occasion) {
        this.occasion = occasion;
    }

    public void setSeason(List<String> season) {
        this.season = season;
    }

    public void setWeather(List<String> weather) {
        this.weather = weather;
    }

    public void setSex(List<String> sex) {
        this.sex = sex;
    }
}

