package com.example.outfitchanges.ui.home.model;

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OutfitClothingItem {
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
}

