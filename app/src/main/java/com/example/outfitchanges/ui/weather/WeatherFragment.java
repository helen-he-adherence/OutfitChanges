package com.example.outfitchanges.ui.weather;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.example.outfitchanges.R;
import com.example.outfitchanges.ui.weather.model.WeatherResponse;
import com.example.outfitchanges.ui.weather.repository.WeatherRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WeatherFragment extends Fragment {
    private WeatherViewModel viewModel;
    private TextView tvLocation;
    private TextView tvCurrentTemp;
    private TextView tvCurrentWeather;
    private TextView tvDressingAdvice;
    private TextView btnChangeCity;
    private LinearLayout hourlyContainer;
    private LinearLayout dailyContainer;
    private View overlayView;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private static final String CITY_DATA_URL = "https://raw.githubusercontent.com/modood/Administrative-divisions-of-China/master/dist/pcas-code.json";
    private static final String BACKUP_CITY_DATA_URL = "https://raw.githubusercontent.com/airyland/china-area-data/master/data.json";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        // 初始化ViewModel
        if (getActivity() != null && getActivity().getApplication() != null) {
            viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication())).get(WeatherViewModel.class);
        } else {
            // 如果无法获取Application，延迟初始化
            return view;
        }

        tvLocation = view.findViewById(R.id.tv_location);
        tvCurrentTemp = view.findViewById(R.id.tv_current_temp);
        tvCurrentWeather = view.findViewById(R.id.tv_current_weather);
        tvDressingAdvice = view.findViewById(R.id.tv_dressing_advice);
        btnChangeCity = view.findViewById(R.id.btn_change_city);
        hourlyContainer = view.findViewById(R.id.hourly_container);
        dailyContainer = view.findViewById(R.id.daily_container);

        btnChangeCity.setOnClickListener(v -> showCityPicker());

        // 初始化定位服务
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        if (viewModel != null) {
            observeViewModel();
            checkAndLoadCityData();
            
            // 检查是否已有保存的位置，如果没有则获取当前位置
            String savedLocationId = viewModel.getSavedLocationId();
            if (savedLocationId == null || savedLocationId.isEmpty() || savedLocationId.equals("101010100")) {
                // 默认位置或空位置，尝试获取当前位置
                getCurrentLocation();
            } else {
                // 已有保存的位置，直接加载天气数据
                loadWeatherData();
            }
        }

        return view;
    }

    private void observeViewModel() {
        if (viewModel == null || getViewLifecycleOwner() == null) {
            return;
        }

        viewModel.getCurrentWeather().observe(getViewLifecycleOwner(), nowWeather -> {
            if (nowWeather != null) {
                if (tvCurrentTemp != null) {
                    tvCurrentTemp.setText(nowWeather.getTemp() + "°C");
                }
                if (tvCurrentWeather != null) {
                    tvCurrentWeather.setText(nowWeather.getText());
                }
                updateDressingAdvice();
            }
        });

        viewModel.getHourlyWeather().observe(getViewLifecycleOwner(), hourlyWeathers -> {
            if (hourlyWeathers != null) {
                displayHourlyWeather(hourlyWeathers);
            }
        });

        viewModel.getDailyWeather().observe(getViewLifecycleOwner(), dailyWeathers -> {
            if (dailyWeathers != null) {
                displayDailyWeather(dailyWeathers);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && getContext() != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // 更新位置显示
        if (tvLocation != null) {
            tvLocation.setText(viewModel.getSavedDistrict());
        }
    }

    private void checkAndLoadCityData() {
        if (viewModel != null) {
            viewModel.getCityCount(count -> {
                if (count == 0) {
                    loadCityDataFromUrl(CITY_DATA_URL);
                }
            });
        }
    }

    private void loadCityDataFromUrl(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (url.equals(CITY_DATA_URL)) {
                    // 如果主URL失败，尝试备用URL
                    loadCityDataFromUrl(BACKUP_CITY_DATA_URL);
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(getContext(), "加载城市数据失败，请检查网络连接", Toast.LENGTH_LONG).show();
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    viewModel.loadCitiesFromJson(jsonData, new WeatherRepository.LoadCitiesCallback() {
                        @Override
                        public void onSuccess() {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                viewModel.getCityCount(count -> {
                                    Toast.makeText(getContext(), "城市数据加载完成，共加载 " + count + " 条数据", Toast.LENGTH_SHORT).show();
                                });
                            });
                        }

                        @Override
                        public void onError(String error) {
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                            });
                        }
                    });
                } else {
                    if (url.equals(CITY_DATA_URL)) {
                        loadCityDataFromUrl(BACKUP_CITY_DATA_URL);
                    }
                }
            }
        });
    }

    private void loadWeatherData() {
        if (viewModel != null) {
            String locationId = viewModel.getSavedLocationId();
            if (locationId != null && !locationId.isEmpty() && !locationId.equals("101010100")) {
                // 如果不是默认位置，加载天气数据
                viewModel.loadWeatherData(locationId);
                // updateDressingAdvice会在数据加载完成后通过observeViewModel自动调用
            } else if (locationId != null && locationId.equals("101010100")) {
                // 如果是默认位置，也加载数据
                viewModel.loadWeatherData(locationId);
            } else {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "位置信息无效，请重新选择城市", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void getCurrentLocation() {
        // 检查权限
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            // 请求权限
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, 
                    Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        // 获取当前位置
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // 使用经纬度查找locationId
                            String lat = String.valueOf(location.getLatitude());
                            String lon = String.valueOf(location.getLongitude());
                            viewModel.lookupLocationByCoordinates(lat, lon, new WeatherRepository.LocationCallback() {
                                @Override
                                public void onSuccess(String locationId) {
                                    // 保存位置并加载天气
                                    // 暂时不保存省市区信息，因为通过坐标查找可能没有这些信息
                                    viewModel.saveLocation(locationId, "", "", "");
                                    // 更新位置显示
                                    if (tvLocation != null) {
                                        tvLocation.setText("当前位置");
                                    }
                                    loadWeatherData();
                                }

                                @Override
                                public void onError(String error) {
                                    // 如果定位失败，使用默认位置（北京）
                                    if (getContext() != null) {
                                        Toast.makeText(getContext(), "定位失败，使用默认位置（北京）", Toast.LENGTH_SHORT).show();
                                    }
                                    viewModel.saveLocation("101010100", "北京", "北京", "东城区");
                                    if (tvLocation != null) {
                                        tvLocation.setText("东城区");
                                    }
                                    loadWeatherData();
                                }
                            });
                        } else {
                            // 如果无法获取位置，使用默认位置
                            Toast.makeText(getContext(), "无法获取当前位置，使用默认位置", Toast.LENGTH_SHORT).show();
                            viewModel.saveLocation("101010100", "北京", "北京", "东城区");
                            loadWeatherData();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限授予，获取位置
                getCurrentLocation();
            } else {
                // 权限被拒绝，使用默认位置
                Toast.makeText(getContext(), "定位权限被拒绝，使用默认位置", Toast.LENGTH_SHORT).show();
                if (viewModel != null) {
                    viewModel.saveLocation("101010100", "北京", "北京", "东城区");
                    loadWeatherData();
                }
            }
        }
    }

    private void showCityPicker() {
        if (viewModel == null) return;
        
        // 确保数据已加载
        viewModel.getCityCount(count -> {
            if (count == 0) {
                // 如果数据未加载，先触发加载
                Toast.makeText(getContext(), "正在加载城市数据，请稍候...", Toast.LENGTH_LONG).show();
                checkAndLoadCityData();
                // 延迟显示对话框，等待数据加载
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    showCityPickerDialog();
                }, 2000);
            } else {
                showCityPickerDialog();
            }
        });
    }
    
    private void showCityPickerDialog() {
        CityPickerBottomSheetDialog dialog = new CityPickerBottomSheetDialog();
        dialog.setOnCitySelectedListener((province, city, district) -> {
            // 显示加载提示
            if (getContext() != null) {
                Toast.makeText(getContext(), "正在切换到 " + district + "...", Toast.LENGTH_SHORT).show();
            }
            
            viewModel.lookupLocationId(province, city, district, new WeatherRepository.LocationCallback() {
                @Override
                public void onSuccess(String locationId) {
                    // 保存选择的位置
                    viewModel.saveLocation(locationId, province, city, district);
                    
                    // 更新UI显示
                    if (tvLocation != null) {
                        tvLocation.setText(district);
                    }
                    
                    // 加载新城市的天气数据
                    // 使用新的locationId重新加载
                    if (locationId != null && !locationId.isEmpty()) {
                        // 直接调用loadWeatherData，它会更新所有天气数据
                        viewModel.loadWeatherData(locationId);
                        // updateDressingAdvice会在数据加载完成后通过observeViewModel自动调用
                    } else {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "位置ID无效，无法加载天气数据", Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    // 显示成功提示
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "已切换到 " + district + "，正在加载天气数据...", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {
                    if (getContext() != null) {
                        new Handler(Looper.getMainLooper()).post(() ->
                                Toast.makeText(getContext(), "切换城市失败: " + error, Toast.LENGTH_LONG).show()
                        );
                    }
                }
            });
        });
        dialog.show(getParentFragmentManager(), "CityPicker");
    }

    private void displayHourlyWeather(WeatherResponse.HourlyWeather[] hourlyWeathers) {
        if (hourlyContainer == null || hourlyWeathers == null || getContext() == null) {
            return;
        }
        hourlyContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (int i = 0; i < Math.min(24, hourlyWeathers.length); i++) {
            WeatherResponse.HourlyWeather weather = hourlyWeathers[i];
            View itemView = inflater.inflate(R.layout.item_hourly_weather, hourlyContainer, false);

            TextView tvTime = itemView.findViewById(R.id.tv_hourly_time);
            ImageView ivIcon = itemView.findViewById(R.id.iv_hourly_icon);
            TextView tvTemp = itemView.findViewById(R.id.tv_hourly_temp);

            // 格式化时间
            String timeStr = formatHourlyTime(weather.getFxTime());
            if (i == 0) {
                timeStr = "现在";
            }
            tvTime.setText(timeStr);
            tvTemp.setText(weather.getTemp() + "°C");

            // 加载天气图标
            loadWeatherIcon(ivIcon, weather.getIcon());

            hourlyContainer.addView(itemView);
        }
    }

    private void displayDailyWeather(WeatherResponse.DailyWeather[] dailyWeathers) {
        if (dailyContainer == null || dailyWeathers == null || getContext() == null) {
            return;
        }
        dailyContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (int i = 0; i < Math.min(7, dailyWeathers.length); i++) {
            WeatherResponse.DailyWeather weather = dailyWeathers[i];
            View itemView = inflater.inflate(R.layout.item_daily_weather, dailyContainer, false);

            TextView tvDate = itemView.findViewById(R.id.tv_daily_date);
            ImageView ivIcon = itemView.findViewById(R.id.iv_daily_icon);
            TextView tvTempMax = itemView.findViewById(R.id.tv_daily_temp_max);
            TextView tvTempMin = itemView.findViewById(R.id.tv_daily_temp_min);

            // 格式化日期
            String dateStr = formatDailyDate(weather.getFxDate(), i);
            tvDate.setText(dateStr);
            tvTempMax.setText(weather.getTempMax() + "°C");
            tvTempMin.setText(weather.getTempMin() + "°C");

            // 加载天气图标
            loadWeatherIcon(ivIcon, weather.getIconDay());

            dailyContainer.addView(itemView);
        }
    }

    private String formatHourlyTime(String fxTime) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm+08:00", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(fxTime);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fxTime;
    }

    private String formatDailyDate(String fxDate, int index) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = inputFormat.parse(fxDate);
            if (date != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);

                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                if (index == 0) {
                    return "今天";
                } else if (calendar.equals(today)) {
                    return "今天";
                } else {
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    String[] weekDays = {"", "周日", "周一", "周二", "周三", "周四", "周五", "周六"};
                    return weekDays[dayOfWeek];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fxDate;
    }

    private void loadWeatherIcon(ImageView imageView, String iconCode) {
        if (imageView == null || iconCode == null || getContext() == null) {
            return;
        }
        String iconUrl = "https://cdn.heweather.com/cond_icon/" + iconCode + ".png";
        try {
            Glide.with(this)
                    .load(iconUrl)
                    .placeholder(R.drawable.ic_weather)
                    .error(R.drawable.ic_weather)
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDressingAdvice() {
        if (viewModel != null && viewModel.getCurrentWeather().getValue() != null) {
            WeatherResponse.NowWeather nowWeather = viewModel.getCurrentWeather().getValue();
            if (nowWeather != null) {
                String advice = generateDressingAdvice(nowWeather);
                tvDressingAdvice.setText(advice);
            } else {
                tvDressingAdvice.setText("根据当前天气，建议穿着轻薄外套，注意保暖。");
            }
        } else {
            tvDressingAdvice.setText("根据当前天气，建议穿着轻薄外套，注意保暖。");
        }
    }

    private String generateDressingAdvice(WeatherResponse.NowWeather weather) {
        try {
            int temp = Integer.parseInt(weather.getTemp());
            String text = weather.getText();

            StringBuilder advice = new StringBuilder();

            if (temp >= 30) {
                advice.append("天气炎热，建议穿着短袖、短裤、裙子等轻薄透气的衣物，注意防晒。");
            } else if (temp >= 25) {
                advice.append("天气较热，建议穿着短袖、薄长裤或短裤，可以选择轻薄透气的材质。");
            } else if (temp >= 20) {
                advice.append("天气舒适，建议穿着长袖T恤、薄外套或薄长裤，早晚可适当添加衣物。");
            } else if (temp >= 15) {
                advice.append("天气较凉，建议穿着长袖、薄外套或薄毛衣，注意保暖。");
            } else if (temp >= 10) {
                advice.append("天气较冷，建议穿着厚外套、毛衣或薄羽绒服，注意保暖。");
            } else if (temp >= 5) {
                advice.append("天气寒冷，建议穿着厚外套、厚毛衣或羽绒服，注意防寒保暖。");
            } else {
                advice.append("天气非常寒冷，建议穿着厚羽绒服、厚毛衣，注意防寒保暖，避免长时间户外活动。");
            }

            if (text.contains("雨")) {
                advice.append(" 今天有雨，记得携带雨具。");
            } else if (text.contains("雪")) {
                advice.append(" 今天有雪，注意防滑，建议穿着防滑鞋。");
            } else if (text.contains("风")) {
                advice.append(" 今天有风，建议穿着防风外套。");
            }

            return advice.toString();
        } catch (Exception e) {
            return "根据当前天气，建议穿着轻薄外套，注意保暖。";
        }
    }
}
