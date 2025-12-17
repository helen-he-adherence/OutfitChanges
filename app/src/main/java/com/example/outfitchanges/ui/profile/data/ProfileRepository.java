package com.example.outfitchanges.ui.profile.data;

import com.example.outfitchanges.auth.model.FavoritesResponse;
import com.example.outfitchanges.auth.model.PreferencesRequest;
import com.example.outfitchanges.auth.model.ProfileResponse;
import com.example.outfitchanges.auth.model.UpdatePreferencesResponse;
import com.example.outfitchanges.auth.model.UserOutfitsResponse;
import com.example.outfitchanges.auth.network.AuthApiService;
import com.example.outfitchanges.auth.network.AuthNetworkClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileRepository {
    private final AuthApiService apiService;

    public ProfileRepository() {
        apiService = AuthNetworkClient.getInstance().getApiService();
    }

    public interface ProfileCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    /**
     * 获取个人资料
     */
    public void getProfile(ProfileCallback<ProfileResponse.Profile> callback) {
        Call<ProfileResponse> call = apiService.getProfile();
        call.enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profileResponse = response.body();
                    if (profileResponse.isSuccess() && profileResponse.getProfile() != null) {
                        callback.onSuccess(profileResponse.getProfile());
                    } else {
                        callback.onError("获取个人资料失败");
                    }
                } else {
                    String errorMsg = "获取个人资料失败";
                    try {
                        if (response.code() == 401) {
                            errorMsg = "未登录或token已过期";
                        } else if (response.errorBody() != null) {
                            errorMsg = "获取个人资料失败: HTTP " + response.code();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ProfileRepository", "Error reading error body", e);
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
                android.util.Log.e("ProfileRepository", "Get profile failed", t);
            }
        });
    }

    /**
     * 更新偏好设置
     */
    public void updatePreferences(PreferencesRequest request, ProfileCallback<String> callback) {
        Call<UpdatePreferencesResponse> call = apiService.updatePreferences(request);
        call.enqueue(new Callback<UpdatePreferencesResponse>() {
            @Override
            public void onResponse(Call<UpdatePreferencesResponse> call, Response<UpdatePreferencesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UpdatePreferencesResponse updateResponse = response.body();
                    if (updateResponse.isSuccess()) {
                        callback.onSuccess(updateResponse.getMessage() != null ? updateResponse.getMessage() : "偏好设置已更新");
                    } else {
                        callback.onError(updateResponse.getMessage() != null ? updateResponse.getMessage() : "更新偏好设置失败");
                    }
                } else {
                    String errorMsg = "更新偏好设置失败";
                    try {
                        if (response.code() == 401) {
                            errorMsg = "未登录或token已过期";
                        } else if (response.errorBody() != null) {
                            errorMsg = "更新偏好设置失败: HTTP " + response.code();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ProfileRepository", "Error reading error body", e);
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<UpdatePreferencesResponse> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
                android.util.Log.e("ProfileRepository", "Update preferences failed", t);
            }
        });
    }

    /**
     * 获取用户收藏列表
     */
    public void getFavorites(ProfileCallback<FavoritesResponse> callback) {
        Call<FavoritesResponse> call = apiService.getFavorites();
        call.enqueue(new Callback<FavoritesResponse>() {
            @Override
            public void onResponse(Call<FavoritesResponse> call, Response<FavoritesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    FavoritesResponse favoritesResponse = response.body();
                    if (favoritesResponse.isSuccess()) {
                        callback.onSuccess(favoritesResponse);
                    } else {
                        callback.onError("获取收藏列表失败");
                    }
                } else {
                    String errorMsg = "获取收藏列表失败";
                    try {
                        if (response.code() == 401) {
                            errorMsg = "未登录或token已过期";
                        } else if (response.errorBody() != null) {
                            errorMsg = "获取收藏列表失败: HTTP " + response.code();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ProfileRepository", "Error reading error body", e);
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<FavoritesResponse> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
                android.util.Log.e("ProfileRepository", "Get favorites failed", t);
            }
        });
    }

    /**
     * 获取用户自己的穿搭
     */
    public void getUserOutfits(ProfileCallback<UserOutfitsResponse> callback) {
        Call<UserOutfitsResponse> call = apiService.getUserOutfits();
        call.enqueue(new Callback<UserOutfitsResponse>() {
            @Override
            public void onResponse(Call<UserOutfitsResponse> call, Response<UserOutfitsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserOutfitsResponse outfitsResponse = response.body();
                    if (outfitsResponse.isSuccess()) {
                        callback.onSuccess(outfitsResponse);
                    } else {
                        callback.onError("获取我的穿搭失败");
                    }
                } else {
                    String errorMsg = "获取我的穿搭失败";
                    try {
                        if (response.code() == 401) {
                            errorMsg = "未登录或token已过期";
                        } else if (response.errorBody() != null) {
                            errorMsg = "获取我的穿搭失败: HTTP " + response.code();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("ProfileRepository", "Error reading error body", e);
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<UserOutfitsResponse> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
                android.util.Log.e("ProfileRepository", "Get user outfits failed", t);
            }
        });
    }
}

