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
            // 预加载个人资料（会自动应用个人偏好和性别筛选到穿搭广场）
            ProfileViewModel profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
            
            // 使用一个标志来确保只加载一次数据
            final boolean[] dataLoaded = {false};
            
            // 观察个人资料加载完成，然后应用筛选并加载穿搭数据
            profileViewModel.getProfile().observe(this, profile -> {
                if (profile != null && !dataLoaded[0]) {
                    dataLoaded[0] = true;
                    
                    // 同时应用用户性别筛选和个人偏好筛选（无先后顺序）
                    boolean hasGender = profile.getGender() != null && !profile.getGender().isEmpty();
                    boolean hasPreferences = profile.getPreferences() != null && 
                        ((profile.getPreferences().getPreferredStyles() != null && profile.getPreferences().getPreferredStyles().length > 0) ||
                         (profile.getPreferences().getPreferredColors() != null && profile.getPreferences().getPreferredColors().length > 0) ||
                         (profile.getPreferences().getPreferredSeasons() != null && profile.getPreferences().getPreferredSeasons().length > 0));
                    
                    if (hasGender || hasPreferences) {
                        // 有筛选条件，同时应用性别筛选和个人偏好筛选（无先后顺序）
                        // 先应用性别筛选（不重新加载）
                        if (hasGender) {
                            homeViewModel.applyUserGender(profile.getGender(), true);
                        }
                        // 再应用个人偏好筛选（会保留性别筛选，并触发加载）
                        if (hasPreferences) {
                            homeViewModel.applyUserPreferences(profile.getPreferences());
                        } else if (hasGender) {
                            // 如果只有性别筛选，没有个人偏好，需要手动触发加载
                            homeViewModel.applyUserGender(profile.getGender(), false);
                        }
                    } else {
                        // 既没有性别也没有偏好，直接加载默认数据
                        homeViewModel.loadData();
                    }
                }
            });
            
            // 观察错误，如果加载失败，直接加载数据
            profileViewModel.getErrorMessage().observe(this, error -> {
                if (error != null && !error.isEmpty() && !dataLoaded[0]) {
                    dataLoaded[0] = true;
                    // 个人资料加载失败，直接加载默认数据
                    homeViewModel.loadData();
                }
            });
            
            // 开始加载个人资料
            profileViewModel.loadProfile();
        } else {
            // 游客模式，直接加载穿搭数据
            homeViewModel.loadData();
        }
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