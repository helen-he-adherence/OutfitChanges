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
import com.example.outfitchanges.R;
import com.example.outfitchanges.StartActivity;
import com.example.outfitchanges.utils.SharedPrefManager;
import com.google.android.material.card.MaterialCardView;

public class ProfileFragment extends Fragment {
    private SharedPrefManager prefManager;
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
        
        initViews(rootView);
        setupClickListeners();
        updateUI();
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
            Intent intent = new Intent(getActivity(), MyCollectionActivity.class);
            startActivity(intent);
        });

        MaterialCardView cardMyPosts = rootView.findViewById(R.id.card_my_posts);
        cardMyPosts.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MyPostsActivity.class);
            startActivity(intent);
        });

        MaterialCardView cardPreferences = rootView.findViewById(R.id.card_preferences);
        cardPreferences.setOnClickListener(v -> {
            PreferencesDialog dialog = new PreferencesDialog(getContext(), prefManager);
            dialog.setOnPreferencesSavedListener(() -> {
                // 可以在这里刷新UI
            });
            dialog.show();
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
        prefManager.clear();
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
