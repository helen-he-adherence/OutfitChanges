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
        
        // 如果已登录且不是游客，加载个人资料
        if (prefManager.isLoggedIn() && !prefManager.isGuestMode()) {
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
                
                // 应用用户性别筛选（优先应用，因为这是基础筛选）
                if (profile.getGender() != null && !profile.getGender().isEmpty()) {
                    applyGenderToHome(profile.getGender());
                }
                
                // 检查是否有个人喜好设置，如果有，应用到穿搭广场
                // 注意：个人偏好会在性别筛选之后应用，这样可以组合筛选
                if (profile.getPreferences() != null) {
                    applyPreferencesToHome(profile.getPreferences());
                }
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
    
    /**
     * 将个人喜好应用到穿搭广场
     */
    private void applyPreferencesToHome(com.example.outfitchanges.auth.model.ProfileResponse.Preferences preferences) {
        if (preferences == null) {
            return;
        }
        
        // 只有正常登录用户才能应用个人喜好
        if (prefManager.isGuestMode() || !prefManager.isLoggedIn()) {
            return;
        }
        
        // 检查是否有任何偏好设置
        boolean hasPreferences = false;
        if (preferences.getPreferredStyles() != null && preferences.getPreferredStyles().length > 0) {
            hasPreferences = true;
        }
        if (preferences.getPreferredColors() != null && preferences.getPreferredColors().length > 0) {
            hasPreferences = true;
        }
        if (preferences.getPreferredSeasons() != null && preferences.getPreferredSeasons().length > 0) {
            hasPreferences = true;
        }
        
        if (hasPreferences && getActivity() != null) {
            // 获取HomeViewModel并应用个人喜好
            androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory factory = 
                new androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication());
            com.example.outfitchanges.ui.home.HomeViewModel homeViewModel = 
                new androidx.lifecycle.ViewModelProvider(getActivity(), factory)
                    .get(com.example.outfitchanges.ui.home.HomeViewModel.class);
            homeViewModel.applyUserPreferences(preferences);
        }
    }
    
    /**
     * 将用户性别应用到穿搭广场筛选
     */
    private void applyGenderToHome(String gender) {
        if (gender == null || gender.isEmpty()) {
            return;
        }
        
        // 只有正常登录用户才能应用性别筛选
        if (prefManager.isGuestMode() || !prefManager.isLoggedIn()) {
            return;
        }
        
        if (getActivity() != null) {
            // 获取HomeViewModel并应用性别筛选
            androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory factory = 
                new androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication());
            com.example.outfitchanges.ui.home.HomeViewModel homeViewModel = 
                new androidx.lifecycle.ViewModelProvider(getActivity(), factory)
                    .get(com.example.outfitchanges.ui.home.HomeViewModel.class);
            homeViewModel.applyUserGender(gender);
        }
    }
    
    /**
     * 重新加载穿搭数据（当个人偏好更新后调用）
     */
    public void reloadHomeData() {
        if (getActivity() != null) {
            androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory factory = 
                new androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication());
            com.example.outfitchanges.ui.home.HomeViewModel homeViewModel = 
                new androidx.lifecycle.ViewModelProvider(getActivity(), factory)
                    .get(com.example.outfitchanges.ui.home.HomeViewModel.class);
            // 重新加载数据，会应用当前的筛选条件（包括性别和个人偏好）
            homeViewModel.loadData();
        }
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
            if (prefManager.isGuestMode() || !prefManager.isLoggedIn()) {
                Toast.makeText(getContext(), "请先登录，才能查看我的收藏", Toast.LENGTH_SHORT).show();
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
            if (prefManager.isGuestMode() || !prefManager.isLoggedIn()) {
                Toast.makeText(getContext(), "请先登录，才能查看我的穿搭", Toast.LENGTH_SHORT).show();
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
            if (prefManager.isGuestMode() || !prefManager.isLoggedIn()) {
                Toast.makeText(getContext(), "请先登录，才能设置个人偏好", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), StartActivity.class);
                startActivity(intent);
            } else {
                PreferencesDialog dialog = new PreferencesDialog(getContext(), prefManager);
                dialog.setProfileViewModel(profileViewModel, getViewLifecycleOwner());
                dialog.setOnPreferencesSavedListener(() -> {
                    // 刷新个人资料（会自动应用新的偏好和性别筛选）
                    profileViewModel.loadProfile();
                    // 重新加载穿搭数据，应用新的筛选条件
                    reloadHomeData();
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
        boolean isGuest = prefManager.isGuestMode();
        
        if (isGuest || !isLoggedIn) {
            // 游客或未登录，显示"请先登录"
            textUsername.setText("请先登录");
            textLoginHint.setText("点击登录/编辑资料");
            
            // 禁用功能卡片
            MaterialCardView cardMyFavorites = rootView.findViewById(R.id.card_my_favorites);
            MaterialCardView cardMyPosts = rootView.findViewById(R.id.card_my_posts);
            MaterialCardView cardPreferences = rootView.findViewById(R.id.card_preferences);
            
            if (cardMyFavorites != null) {
                cardMyFavorites.setAlpha(0.5f);
                cardMyFavorites.setClickable(false);
            }
            if (cardMyPosts != null) {
                cardMyPosts.setAlpha(0.5f);
                cardMyPosts.setClickable(false);
            }
            if (cardPreferences != null) {
                cardPreferences.setAlpha(0.5f);
                cardPreferences.setClickable(false);
            }
            
            // 隐藏退出登录按钮
            Button btnLogout = rootView.findViewById(R.id.btn_logout);
            if (btnLogout != null) {
                btnLogout.setVisibility(View.GONE);
            }
        } else {
            // 正常登录
            String username = prefManager.getUsername();
            textUsername.setText(username.isEmpty() ? "用户" : username);
            textLoginHint.setText("点击编辑资料");
            
            // 启用功能卡片
            MaterialCardView cardMyFavorites = rootView.findViewById(R.id.card_my_favorites);
            MaterialCardView cardMyPosts = rootView.findViewById(R.id.card_my_posts);
            MaterialCardView cardPreferences = rootView.findViewById(R.id.card_preferences);
            
            if (cardMyFavorites != null) {
                cardMyFavorites.setAlpha(1.0f);
                cardMyFavorites.setClickable(true);
            }
            if (cardMyPosts != null) {
                cardMyPosts.setAlpha(1.0f);
                cardMyPosts.setClickable(true);
            }
            if (cardPreferences != null) {
                cardPreferences.setAlpha(1.0f);
                cardPreferences.setClickable(true);
            }
            
            // 显示退出登录按钮
            Button btnLogout = rootView.findViewById(R.id.btn_logout);
            if (btnLogout != null) {
                btnLogout.setVisibility(View.VISIBLE);
            }
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
