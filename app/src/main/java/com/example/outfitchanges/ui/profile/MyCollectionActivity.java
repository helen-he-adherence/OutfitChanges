package com.example.outfitchanges.ui.profile;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.example.outfitchanges.R;
import com.example.outfitchanges.auth.model.FavoritesResponse;
import com.example.outfitchanges.ui.home.adapter.HomeAdapter;
import com.example.outfitchanges.ui.home.model.OutfitDisplayModel;
import com.example.outfitchanges.ui.home.model.OutfitTags;
import com.example.outfitchanges.utils.SharedPrefManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyCollectionActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private HomeAdapter adapter;
    private ProfileViewModel viewModel;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_collection);

        prefManager = new SharedPrefManager(this);
        
        // 检查是否登录
        if (!prefManager.isLoggedIn()) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        
        setupRecyclerView();
        setupObservers();
        loadFavoriteOutfits();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        adapter = new HomeAdapter();
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getFavorites().observe(this, favoritesResponse -> {
            if (favoritesResponse != null && favoritesResponse.isSuccess()) {
                List<OutfitDisplayModel> displayModels = new ArrayList<>();
                if (favoritesResponse.getFavorites() != null) {
                    for (FavoritesResponse.FavoriteItem item : favoritesResponse.getFavorites()) {
                        displayModels.add(toDisplayModel(item));
                    }
                }
                adapter.setData(displayModels);
            }
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFavoriteOutfits() {
        viewModel.loadFavorites();
    }

    private OutfitDisplayModel toDisplayModel(FavoritesResponse.FavoriteItem item) {
        OutfitTags tags = item.getTags();
        List<String> mergedTags = new ArrayList<>();

        if (tags != null) {
            addLimited(mergedTags, tags.getSeason(), 1);
            addLimited(mergedTags, tags.getOverallStyle(), 2);
            addLimited(mergedTags, tags.getOccasion(), 1);
            addLimited(mergedTags, tags.getAllCategories(), 2);
            addLimited(mergedTags, tags.getAllColors(), 1);
            addLimited(mergedTags, tags.getWeather(), 1);
        }

        String owner = "用户";
        int likes = item.getLikes();

        OutfitDisplayModel model = new OutfitDisplayModel(item.getImageUrl(), mergedTags, owner, likes);
        model.setFavorite(true); // 收藏列表中的总是true
        return model;
    }

    private void addLimited(List<String> target, List<String> source, int limit) {
        if (source == null || source.isEmpty() || limit <= 0) return;
        int count = 0;
        for (String s : source) {
            if (count >= limit) break;
            target.add(s);
            count++;
        }
    }
}

