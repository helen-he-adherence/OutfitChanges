package com.example.outfitchanges.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.outfitchanges.R;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private com.example.outfitchanges.ui.home.adapter.HomeAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setupRecyclerView();

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 模拟数据
        List<String> dummyData = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            dummyData.add("穿搭分享 #" + i);
        }

        adapter = new com.example.outfitchanges.ui.home.adapter.HomeAdapter(dummyData);
        recyclerView.setAdapter(adapter);
    }
}