package com.example.outfitchanges.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.outfitchanges.R;
import com.example.outfitchanges.StartActivity;
import com.example.outfitchanges.auth.AuthViewModel;
import com.example.outfitchanges.auth.network.AuthNetworkClient;
import com.example.outfitchanges.auth.model.ProfileResponse;
import com.example.outfitchanges.utils.SharedPrefManager;
import com.google.android.material.card.MaterialCardView;

public class ProfileFragment extends Fragment {
    private SharedPrefManager prefManager;
    private ProfileViewModel profileViewModel;
    private TextView textUsername;
    private TextView textLoginHint;
    private ImageView imageProfileIcon;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        prefManager = new SharedPrefManager(requireContext());
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        
        initViews(rootView);
        setupClickListeners();
        setupObservers();
        updateUI();
        
        // 如果已登录，加载个人资料
        if (prefManager.isLoggedIn()) {
            profileViewModel.loadProfile();
        }
    }
    
    private void setupObservers() {
        // 观察个人资料数据
        profileViewModel.getProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                textUsername.setText(profile.getUsername() != null ? profile.getUsername() : "用户");
                textLoginHint.setText("点击编辑资料");
                // 可以在这里加载头像等
            }
        });
        
        // 观察错误信息
        profileViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
        
        // 观察更新成功消息
        profileViewModel.getUpdateSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews(View view) {
        textUsername = view.findViewById(R.id.text_username);
        textLoginHint = view.findViewById(R.id.text_login_hint);
        imageProfileIcon = view.findViewById(R.id.image_profile_icon);
    }

    private void setupClickListeners() {
        MaterialCardView cardUserInfo = rootView.findViewById(R.id.card_user_info);
        cardUserInfo.setOnClickListener(v -> {
            if (!prefManager.isLoggedIn()) {
                // 跳转到登录页面
                Intent intent = new Intent(getActivity(), StartActivity.class);
                startActivity(intent);
            } else {
                // TODO: 跳转到编辑资料页面
                Toast.makeText(getContext(), "编辑资料功能开发中", Toast.LENGTH_SHORT).show();
            }
        });

        MaterialCardView cardMyFavorites = rootView.findViewById(R.id.card_my_favorites);
        cardMyFavorites.setOnClickListener(v -> {
            if (!prefManager.isLoggedIn()) {
                Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), StartActivity.class);
                startActivity(intent);
            } else {
                // 加载收藏列表并跳转
                profileViewModel.loadFavorites();
                Intent intent = new Intent(getActivity(), MyCollectionActivity.class);
                startActivity(intent);
            }
        });

        MaterialCardView cardMyPosts = rootView.findViewById(R.id.card_my_posts);
        cardMyPosts.setOnClickListener(v -> {
            if (!prefManager.isLoggedIn()) {
                Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), StartActivity.class);
                startActivity(intent);
            } else {
                // 加载我的穿搭并跳转
                profileViewModel.loadUserOutfits();
                Intent intent = new Intent(getActivity(), MyPostsActivity.class);
                startActivity(intent);
            }
        });

        MaterialCardView cardPreferences = rootView.findViewById(R.id.card_preferences);
        cardPreferences.setOnClickListener(v -> {
            if (!prefManager.isLoggedIn()) {
                Toast.makeText(getContext(), "请先登录", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), StartActivity.class);
                startActivity(intent);
            } else {
                PreferencesDialog dialog = new PreferencesDialog(getContext(), prefManager);
                dialog.setProfileViewModel(profileViewModel, getViewLifecycleOwner());
                dialog.setOnPreferencesSavedListener(() -> {
                    // 刷新个人资料
                    profileViewModel.loadProfile();
                });
                dialog.show();
            }
        });

        Button btnLogout = rootView.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> {
            logout();
        });
    }

    private void updateUI() {
        boolean isLoggedIn = prefManager.isLoggedIn();
        if (isLoggedIn) {
            String username = prefManager.getUsername();
            textUsername.setText(username.isEmpty() ? "用户" : username);
            textLoginHint.setText("点击编辑资料");
        } else {
            textUsername.setText("未登录");
            textLoginHint.setText("点击登录/编辑资料");
        }
    }

    private void logout() {
        // 使用 TokenManager 统一清除所有 NetworkClient 的 token
        com.example.outfitchanges.utils.TokenManager.getInstance(requireContext()).clearToken();
        // 清除本地存储
        prefManager.clear();
        // 清除 ViewModel 的登录状态（如果有）
        AuthViewModel authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        authViewModel.logout();
        
        Toast.makeText(getContext(), "已退出登录", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), StartActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }
}
