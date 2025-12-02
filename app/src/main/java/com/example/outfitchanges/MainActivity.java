package com.example.outfitchanges;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.example.outfitchanges.ui.home.HomeFragment;
import com.example.outfitchanges.ui.profile.ProfileFragment;
import com.example.outfitchanges.ui.publish.PublishFragment;
import com.example.outfitchanges.ui.smart.SmartFragment;
import com.example.outfitchanges.ui.weather.WeatherFragment;

public class MainActivity extends AppCompatActivity {

    private TextView toolbarTitle;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbarTitle = findViewById(R.id.toolbar_title);
        bottomNavigationView = findViewById(R.id.bottom_navigation_view);

        setupBottomNavigation();
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
    }
}