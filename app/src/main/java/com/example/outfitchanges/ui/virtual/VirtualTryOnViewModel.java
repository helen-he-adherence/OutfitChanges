package com.example.outfitchanges.ui.virtual;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.outfitchanges.ui.virtual.model.VirtualTryOnResponse;
import com.example.outfitchanges.ui.virtual.network.VirtualTryOnApiService;
import com.example.outfitchanges.ui.virtual.network.VirtualTryOnNetworkClient;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.File;

public class VirtualTryOnViewModel extends ViewModel {
    private final MutableLiveData<VirtualTryOnResponse> tryOnResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> taskStatus = new MutableLiveData<>();
    private final VirtualTryOnApiService apiService;

    public VirtualTryOnViewModel() {
        apiService = VirtualTryOnNetworkClient.getInstance().getApiService();
    }

    public LiveData<VirtualTryOnResponse> getTryOnResult() {
        return tryOnResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getTaskStatus() {
        return taskStatus;
    }

    // 提供方法来控制加载状态（供Fragment调用）
    public void setLoading(boolean loading) {
        isLoading.setValue(loading);
    }

    public void submitTryOnWithOutfitId(File targetImageFile, int outfitId, String token) {
        if (targetImageFile == null || !targetImageFile.exists()) {
            errorMessage.setValue("人像图片不存在");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/*"),
                targetImageFile
        );

        MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                "target_image",
                targetImageFile.getName(),
                requestFile
        );

        RequestBody outfitIdBody = RequestBody.create(
                MediaType.parse("text/plain"),
                String.valueOf(outfitId)
        );

        String authHeader = "Bearer " + token;
        Call<VirtualTryOnResponse> call = apiService.submitTryOnTask(authHeader, imagePart, outfitIdBody);
        call.enqueue(new Callback<VirtualTryOnResponse>() {
            @Override
            public void onResponse(Call<VirtualTryOnResponse> call, Response<VirtualTryOnResponse> response) {
                // 202 ACCEPTED 也是成功状态
                if ((response.isSuccessful() || response.code() == 202) && response.body() != null) {
                    VirtualTryOnResponse result = response.body();
                    android.util.Log.d("VirtualTryOnViewModel", "Submit response: success=" + result.isSuccess() + ", taskId=" + result.getTaskId());
                    tryOnResult.setValue(result);
                    // 不设置isLoading为false，因为需要继续轮询
                } else {
                    isLoading.setValue(false);
                    String errorMsg = "提交失败";
                    try {
                        if (response.code() == 500) {
                            errorMsg = "服务器内部错误，请稍后重试";
                        } else if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("VirtualTryOnViewModel", "Error response: " + errorBody);
                            errorMsg = "提交失败: HTTP " + response.code();
                        } else {
                            errorMsg = "提交失败: HTTP " + response.code();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("VirtualTryOnViewModel", "Error reading error body", e);
                        errorMsg = "提交失败: HTTP " + response.code();
                    }
                    errorMessage.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<VirtualTryOnResponse> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("网络错误: " + t.getMessage());
                android.util.Log.e("VirtualTryOnViewModel", "Submit try-on failed", t);
            }
        });
    }

    public void submitTryOnWithSourceImage(File targetImageFile, File sourceImageFile, String token) {
        android.util.Log.d("VirtualTryOnViewModel", "=== submitTryOnWithSourceImage 开始 ===");
        android.util.Log.d("VirtualTryOnViewModel", "Target image: " + (targetImageFile != null ? targetImageFile.getAbsolutePath() : "null"));
        android.util.Log.d("VirtualTryOnViewModel", "Source image: " + (sourceImageFile != null ? sourceImageFile.getAbsolutePath() : "null"));
        android.util.Log.d("VirtualTryOnViewModel", "Token length: " + (token != null ? token.length() : 0));
        
        if (targetImageFile == null || !targetImageFile.exists()) {
            android.util.Log.e("VirtualTryOnViewModel", "人像图片不存在");
            errorMessage.setValue("人像图片不存在");
            return;
        }
        if (sourceImageFile == null || !sourceImageFile.exists()) {
            android.util.Log.e("VirtualTryOnViewModel", "服装图片不存在");
            errorMessage.setValue("服装图片不存在");
            return;
        }

        android.util.Log.d("VirtualTryOnViewModel", "设置isLoading=true");
        isLoading.setValue(true);
        errorMessage.setValue(null);

        RequestBody targetFile = RequestBody.create(
                MediaType.parse("image/*"),
                targetImageFile
        );

        RequestBody sourceFile = RequestBody.create(
                MediaType.parse("image/*"),
                sourceImageFile
        );

        MultipartBody.Part targetImagePart = MultipartBody.Part.createFormData(
                "target_image",
                targetImageFile.getName(),
                targetFile
        );

        MultipartBody.Part sourceImagePart = MultipartBody.Part.createFormData(
                "source_image",
                sourceImageFile.getName(),
                sourceFile
        );

        String authHeader = "Bearer " + token;
        android.util.Log.d("VirtualTryOnViewModel", "创建API请求，authHeader: " + (authHeader.length() > 20 ? authHeader.substring(0, 20) + "..." : authHeader));
        android.util.Log.d("VirtualTryOnViewModel", "Target image part name: " + targetImageFile.getName());
        android.util.Log.d("VirtualTryOnViewModel", "Source image part name: " + sourceImageFile.getName());
        
        Call<VirtualTryOnResponse> call = apiService.submitTryOnTaskWithSourceImage(authHeader, targetImagePart, sourceImagePart);
        android.util.Log.d("VirtualTryOnViewModel", "API请求已创建，开始执行...");
        call.enqueue(new Callback<VirtualTryOnResponse>() {
            @Override
            public void onResponse(Call<VirtualTryOnResponse> call, Response<VirtualTryOnResponse> response) {
                android.util.Log.d("VirtualTryOnViewModel", "=== API响应收到 ===");
                android.util.Log.d("VirtualTryOnViewModel", "Response code: " + response.code());
                android.util.Log.d("VirtualTryOnViewModel", "Response isSuccessful: " + response.isSuccessful());
                android.util.Log.d("VirtualTryOnViewModel", "Response body is null: " + (response.body() == null));
                
                // 202 ACCEPTED 也是成功状态
                if ((response.isSuccessful() || response.code() == 202) && response.body() != null) {
                    VirtualTryOnResponse result = response.body();
                    android.util.Log.d("VirtualTryOnViewModel", "=== 提交成功 ===");
                    android.util.Log.d("VirtualTryOnViewModel", "Success: " + result.isSuccess());
                    android.util.Log.d("VirtualTryOnViewModel", "Message: " + result.getMessage());
                    android.util.Log.d("VirtualTryOnViewModel", "Status: " + result.getStatus());
                    android.util.Log.d("VirtualTryOnViewModel", "TaskId: " + result.getTaskId());
                    
                    tryOnResult.setValue(result);
                    android.util.Log.d("VirtualTryOnViewModel", "已设置tryOnResult，等待Fragment观察者处理");
                    // 不设置isLoading为false，因为需要继续轮询
                } else {
                    android.util.Log.e("VirtualTryOnViewModel", "=== 提交失败 ===");
                    isLoading.setValue(false);
                    String errorMsg = "提交失败";
                    try {
                        if (response.code() == 500) {
                            errorMsg = "服务器内部错误，请稍后重试";
                        } else if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("VirtualTryOnViewModel", "Error response body: " + errorBody);
                            errorMsg = "提交失败: HTTP " + response.code();
                        } else {
                            android.util.Log.e("VirtualTryOnViewModel", "Error response body is null");
                            errorMsg = "提交失败: HTTP " + response.code();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("VirtualTryOnViewModel", "Error reading error body", e);
                        errorMsg = "提交失败: HTTP " + response.code();
                    }
                    errorMessage.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<VirtualTryOnResponse> call, Throwable t) {
                android.util.Log.e("VirtualTryOnViewModel", "=== API请求失败 ===");
                android.util.Log.e("VirtualTryOnViewModel", "Error: " + t.getMessage(), t);
                isLoading.setValue(false);
                errorMessage.setValue("网络错误: " + t.getMessage());
            }
        });
    }

    public void checkTaskStatus(String taskId, String token) {
        android.util.Log.d("VirtualTryOnViewModel", "=== checkTaskStatus 开始 ===");
        android.util.Log.d("VirtualTryOnViewModel", "TaskId: " + taskId);
        android.util.Log.d("VirtualTryOnViewModel", "Token length: " + (token != null ? token.length() : 0));
        
        String authHeader = "Bearer " + token;
        Call<VirtualTryOnResponse> call = apiService.getTaskStatus(authHeader, taskId);
        android.util.Log.d("VirtualTryOnViewModel", "API请求已创建，开始执行...");
        call.enqueue(new Callback<VirtualTryOnResponse>() {
            @Override
            public void onResponse(Call<VirtualTryOnResponse> call, Response<VirtualTryOnResponse> response) {
                android.util.Log.d("VirtualTryOnViewModel", "=== 状态查询响应收到 ===");
                android.util.Log.d("VirtualTryOnViewModel", "Response code: " + response.code());
                android.util.Log.d("VirtualTryOnViewModel", "Response isSuccessful: " + response.isSuccessful());
                android.util.Log.d("VirtualTryOnViewModel", "Response body is null: " + (response.body() == null));
                
                if (response.isSuccessful() && response.body() != null) {
                    VirtualTryOnResponse result = response.body();
                    String status = result.getStatus();
                    String resultUrl = result.getResultUrl();
                    android.util.Log.d("VirtualTryOnViewModel", "=== 状态查询成功 ===");
                    android.util.Log.d("VirtualTryOnViewModel", "Status: " + status);
                    android.util.Log.d("VirtualTryOnViewModel", "ResultUrl: " + resultUrl);
                    
                    taskStatus.setValue(status);
                    android.util.Log.d("VirtualTryOnViewModel", "已设置taskStatus为: " + status);
                    
                    if ("completed".equals(status) && resultUrl != null) {
                        android.util.Log.d("VirtualTryOnViewModel", "任务完成，设置结果");
                        // 任务完成，停止加载
                        isLoading.setValue(false);
                        // 确保success字段为true
                        result.setSuccess(true);
                        tryOnResult.setValue(result);
                        android.util.Log.d("VirtualTryOnViewModel", "已设置tryOnResult，等待Fragment观察者处理");
                    } else {
                        android.util.Log.d("VirtualTryOnViewModel", "任务未完成，继续等待");
                    }
                } else {
                    android.util.Log.e("VirtualTryOnViewModel", "=== 状态查询失败 ===");
                    android.util.Log.e("VirtualTryOnViewModel", "HTTP code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("VirtualTryOnViewModel", "Error body: " + errorBody);
                        } catch (Exception e) {
                            android.util.Log.e("VirtualTryOnViewModel", "Error reading error body", e);
                        }
                    }
                    // 查询失败不停止轮询，继续尝试
                }
            }

            @Override
            public void onFailure(Call<VirtualTryOnResponse> call, Throwable t) {
                android.util.Log.e("VirtualTryOnViewModel", "=== 状态查询网络错误 ===");
                android.util.Log.e("VirtualTryOnViewModel", "Error: " + t.getMessage(), t);
                // 网络错误不停止轮询，继续尝试
            }
        });
    }
}

