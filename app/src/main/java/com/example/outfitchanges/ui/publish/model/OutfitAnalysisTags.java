package com.example.outfitchanges.ui.publish.model;

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OutfitAnalysisTags {
    @SerializedName("items")
    private List<ClothingItem> items;

    @SerializedName("overall_style")
    private List<String> overallStyle;

    @SerializedName("occasion")
    private List<String> occasion;

    @SerializedName("season")
    private List<String> season;

    @SerializedName("weather")
    private List<String> weather;

    public List<ClothingItem> getItems() {
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

    public void setItems(List<ClothingItem> items) {
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

    public static class ClothingItem {
        @SerializedName("category")
        private String category;

        @SerializedName("attributes")
        private Map<String, String> attributes;

        @SerializedName("fabric")
        private List<String> fabric;

        @SerializedName("color")
        private List<String> color;

        @SerializedName("design_elements")
        private List<String> designElements;

        public String getCategory() {
            return category == null ? "" : category;
        }

        public Map<String, String> getAttributes() {
            return attributes == null ? Collections.emptyMap() : attributes;
        }

        public List<String> getFabric() {
            return fabric == null ? Collections.emptyList() : fabric;
        }

        public List<String> getColor() {
            return color == null ? Collections.emptyList() : color;
        }

        public List<String> getDesignElements() {
            return designElements == null ? Collections.emptyList() : designElements;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public void setAttributes(Map<String, String> attributes) {
            this.attributes = attributes;
        }

        public void setFabric(List<String> fabric) {
            this.fabric = fabric;
        }

        public void setColor(List<String> color) {
            this.color = color;
        }

        public void setDesignElements(List<String> designElements) {
            this.designElements = designElements;
        }
    }
}

