package com.example.outfitchanges.ui.home.data;

import android.content.Context;
import com.example.outfitchanges.ui.home.model.CreateOutfitResponse;
import com.example.outfitchanges.ui.home.model.DeleteOutfitResponse;
import com.example.outfitchanges.ui.home.model.FavoriteActionResponse;
import com.example.outfitchanges.ui.home.model.OutfitDetailResponse;
import com.example.outfitchanges.ui.home.model.OutfitListResponse;
import com.example.outfitchanges.ui.home.model.UpdateOutfitRequest;
import com.example.outfitchanges.ui.home.model.UpdateOutfitResponse;
import com.example.outfitchanges.ui.home.network.OutfitApiService;
import com.example.outfitchanges.ui.home.network.OutfitNetworkClient;
import com.example.outfitchanges.utils.TokenManager;
import java.io.File;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutfitRepository {

    public interface OutfitCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    private final OutfitApiService apiService;
    private Context context; // 用于访问 TokenManager

    public OutfitRepository() {
        apiService = OutfitNetworkClient.getRetrofit().create(OutfitApiService.class);
    }

    public OutfitRepository(Context context) {
        this();
        this.context = context;
    }

    /**
     * 获取穿搭列表（需要登录）
     */
    public void getOutfits(Integer limit, Integer offset, OutfitCallback<OutfitListResponse> callback) {
        Call<OutfitListResponse> call = apiService.getOutfits(limit, offset);
        call.enqueue(new Callback<OutfitListResponse>() {
            @Override
            public void onResponse(Call<OutfitListResponse> call, Response<OutfitListResponse> response) {
                android.util.Log.d("OutfitRepository", "getOutfits response code: " + response.code() + ", isSuccessful: " + response.isSuccessful());
                if (response.isSuccessful()) {
                    OutfitListResponse body = response.body();
                    android.util.Log.d("OutfitRepository", "Response body is null: " + (body == null));
                    if (body != null) {
                        android.util.Log.d("OutfitRepository", "Response success: " + body.isSuccess() + ", outfits count: " + (body.getOutfits() != null ? body.getOutfits().size() : 0));
                        callback.onSuccess(body);
                    } else {
                        android.util.Log.e("OutfitRepository", "Response body is null");
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                android.util.Log.e("OutfitRepository", "Error body: " + errorBody);
                            }
                        } catch (Exception e) {
                            android.util.Log.e("OutfitRepository", "Error reading error body", e);
                        }
                        callback.onError("获取穿搭列表失败: 响应体为空");
                    }
                } else {
                    String errorMsg = "获取穿搭列表失败: HTTP " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("OutfitRepository", "Error response: " + errorBody);
                            if (errorBody.length() < 200) {
                                errorMsg = "获取穿搭列表失败: " + errorBody;
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("OutfitRepository", "Error reading error body", e);
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<OutfitListResponse> call, Throwable t) {
                android.util.Log.e("OutfitRepository", "getOutfits onFailure", t);
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    /**
     * 创建穿搭（需要登录）
     */
    public void createOutfit(File imageFile, String modifiedTagsJson, OutfitCallback<CreateOutfitResponse> callback) {
        // 创建图片的 MultipartBody.Part
        RequestBody imageRequestBody = RequestBody.create(
                MediaType.parse("image/*"),
                imageFile
        );
        MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                "image",
                imageFile.getName(),
                imageRequestBody
        );

        // 创建 modified_tags 的 RequestBody（如果存在）
        RequestBody modifiedTagsBody = null;
        if (modifiedTagsJson != null && !modifiedTagsJson.isEmpty()) {
            modifiedTagsBody = RequestBody.create(
                    MediaType.parse("application/json"),
                    modifiedTagsJson
            );
        }

        Call<CreateOutfitResponse> call = apiService.createOutfit(imagePart, modifiedTagsBody);
        call.enqueue(new Callback<CreateOutfitResponse>() {
            @Override
            public void onResponse(Call<CreateOutfitResponse> call, Response<CreateOutfitResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "发布穿搭失败";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("OutfitRepository", "Error response: " + errorBody);
                            if (response.code() == 401) {
                                errorMsg = "未登录或token已过期";
                            } else {
                                errorMsg = "发布穿搭失败: HTTP " + response.code();
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("OutfitRepository", "Error reading error body", e);
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<CreateOutfitResponse> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    /**
     * 获取单个穿搭
     */
    public void getOutfitDetail(int outfitId, OutfitCallback<OutfitDetailResponse> callback) {
        Call<OutfitDetailResponse> call = apiService.getOutfitDetail(outfitId);
        call.enqueue(new Callback<OutfitDetailResponse>() {
            @Override
            public void onResponse(Call<OutfitDetailResponse> call, Response<OutfitDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("获取穿搭详情失败: HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<OutfitDetailResponse> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    /**
     * 更新穿搭（需要登录）
     */
    public void updateOutfit(int outfitId, UpdateOutfitRequest request, OutfitCallback<UpdateOutfitResponse> callback) {
        Call<UpdateOutfitResponse> call = apiService.updateOutfit(outfitId, request);
        call.enqueue(new Callback<UpdateOutfitResponse>() {
            @Override
            public void onResponse(Call<UpdateOutfitResponse> call, Response<UpdateOutfitResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "更新穿搭失败";
                    try {
                        if (response.code() == 401) {
                            errorMsg = "未登录或token已过期";
                        } else if (response.code() == 403) {
                            errorMsg = "无权修改此穿搭";
                        } else if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("OutfitRepository", "Error response: " + errorBody);
                            errorMsg = "更新穿搭失败: HTTP " + response.code();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("OutfitRepository", "Error reading error body", e);
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<UpdateOutfitResponse> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    /**
     * 删除穿搭（需要登录）
     */
    public void deleteOutfit(int outfitId, OutfitCallback<DeleteOutfitResponse> callback) {
        Call<DeleteOutfitResponse> call = apiService.deleteOutfit(outfitId);
        call.enqueue(new Callback<DeleteOutfitResponse>() {
            @Override
            public void onResponse(Call<DeleteOutfitResponse> call, Response<DeleteOutfitResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "删除穿搭失败";
                    try {
                        if (response.code() == 401) {
                            errorMsg = "未登录或token已过期";
                        } else if (response.code() == 403) {
                            errorMsg = "无权删除此穿搭";
                        } else {
                            errorMsg = "删除穿搭失败: HTTP " + response.code();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("OutfitRepository", "Error reading error body", e);
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<DeleteOutfitResponse> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    /**
     * 收藏穿搭（需要登录）
     */
    public void favoriteOutfit(int outfitId, OutfitCallback<FavoriteActionResponse> callback) {
        Call<FavoriteActionResponse> call = apiService.favoriteOutfit(outfitId);
        call.enqueue(new Callback<FavoriteActionResponse>() {
            @Override
            public void onResponse(Call<FavoriteActionResponse> call, Response<FavoriteActionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "收藏失败";
                    try {
                        if (response.code() == 401) {
                            errorMsg = "请先登录";
                        } else {
                            errorMsg = "收藏失败: HTTP " + response.code();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("OutfitRepository", "Error reading error body", e);
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<FavoriteActionResponse> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    /**
     * 取消收藏穿搭（需要登录）
     */
    public void unfavoriteOutfit(int outfitId, OutfitCallback<FavoriteActionResponse> callback) {
        Call<FavoriteActionResponse> call = apiService.unfavoriteOutfit(outfitId);
        call.enqueue(new Callback<FavoriteActionResponse>() {
            @Override
            public void onResponse(Call<FavoriteActionResponse> call, Response<FavoriteActionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "取消收藏失败";
                    try {
                        if (response.code() == 401) {
                            errorMsg = "请先登录";
                        } else {
                            errorMsg = "取消收藏失败: HTTP " + response.code();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("OutfitRepository", "Error reading error body", e);
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<FavoriteActionResponse> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    /**
     * 穿搭广场（可筛选）
     * 如果带 token 的请求返回 500 错误，会降级为不带 token 的请求
     */
    public void discoverOutfits(String season, String weather, String occasion, String style,
                                 String category, String color, Integer limit, Integer offset,
                                 OutfitCallback<OutfitListResponse> callback) {
        // 检查是否有 token，如果有则尝试带 token 的请求，否则直接使用不带 token 的请求
        String token = null;
        if (context != null) {
            token = com.example.outfitchanges.utils.TokenManager.getInstance(context).getToken();
        }
        boolean hasToken = token != null && !token.isEmpty();
        discoverOutfitsWithToken(season, weather, occasion, style, category, color, limit, offset, callback, hasToken);
    }

    /**
     * 内部方法：尝试带 token 的请求，如果失败则降级为不带 token 的请求
     */
    private void discoverOutfitsWithToken(String season, String weather, String occasion, String style,
                                          String category, String color, Integer limit, Integer offset,
                                          OutfitCallback<OutfitListResponse> callback, boolean withToken) {
        // 如果不需要token，传递X-Skip-Auth header来跳过token拦截器
        String skipAuth = withToken ? null : "true";
        Call<OutfitListResponse> call = apiService.discoverOutfits(
                season, weather, occasion, style, category, color, limit, offset, skipAuth
        );
        call.enqueue(new Callback<OutfitListResponse>() {
            @Override
            public void onResponse(Call<OutfitListResponse> call, Response<OutfitListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("OutfitRepository", "Discover outfits success, withToken: " + withToken);
                    callback.onSuccess(response.body());
                } else {
                    // 如果是 500 错误且是带 token 的请求，尝试不带 token 的降级请求
                    if (response.code() == 500 && withToken) {
                        android.util.Log.w("OutfitRepository", "Discover outfits with token returned 500, retrying without token");
                        // 使用X-Skip-Auth header重试，不需要清除token
                        discoverOutfitsWithToken(season, weather, occasion, style, category, color, limit, offset, callback, false);
                        return;
                    }
                    
                    // 如果是不带 token 的请求也返回 500，可能是服务器问题，但继续尝试
                    if (response.code() == 500 && !withToken) {
                        android.util.Log.e("OutfitRepository", "Discover outfits without token also returned 500, server error");
                    }
                    
                    String errorMsg = "获取穿搭广场失败: HTTP " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("OutfitRepository", "Discover outfits error: " + errorBody);
                            // 如果是HTML错误页面，尝试提取有用信息
                            if (errorBody.contains("500")) {
                                errorMsg = "服务器内部错误，请稍后重试";
                            } else if (errorBody.length() < 200) {
                                errorMsg = "获取穿搭广场失败: " + errorBody;
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("OutfitRepository", "Error reading error body", e);
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<OutfitListResponse> call, Throwable t) {
                // 如果是带 token 的请求失败，尝试不带 token 的降级请求
                if (withToken) {
                    android.util.Log.w("OutfitRepository", "Discover outfits with token failed, retrying without token: " + t.getMessage());
                    // 使用X-Skip-Auth header重试，不需要清除token
                    discoverOutfitsWithToken(season, weather, occasion, style, category, color, limit, offset, callback, false);
                } else {
                    android.util.Log.e("OutfitRepository", "Discover outfits without token also failed: " + t.getMessage());
                    callback.onError("网络错误: " + t.getMessage());
                }
            }
        });
    }

    /**
     * 个性化推荐（需要登录）
     */
    public void getPersonalizedOutfits(Integer limit, Integer offset, OutfitCallback<OutfitListResponse> callback) {
        Call<OutfitListResponse> call = apiService.getPersonalizedOutfits(limit, offset);
        call.enqueue(new Callback<OutfitListResponse>() {
            @Override
            public void onResponse(Call<OutfitListResponse> call, Response<OutfitListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "获取个性化推荐失败";
                    try {
                        if (response.code() == 401) {
                            errorMsg = "请先登录";
                        } else {
                            errorMsg = "获取个性化推荐失败: HTTP " + response.code();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("OutfitRepository", "Error reading error body", e);
                    }
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<OutfitListResponse> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }
}
