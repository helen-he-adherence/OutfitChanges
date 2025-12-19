package com.example.outfitchanges;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.outfitchanges.auth.network.AuthNetworkClient;
import com.example.outfitchanges.ui.home.HomeFragment;
import com.example.outfitchanges.ui.home.HomeViewModel;
import com.example.outfitchanges.ui.profile.ProfileFragment;
import com.example.outfitchanges.ui.profile.ProfileViewModel;
import com.example.outfitchanges.ui.publish.PublishFragment;
import com.example.outfitchanges.ui.smart.SmartFragment;
import com.example.outfitchanges.ui.weather.WeatherFragment;
import com.example.outfitchanges.ui.weather.WeatherViewModel;
import com.example.outfitchanges.utils.SharedPrefManager;

public class MainActivity extends AppCompatActivity {

    private TextView toolbarTitle;
    private BottomNavigationView bottomNavigationView;
    private LinearLayout toolbarMenuContainer;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 从 SharedPreferences 恢复 token 到所有 NetworkClient
        com.example.outfitchanges.utils.TokenManager.getInstance(this).restoreToken();

        toolbarTitle = findViewById(R.id.toolbar_title);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        toolbarMenuContainer = findViewById(R.id.toolbar_menu_container);

        // 提前初始化 HomeViewModel 并开始加载数据
        // 这样当用户进入穿搭广场时，数据可能已经加载好了
        preloadHomeData();

        setupBottomNavigation();
    }

    /**
     * 提前加载数据
     * 在应用启动时就开始加载，这样用户进入页面时就能立即看到内容
     */
    private void preloadHomeData() {
        SharedPrefManager prefManager = new SharedPrefManager(this);
        ViewModelProvider.AndroidViewModelFactory factory = 
            new ViewModelProvider.AndroidViewModelFactory(getApplication());
        
        // 预加载天气数据（游客和正常登录都需要）
        WeatherViewModel weatherViewModel = new ViewModelProvider(this, factory).get(WeatherViewModel.class);
        HomeViewModel homeViewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
        
        // 观察天气数据，自动应用天气筛选到穿搭广场
        weatherViewModel.getCurrentWeather().observe(this, nowWeather -> {
            if (nowWeather != null) {
                homeViewModel.applyWeatherFilter(nowWeather);
            }
        });
        
        String savedLocationId = weatherViewModel.getSavedLocationId();
        if (savedLocationId != null && !savedLocationId.isEmpty()) {
            weatherViewModel.loadWeatherData(savedLocationId);
        }
        
        // 如果是正常登录，预加载个人资料和个人偏好
        if (prefManager.isLoggedIn() && !prefManager.isGuestMode()) {
            // 预加载个人资料（会自动应用个人偏好到穿搭广场）
            ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
            profileViewModel.loadProfile();
        }
        
        // 开始加载穿搭广场数据（游客使用/api/outfits，正常登录根据个人偏好使用/api/discover/outfits）
        // 注意：天气筛选会在天气数据加载后自动应用
        homeViewModel.loadData();
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String title = "";

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                title = "穿搭广场";
            } else if (itemId == R.id.nav_weather) {
                selectedFragment = new WeatherFragment();
                title = "天气";
            } else if (itemId == R.id.nav_publish) {
                selectedFragment = new PublishFragment();
                title = "发布";
            } else if (itemId == R.id.nav_smart) {
                selectedFragment = new SmartFragment();
                title = "智能换装";
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                title = "我的";
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment, title);
                return true;
            }
            return false;
        });

        // 默认选中首页
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    private void loadFragment(Fragment fragment, String title) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
        toolbarTitle.setText(title);
        currentFragment = fragment;
        
        // 清空菜单容器
        toolbarMenuContainer.removeAllViews();
    }
}