package com.example.outfitchanges.ui.profile;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.example.outfitchanges.R;
import com.example.outfitchanges.auth.model.UserOutfitsResponse;
import com.example.outfitchanges.ui.home.adapter.HomeAdapter;
import com.example.outfitchanges.ui.home.model.OutfitDisplayModel;
import com.example.outfitchanges.ui.home.model.OutfitItem;
import com.example.outfitchanges.ui.home.model.OutfitTags;
import com.example.outfitchanges.utils.SharedPrefManager;
import java.util.ArrayList;
import java.util.List;

public class MyPostsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private HomeAdapter adapter;
    private ProfileViewModel viewModel;
    private SharedPrefManager prefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

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
        loadMyPosts();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        adapter = new HomeAdapter();
        // 设置收藏点击监听器，在我的发布中可以收藏/取消收藏
        adapter.setOnFavoriteClickListener((outfitId, isCurrentlyFavorited) -> {
            // 使用HomeViewModel来切换收藏状态
            com.example.outfitchanges.ui.home.HomeViewModel homeViewModel = 
                new ViewModelProvider(this).get(com.example.outfitchanges.ui.home.HomeViewModel.class);
            homeViewModel.toggleFavoriteOutfit(outfitId, isCurrentlyFavorited);
            // 重新加载我的发布列表以更新收藏状态
            viewModel.loadUserOutfits();
        });
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getUserOutfits().observe(this, outfitsResponse -> {
            if (outfitsResponse != null && outfitsResponse.isSuccess()) {
                List<OutfitDisplayModel> displayModels = new ArrayList<>();
                if (outfitsResponse.getOutfits() != null) {
                    int index = 0;
                    for (OutfitItem item : outfitsResponse.getOutfits()) {
                        displayModels.add(toDisplayModel(item, index));
                        index++;
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

    private void loadMyPosts() {
        viewModel.loadUserOutfits();
    }

    private OutfitDisplayModel toDisplayModel(OutfitItem item, int index) {
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

        String owner = "我的穿搭";
        int likes = item.getLikes() != null ? item.getLikes() : 0;

        OutfitDisplayModel model = new OutfitDisplayModel(item.getImageUrl(), mergedTags, owner, likes);
        if (item.getId() != null) {
            model.setOutfitId(item.getId());
        }
        if (item.getIsFavorited() != null) {
            model.setFavorite(item.getIsFavorited());
        }
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

