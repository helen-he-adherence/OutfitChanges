package com.example.outfitchanges.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.outfitchanges.auth.model.FavoritesResponse;
import com.example.outfitchanges.auth.model.PreferencesRequest;
import com.example.outfitchanges.auth.model.ProfileResponse;
import com.example.outfitchanges.auth.model.UserOutfitsResponse;
import com.example.outfitchanges.ui.profile.data.ProfileRepository;

public class ProfileViewModel extends ViewModel {
    private final ProfileRepository repository;
    
    private MutableLiveData<ProfileResponse.Profile> profile = new MutableLiveData<>();
    private MutableLiveData<FavoritesResponse> favorites = new MutableLiveData<>();
    private MutableLiveData<UserOutfitsResponse> userOutfits = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> updateSuccessMessage = new MutableLiveData<>();

    public ProfileViewModel() {
        repository = new ProfileRepository();
    }

    /**
     * 获取个人资料
     */
    public void loadProfile() {
        isLoading.setValue(true);
        repository.getProfile(new ProfileRepository.ProfileCallback<ProfileResponse.Profile>() {
            @Override
            public void onSuccess(ProfileResponse.Profile data) {
                profile.postValue(data);
                isLoading.postValue(false);
                errorMessage.postValue(null);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }

    /**
     * 更新偏好设置
     */
    public void updatePreferences(PreferencesRequest request) {
        isLoading.setValue(true);
        repository.updatePreferences(request, new ProfileRepository.ProfileCallback<String>() {
            @Override
            public void onSuccess(String data) {
                updateSuccessMessage.postValue(data);
                isLoading.postValue(false);
                errorMessage.postValue(null);
                // 更新成功后重新加载个人资料
                loadProfile();
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }

    /**
     * 获取用户收藏列表
     */
    public void loadFavorites() {
        isLoading.setValue(true);
        repository.getFavorites(new ProfileRepository.ProfileCallback<FavoritesResponse>() {
            @Override
            public void onSuccess(FavoritesResponse data) {
                favorites.postValue(data);
                isLoading.postValue(false);
                errorMessage.postValue(null);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }

    /**
     * 获取用户自己的穿搭
     */
    public void loadUserOutfits() {
        isLoading.setValue(true);
        repository.getUserOutfits(new ProfileRepository.ProfileCallback<UserOutfitsResponse>() {
            @Override
            public void onSuccess(UserOutfitsResponse data) {
                userOutfits.postValue(data);
                isLoading.postValue(false);
                errorMessage.postValue(null);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });
    }

    // Getter methods
    public LiveData<ProfileResponse.Profile> getProfile() {
        return profile;
    }

    public LiveData<FavoritesResponse> getFavorites() {
        return favorites;
    }

    public LiveData<UserOutfitsResponse> getUserOutfits() {
        return userOutfits;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getUpdateSuccessMessage() {
        return updateSuccessMessage;
    }
}
