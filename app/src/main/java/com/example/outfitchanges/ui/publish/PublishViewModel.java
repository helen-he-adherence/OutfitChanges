package com.example.outfitchanges.ui.publish;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.outfitchanges.ui.home.model.CreateOutfitResponse;
import com.example.outfitchanges.ui.home.model.UpdateOutfitRequest;
import com.example.outfitchanges.ui.home.model.UpdateOutfitResponse;
import com.example.outfitchanges.ui.home.data.OutfitRepository;
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
    private final MutableLiveData<CreateOutfitResponse> createOutfitResult = new MutableLiveData<>();
    private final MutableLiveData<UpdateOutfitResponse> updateOutfitResult = new MutableLiveData<>();
    private final MutableLiveData<UploadResponse> uploadResult = new MutableLiveData<>(); // 保持兼容性
    private final MutableLiveData<UploadResponse> saveResult = new MutableLiveData<>(); // 保持兼容性
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final PublishApiService apiService;
    private final OutfitRepository outfitRepository;

    public PublishViewModel() {
        apiService = PublishNetworkClient.getInstance().getApiService();
        outfitRepository = new OutfitRepository();
    }

    public LiveData<CreateOutfitResponse> getCreateOutfitResult() {
        return createOutfitResult;
    }

    public LiveData<UpdateOutfitResponse> getUpdateOutfitResult() {
        return updateOutfitResult;
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

    /**
     * 创建穿搭（新的API，需要登录）
     */
    public void createOutfit(File imageFile, String modifiedTagsJson) {
        if (imageFile == null || !imageFile.exists()) {
            errorMessage.setValue("图片文件不存在");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        outfitRepository.createOutfit(imageFile, modifiedTagsJson, new OutfitRepository.OutfitCallback<CreateOutfitResponse>() {
            @Override
            public void onSuccess(CreateOutfitResponse data) {
                isLoading.setValue(false);
                createOutfitResult.setValue(data);
                // 同时设置uploadResult以保持兼容性
                UploadResponse uploadResponse = new UploadResponse();
                uploadResponse.setSuccess(data.isSuccess());
                uploadResponse.setMessage(data.getMessage());
                uploadResponse.setImageUrl(data.getImageUrl());
                uploadResponse.setOutfitId(data.getOutfitId());
                // 如果需要tags，可以从data.getTags()转换
                uploadResult.setValue(uploadResponse);
            }

            @Override
            public void onError(String errorMsg) {
                isLoading.setValue(false);
                errorMessage.setValue(errorMsg);
            }
        });
    }

    /**
     * 更新穿搭标签（需要登录）
     */
    public void updateOutfitTags(int outfitId, com.example.outfitchanges.ui.home.model.OutfitTags tags, Boolean isPublic) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        UpdateOutfitRequest request = new UpdateOutfitRequest();
        request.setTags(tags);
        request.setIsPublic(isPublic);

        outfitRepository.updateOutfit(outfitId, request, new OutfitRepository.OutfitCallback<UpdateOutfitResponse>() {
            @Override
            public void onSuccess(UpdateOutfitResponse data) {
                isLoading.setValue(false);
                updateOutfitResult.setValue(data);
            }

            @Override
            public void onError(String errorMsg) {
                isLoading.setValue(false);
                errorMessage.setValue(errorMsg);
            }
        });
    }

    /**
     * 旧版uploadImage方法（保持兼容性，现在调用createOutfit）
     */
    public void uploadImage(File imageFile) {
        createOutfit(imageFile, null);
    }

    /**
     * 上传图片并修改标签（新的API，需要登录）
     */
    public void uploadImageWithModifiedTags(File imageFile, String modifiedTagsJson) {
        createOutfit(imageFile, modifiedTagsJson);
    }
}
