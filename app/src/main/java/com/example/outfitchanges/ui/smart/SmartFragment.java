package com.example.outfitchanges.ui.smart;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.outfitchanges.R;
import com.example.outfitchanges.StartActivity;
import com.example.outfitchanges.ui.virtual.FreeTryOnFragment;
import com.example.outfitchanges.ui.virtual.VirtualTryOnFragment;
import com.example.outfitchanges.utils.SharedPrefManager;
import com.google.android.material.tabs.TabLayout;

public class SmartFragment extends Fragment {
    private TabLayout tabLayout;
    private FreeTryOnFragment freeTryOnFragment;
    private VirtualTryOnFragment virtualTryOnFragment;
    private SharedPrefManager prefManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_smart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        prefManager = new SharedPrefManager(requireContext());
        
        // 检查登录状态，游客不能使用
        if (prefManager.isGuestMode() || !prefManager.isLoggedIn()) {
            Toast.makeText(getContext(), "请先登录，才能使用智能换装", Toast.LENGTH_SHORT).show();
            // 可以显示一个提示页面，或者直接跳转到登录页面
            // 这里我们显示默认页面，但功能不可用
            return;
        }
        
        tabLayout = view.findViewById(R.id.tab_layout);
        
        // 创建两个Fragment
        freeTryOnFragment = new FreeTryOnFragment();
        virtualTryOnFragment = new VirtualTryOnFragment();
        
        // 设置Tab
        tabLayout.addTab(tabLayout.newTab().setText("自由换装"));
        tabLayout.addTab(tabLayout.newTab().setText("虚拟换装"));
        
        // 默认显示第一个Fragment
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, freeTryOnFragment)
                .commit();
        
        // Tab切换监听
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selectedFragment = null;
                if (tab.getPosition() == 0) {
                    selectedFragment = freeTryOnFragment;
                } else if (tab.getPosition() == 1) {
                    selectedFragment = virtualTryOnFragment;
                }
                
                if (selectedFragment != null) {
                    getChildFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 每次显示时检查登录状态
        if (prefManager.isGuestMode() || !prefManager.isLoggedIn()) {
            Toast.makeText(getContext(), "请先登录，才能使用智能换装", Toast.LENGTH_SHORT).show();
        }
    }
}