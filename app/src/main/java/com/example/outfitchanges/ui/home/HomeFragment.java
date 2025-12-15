package com.example.outfitchanges.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.example.outfitchanges.R;
import com.example.outfitchanges.ui.home.adapter.HomeAdapter;
import com.example.outfitchanges.ui.weather.WeatherViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private HomeAdapter adapter;
    private HomeViewModel viewModel;
    private WeatherViewModel weatherViewModel;
    private FusedLocationProviderClient fusedLocationClient;
    private DrawerLayout drawerLayout;
    private View filterDrawer;
    private EditText inputSearch;
    private final Map<String, Set<String>> localFilters = new HashMap<>();
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2001;

    private final String[] seasons = {"春季", "夏季", "秋季", "冬季"};
    private final String[] weathers = {"晴天", "多云", "下雨", "下雪"};
    private final String[] scenes = {"上班", "约会", "聚餐", "运动", "旅行"};
    private final String[] styles = {"简约", "甜美", "优雅", "街头", "复古"};
    private final String[] categories = {"夹克", "牛仔裤", "连衣裙", "外套", "配饰", "吊带/背心", "西装"};
    private final String[] colors = {"黑色", "白色", "灰色", "咖色", "米色", "粉色", "蓝色"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        weatherViewModel = new ViewModelProvider(requireActivity(),
                new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(WeatherViewModel.class);
        initLocalFilters();
        initViews(view);
        setupRecyclerView();
        setupFilters(view);
        observeData();
        viewModel.loadData();
        observeWeather();
        triggerWeatherLoadIfNeeded();
        tryStartLocate();
    }

    private void initLocalFilters() {
        localFilters.put("season", new HashSet<>());
        localFilters.put("weather", new HashSet<>());
        localFilters.put("scene", new HashSet<>());
        localFilters.put("style", new HashSet<>());
        localFilters.put("category", new HashSet<>());
        localFilters.put("color", new HashSet<>());
    }

    private void initViews(View view) {
        drawerLayout = view.findViewById(R.id.home_drawer);
        filterDrawer = view.findViewById(R.id.filter_drawer);
        recyclerView = view.findViewById(R.id.recycler_view);
        inputSearch = view.findViewById(R.id.input_search);
        MaterialCardView btnFilter = view.findViewById(R.id.btn_filter);
        ImageButton btnCloseFilter = view.findViewById(R.id.btn_close_filter);
        Button btnClearFilters = view.findViewById(R.id.btn_clear_filters);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        btnFilter.setOnClickListener(v -> drawerLayout.openDrawer(Gravity.END));
        btnCloseFilter.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.END));
        btnClearFilters.setOnClickListener(v -> {
            clearAllChipSelections((ChipGroup) view.findViewById(R.id.chip_group_season));
            clearAllChipSelections((ChipGroup) view.findViewById(R.id.chip_group_weather));
            clearAllChipSelections((ChipGroup) view.findViewById(R.id.chip_group_scene));
            clearAllChipSelections((ChipGroup) view.findViewById(R.id.chip_group_style));
            clearAllChipSelections((ChipGroup) view.findViewById(R.id.chip_group_category));
            clearAllChipSelections((ChipGroup) view.findViewById(R.id.chip_group_color));
            resetLocalFilters();
            viewModel.clearFilters();
        });

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.updateKeyword(s.toString());
            }
        });

        adjustDrawerWidth();
    }

    private void setupRecyclerView() {
        adapter = new HomeAdapter();
        adapter.setOnFavoriteClickListener(imageUrl -> viewModel.toggleFavorite(imageUrl));
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void setupFilters(View root) {
        createFilterChips((ChipGroup) root.findViewById(R.id.chip_group_season), "season", seasons);
        createFilterChips((ChipGroup) root.findViewById(R.id.chip_group_weather), "weather", weathers);
        createFilterChips((ChipGroup) root.findViewById(R.id.chip_group_scene), "scene", scenes);
        createFilterChips((ChipGroup) root.findViewById(R.id.chip_group_style), "style", styles);
        createFilterChips((ChipGroup) root.findViewById(R.id.chip_group_category), "category", categories);
        createFilterChips((ChipGroup) root.findViewById(R.id.chip_group_color), "color", colors);
    }

    private void observeData() {
        viewModel.getOutfits().observe(getViewLifecycleOwner(), outfitDisplayModels -> {
            adapter.setData(outfitDisplayModels);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeWeather() {
        weatherViewModel.getCurrentWeather().observe(getViewLifecycleOwner(), now -> {
            viewModel.applyWeatherDefaults(now);
        });
    }

    private void triggerWeatherLoadIfNeeded() {
        String locationId = weatherViewModel.getSavedLocationId();
        if (locationId != null && !locationId.isEmpty()) {
            weatherViewModel.loadWeatherData(locationId);
        }
    }

    private void tryStartLocate() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        handleLocation(location);
                    }
                });
    }

    private void handleLocation(Location location) {
        String lat = String.valueOf(location.getLatitude());
        String lon = String.valueOf(location.getLongitude());
        weatherViewModel.lookupLocationByCoordinates(lat, lon, new com.example.outfitchanges.ui.weather.repository.WeatherRepository.LocationCallback() {
            @Override
            public void onSuccess(String locationId) {
                weatherViewModel.saveLocation(locationId, "", "", "");
                weatherViewModel.loadWeatherData(locationId);
            }

            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "定位失败：" + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tryStartLocate();
            } else {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "未授予定位权限，将无法自动推荐本地穿搭", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void createFilterChips(ChipGroup group, String key, String[] labels) {
        group.removeAllViews();
        for (String label : labels) {
            Chip chip = new Chip(requireContext());
            chip.setText(label);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_bg_color);
            chip.setChipStrokeColorResource(R.color.gray_button_bg);
            chip.setChipStrokeWidth(1);
            chip.setTextColor(requireContext().getColorStateList(R.color.chip_text_color));
            chip.setEnsureMinTouchTargetSize(false);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateLocalFilter(key, label, isChecked);
                viewModel.toggleFilter(key, label);
            });
            group.addView(chip);
        }
    }

    private void updateLocalFilter(String key, String label, boolean checked) {
        Set<String> set = localFilters.get(key);
        if (set == null) return;
        if (checked) {
            set.add(label);
        } else {
            set.remove(label);
        }
    }

    private void clearAllChipSelections(ChipGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setChecked(false);
            }
        }
    }

    private void resetLocalFilters() {
        for (Set<String> set : localFilters.values()) {
            set.clear();
        }
    }

    private void adjustDrawerWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        requireActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = (int) (metrics.widthPixels * 0.66f);
        ViewGroup.LayoutParams params = filterDrawer.getLayoutParams();
        params.width = width;
        filterDrawer.setLayoutParams(params);
    }
}