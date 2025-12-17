package com.example.outfitchanges.auth.model;

import com.google.gson.annotations.SerializedName;

public class ProfileResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("profile")
    private Profile profile;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public static class Profile {
        @SerializedName("id")
        private int id;

        @SerializedName("username")
        private String username;

        @SerializedName("email")
        private String email;

        @SerializedName("avatar_url")
        private String avatarUrl;

        @SerializedName("gender")
        private String gender;

        @SerializedName("age")
        private Integer age;

        @SerializedName("occupation")
        private String occupation;

        @SerializedName("bio")
        private String bio;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("last_login")
        private String lastLogin;

        @SerializedName("preferences")
        private Preferences preferences;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

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

        public String getBio() {
            return bio;
        }

        public void setBio(String bio) {
            this.bio = bio;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }

        public String getLastLogin() {
            return lastLogin;
        }

        public void setLastLogin(String lastLogin) {
            this.lastLogin = lastLogin;
        }

        public Preferences getPreferences() {
            return preferences;
        }

        public void setPreferences(Preferences preferences) {
            this.preferences = preferences;
        }
    }

    public static class Preferences {
        @SerializedName("preferred_styles")
        private String[] preferredStyles;

        @SerializedName("preferred_colors")
        private String[] preferredColors;

        @SerializedName("preferred_seasons")
        private String[] preferredSeasons;

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
}

