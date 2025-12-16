package com.example.outfitchanges.ui.profile;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.example.outfitchanges.R;
import com.example.outfitchanges.ui.home.adapter.HomeAdapter;
import com.example.outfitchanges.ui.home.HomeViewModel;
import androidx.lifecycle.ViewModelProvider;
import java.util.ArrayList;

public class MyCollectionActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private HomeAdapter adapter;
    private HomeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_collection);

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        setupRecyclerView();
        loadFavoriteOutfits();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        adapter = new HomeAdapter();
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void loadFavoriteOutfits() {
        // TODO: 从服务器加载收藏的穿搭
        // 暂时使用空列表
        adapter.setData(new ArrayList<>());
    }
}

