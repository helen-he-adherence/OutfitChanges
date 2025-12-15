package com.example.outfitchanges.ui.home.data;

import com.example.outfitchanges.ui.home.model.OutfitItem;
import com.example.outfitchanges.ui.home.network.OutfitApiService;
import com.example.outfitchanges.ui.home.network.OutfitNetworkClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutfitRepository {

    public interface LoadCallback {
        void onSuccess(List<OutfitItem> items);

        void onError(String message);
    }

    private final OutfitApiService apiService;

    public OutfitRepository() {
        apiService = OutfitNetworkClient.getRetrofit().create(OutfitApiService.class);
    }

    public void loadOutfits(LoadCallback callback) {
        apiService.getOutfits().enqueue(new Callback<List<OutfitItem>>() {
            @Override
            public void onResponse(Call<List<OutfitItem>> call, Response<List<OutfitItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("数据加载失败");
                }
            }

            @Override
            public void onFailure(Call<List<OutfitItem>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}

