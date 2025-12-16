package com.example.outfitchanges.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String PREF_NAME = "OutfitChangesPref";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_AGE = "age";
    private static final String KEY_OCCUPATION = "occupation";
    private static final String KEY_PREFERRED_STYLE = "preferred_style";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setUserId(String userId) {
        editor.putString(KEY_USER_ID, userId);
        editor.apply();
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, "");
    }

    public void setUsername(String username) {
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, "未登录");
    }

    public void setEmail(String email) {
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, "");
    }

    public void setGender(String gender) {
        editor.putString(KEY_GENDER, gender);
        editor.apply();
    }

    public String getGender() {
        return sharedPreferences.getString(KEY_GENDER, "");
    }

    public void setAge(String age) {
        editor.putString(KEY_AGE, age);
        editor.apply();
    }

    public String getAge() {
        return sharedPreferences.getString(KEY_AGE, "");
    }

    public void setOccupation(String occupation) {
        editor.putString(KEY_OCCUPATION, occupation);
        editor.apply();
    }

    public String getOccupation() {
        return sharedPreferences.getString(KEY_OCCUPATION, "");
    }

    public void setPreferredStyle(String style) {
        editor.putString(KEY_PREFERRED_STYLE, style);
        editor.apply();
    }

    public String getPreferredStyle() {
        return sharedPreferences.getString(KEY_PREFERRED_STYLE, "");
    }

    public void clear() {
        editor.clear();
        editor.apply();
    }
}
