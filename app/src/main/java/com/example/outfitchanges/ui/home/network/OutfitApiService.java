package com.example.outfitchanges.ui.home.network;

import com.example.outfitchanges.ui.home.model.OutfitItem;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface OutfitApiService {
    @GET("labeled_results.json")
    Call<List<OutfitItem>> getOutfits();
}

