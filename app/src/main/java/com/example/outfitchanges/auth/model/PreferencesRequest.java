package com.example.outfitchanges.auth.model;

import com.google.gson.annotations.SerializedName;

public class PreferencesRequest {
    @SerializedName("gender")
    private String gender;

    @SerializedName("age")
    private Integer age;

    @SerializedName("occupation")
    private String occupation;

    @SerializedName("preferred_styles")
    private String[] preferredStyles;

    @SerializedName("preferred_colors")
    private String[] preferredColors;

    @SerializedName("preferred_seasons")
    private String[] preferredSeasons;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String[] getPreferredStyles() {
        return preferredStyles;
    }

    public void setPreferredStyles(String[] preferredStyles) {
        this.preferredStyles = preferredStyles;
    }

    public String[] getPreferredColors() {
        return preferredColors;
    }

    public void setPreferredColors(String[] preferredColors) {
        this.preferredColors = preferredColors;
    }

    public String[] getPreferredSeasons() {
        return preferredSeasons;
    }

    public void setPreferredSeasons(String[] preferredSeasons) {
        this.preferredSeasons = preferredSeasons;
    }
}

