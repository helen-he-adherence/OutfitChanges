package com.example.outfitchanges.ui.publish;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.outfitchanges.ui.publish.model.UploadResponse;
import com.example.outfitchanges.ui.publish.network.PublishApiService;
import com.example.outfitchanges.ui.publish.network.PublishNetworkClient;
import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.File;

public class PublishViewModel extends ViewModel {
    private final MutableLiveData<UploadResponse> uploadResult = new MutableLiveData<>();
    private final MutableLiveData<UploadResponse> saveResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final PublishApiService apiService;

    public PublishViewModel() {
        apiService = PublishNetworkClient.getInstance().getApiService();
    }

    public LiveData<UploadResponse> getUploadResult() {
        return uploadResult;
    }

    public LiveData<UploadResponse> getSaveResult() {
        return saveResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void uploadImage(File imageFile) {
        if (imageFile == null || !imageFile.exists()) {
            errorMessage.setValue("图片文件不存在");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/*"),
                imageFile
        );

        MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                "image",
                imageFile.getName(),
                requestFile
        );

        Call<UploadResponse> call = apiService.uploadImage(imagePart);
        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    uploadResult.setValue(response.body());
                } else {
                    String errorMsg = "上传失败";
                    try {
                        if (response.code() == 500) {
                            errorMsg = "服务器内部错误，请稍后重试";
                        } else if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("PublishViewModel", "Error response body: " + errorBody);
                            errorMsg = "上传失败: " + response.code();
                        } else if (response.message() != null && !response.message().isEmpty()) {
                            errorMsg = "上传失败: " + response.message();
                        } else {
                            errorMsg = "上传失败: HTTP " + response.code();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("PublishViewModel", "Error reading error body", e);
                        errorMsg = "上传失败: HTTP " + response.code();
                    }
                    errorMessage.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("网络错误: " + t.getMessage());
            }
        });
    }

    public void uploadImageWithModifiedTags(File imageFile, String modifiedTagsJson) {
        if (imageFile == null || !imageFile.exists()) {
            errorMessage.setValue("图片文件不存在");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        RequestBody requestFile = RequestBody.create(
                MediaType.parse("image/*"),
                imageFile
        );

        MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                "image",
                imageFile.getName(),
                requestFile
        );

        // 将 JSON 字符串作为文本字段发送（form-data），而不是 JSON 格式
        // 根据 curl 示例：-F "modified_tags={\"items\":[...]}"
        RequestBody tagsBody = RequestBody.create(
                MediaType.parse("text/plain"),
                modifiedTagsJson
        );

        android.util.Log.d("PublishViewModel", "Sending modified_tags: " + modifiedTagsJson);
        android.util.Log.d("PublishViewModel", "Image file: " + (imageFile != null ? imageFile.getAbsolutePath() : "null"));

        Call<UploadResponse> call = apiService.uploadImageWithTags(imagePart, tagsBody);
        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                isLoading.setValue(false);
                android.util.Log.d("PublishViewModel", "Save response code: " + response.code());
                android.util.Log.d("PublishViewModel", "Save response successful: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    android.util.Log.d("PublishViewModel", "Save success: " + response.body().isSuccess());
                    // 保存修改后的标签时，使用 saveResult 而不是 uploadResult
                    // 这样不会触发跳转到编辑界面
                    saveResult.setValue(response.body());
                } else {
                    String errorMsg = "保存失败";
                    try {
                        if (response.code() == 500) {
                            errorMsg = "服务器内部错误，请稍后重试";
                        } else if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("PublishViewModel", "Save error response body: " + errorBody);
                            errorMsg = "保存失败: HTTP " + response.code();
                        } else if (response.message() != null && !response.message().isEmpty()) {
                            errorMsg = "保存失败: " + response.message();
                        } else {
                            errorMsg = "保存失败: HTTP " + response.code();
                        }
                    } catch (Exception e) {
                        android.util.Log.e("PublishViewModel", "Error reading error body", e);
                        errorMsg = "保存失败: HTTP " + response.code();
                    }
                    errorMessage.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("网络错误: " + t.getMessage());
            }
        });
    }
}
