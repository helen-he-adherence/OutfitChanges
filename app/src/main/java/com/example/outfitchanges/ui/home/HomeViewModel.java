package com.example.outfitchanges.ui.home;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.outfitchanges.ui.home.data.OutfitRepository;
import com.example.outfitchanges.ui.home.model.FavoriteActionResponse;
import com.example.outfitchanges.ui.home.model.OutfitDisplayModel;
import com.example.outfitchanges.ui.home.model.OutfitItem;
import com.example.outfitchanges.ui.home.model.OutfitListResponse;
import com.example.outfitchanges.ui.home.model.OutfitTags;
import com.example.outfitchanges.ui.weather.WeatherViewModel;
import com.example.outfitchanges.ui.weather.model.WeatherResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class HomeViewModel extends AndroidViewModel {

    private final OutfitRepository repository;

    public HomeViewModel(Application application) {
        super(application);
        repository = new OutfitRepository(application);
        
        // 初始化筛选器（天气筛选已移除，改为自动根据温度判断）
        selectedFilters.put("season", new HashSet<>());
        selectedFilters.put("weather", new HashSet<>()); // 保留weather筛选器，但由天气数据自动填充
        selectedFilters.put("scene", new HashSet<>());
        selectedFilters.put("style", new HashSet<>());
        selectedFilters.put("category", new HashSet<>());
        selectedFilters.put("color", new HashSet<>());
        selectedFilters.put("sex", new HashSet<>());
    }

    private final MutableLiveData<List<OutfitDisplayModel>> allOutfits = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<OutfitDisplayModel>> filteredOutfits = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<OutfitListResponse.Pagination> pagination = new MutableLiveData<>();

    private final Map<String, Set<String>> selectedFilters = new HashMap<>();
    private final Set<String> favoriteIds = new HashSet<>();
    private String keyword = "";
    private int currentOffset = 0;
    private static final int DEFAULT_LIMIT = 20;

    public LiveData<List<OutfitDisplayModel>> getOutfits() {
        return filteredOutfits;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<OutfitListResponse.Pagination> getPagination() {
        return pagination;
    }

    /**
     * 加载穿搭广场数据（使用新的API）
     */
    public void loadDiscoverOutfits() {
        loadDiscoverOutfits(0);
    }

    /**
     * 加载更多穿搭广场数据
     */
    public void loadMoreDiscoverOutfits() {
        OutfitListResponse.Pagination pag = pagination.getValue();
        if (pag != null && pag.isHasMore()) {
            loadDiscoverOutfits(pag.getOffset() + pag.getLimit());
        }
    }

    /**
     * 检查是否有筛选条件
     */
    private boolean hasFilters() {
        // 检查是否有任何筛选条件被选中
        for (Set<String> filterSet : selectedFilters.values()) {
            if (filterSet != null && !filterSet.isEmpty()) {
                return true;
            }
        }
        // 检查是否有搜索关键词
        return keyword != null && !keyword.trim().isEmpty();
    }

    /**
     * 加载穿搭广场数据
     * - 如果没有筛选条件，使用 /api/outfits 接口（默认列表）
     * - 如果有筛选条件，使用 /api/discover/outfits 接口（筛选列表）
     */
    public void loadDiscoverOutfits(int offset) {
        loading.postValue(true);
        currentOffset = offset;

        // 如果是首次加载（offset == 0），立即清空列表，避免显示旧数据
        if (offset == 0) {
            filteredOutfits.postValue(new ArrayList<>());
        }

        // 检查是否有筛选条件
        if (hasFilters()) {
            // 有筛选条件，使用 discover/outfits 接口
            loadWithFilters(offset);
        } else {
            // 没有筛选条件，使用 outfits 接口（默认列表）
            loadDefaultOutfits(offset);
        }
    }

    /**
     * 加载默认穿搭列表（使用 /api/outfits 接口）
     */
    private void loadDefaultOutfits(int offset) {
        android.util.Log.d("HomeViewModel", "loadDefaultOutfits called with offset: " + offset);
        repository.getOutfits(DEFAULT_LIMIT, offset, new OutfitRepository.OutfitCallback<OutfitListResponse>() {
            @Override
            public void onSuccess(OutfitListResponse data) {
                android.util.Log.d("HomeViewModel", "loadDefaultOutfits onSuccess, data.isSuccess: " + (data != null ? data.isSuccess() : "null") + ", outfits count: " + (data != null && data.getOutfits() != null ? data.getOutfits().size() : "null"));
                try {
                    if (data != null && data.isSuccess() && data.getOutfits() != null) {
                        List<OutfitDisplayModel> displayModels = new ArrayList<>();
                        
                        android.util.Log.d("HomeViewModel", "Converting " + data.getOutfits().size() + " outfits to display models");
                        for (OutfitListResponse.OutfitListItem item : data.getOutfits()) {
                            try {
                                OutfitDisplayModel model = toDisplayModelFromListItem(item);
                                displayModels.add(model);
                            } catch (Exception e) {
                                android.util.Log.e("HomeViewModel", "Error converting outfit item: " + item.getId(), e);
                            }
                        }
                        
                        android.util.Log.d("HomeViewModel", "Created " + displayModels.size() + " display models");
                        
                        if (offset == 0) {
                            // 首次加载，直接替换所有数据
                            allOutfits.postValue(displayModels);
                            android.util.Log.d("HomeViewModel", "Replaced allOutfits with " + displayModels.size() + " default items");
                        } else {
                            // 加载更多，追加数据
                            List<OutfitDisplayModel> existing = allOutfits.getValue();
                            if (existing == null) {
                                existing = new ArrayList<>();
                            }
                            existing.addAll(displayModels);
                            allOutfits.postValue(existing);
                            android.util.Log.d("HomeViewModel", "Appended " + displayModels.size() + " items, total: " + existing.size());
                        }
                        
                        pagination.postValue(data.getPagination());
                        // 默认列表需要应用本地筛选（关键词搜索）
                        // 直接使用新加载的数据，避免异步问题
                        applyFilters(displayModels);
                        // 成功加载数据，清空错误消息
                        error.postValue("");
                    } else {
                        String errorMsg = "获取穿搭数据失败";
                        if (data == null) {
                            errorMsg = "响应数据为空";
                        } else if (!data.isSuccess()) {
                            errorMsg = "服务器返回失败";
                        } else if (data.getOutfits() == null) {
                            errorMsg = "穿搭列表为空";
                        }
                        android.util.Log.e("HomeViewModel", errorMsg);
                        error.postValue(errorMsg);
                    }
                } catch (Exception e) {
                    android.util.Log.e("HomeViewModel", "Error processing outfit data", e);
                    error.postValue("处理数据时出错: " + e.getMessage());
                }
                loading.postValue(false);
            }

            @Override
            public void onError(String errorMsg) {
                android.util.Log.e("HomeViewModel", "loadDefaultOutfits onError: " + errorMsg);
                loading.postValue(false);
                error.postValue(errorMsg);
            }
        });
    }

    /**
     * 加载筛选后的穿搭列表（使用 /api/discover/outfits 接口）
     */
    private void loadWithFilters(int offset) {
        // 构建筛选参数（传入key以进行值映射）
        String season = buildFilterString(selectedFilters.get("season"), "season");
        String weather = buildFilterString(selectedFilters.get("weather"), "weather");
        String occasion = buildFilterString(selectedFilters.get("scene"), "scene"); // scene对应occasion
        String style = buildFilterString(selectedFilters.get("style"), "style");
        String category = buildFilterString(selectedFilters.get("category"), "category");
        String color = buildFilterString(selectedFilters.get("color"), "color");
        String sex = buildFilterString(selectedFilters.get("sex"), "sex");

        android.util.Log.d("HomeViewModel", "loadWithFilters called with offset: " + offset);
        repository.discoverOutfits(season, weather, occasion, style, category, color, sex,
                DEFAULT_LIMIT, offset, new OutfitRepository.OutfitCallback<OutfitListResponse>() {
            @Override
            public void onSuccess(OutfitListResponse data) {
                android.util.Log.d("HomeViewModel", "loadWithFilters onSuccess, data.isSuccess: " + (data != null ? data.isSuccess() : "null") + ", outfits count: " + (data != null && data.getOutfits() != null ? data.getOutfits().size() : "null"));
                try {
                    if (data != null && data.isSuccess() && data.getOutfits() != null) {
                        List<OutfitDisplayModel> displayModels = new ArrayList<>();
                        
                        android.util.Log.d("HomeViewModel", "Converting " + data.getOutfits().size() + " outfits to display models");
                        for (OutfitListResponse.OutfitListItem item : data.getOutfits()) {
                            try {
                                OutfitDisplayModel model = toDisplayModelFromListItem(item);
                                displayModels.add(model);
                            } catch (Exception e) {
                                android.util.Log.e("HomeViewModel", "Error converting outfit item: " + item.getId(), e);
                            }
                        }
                        
                        android.util.Log.d("HomeViewModel", "Created " + displayModels.size() + " display models");
                        
                        if (offset == 0) {
                            // 首次加载筛选结果，直接替换所有数据（只显示筛选后的结果）
                            allOutfits.postValue(displayModels);
                            android.util.Log.d("HomeViewModel", "Replaced allOutfits with " + displayModels.size() + " filtered items");
                        } else {
                            // 加载更多，追加数据
                            List<OutfitDisplayModel> existing = allOutfits.getValue();
                            if (existing == null) {
                                existing = new ArrayList<>();
                            }
                            existing.addAll(displayModels);
                            allOutfits.postValue(existing);
                            android.util.Log.d("HomeViewModel", "Appended " + displayModels.size() + " items, total: " + existing.size());
                        }
                        
                        pagination.postValue(data.getPagination());
                        
                        // 筛选后的数据已经经过服务器筛选，只需要应用关键词搜索
                        // 不需要再应用本地筛选条件，直接显示结果
                        // 直接使用新加载的数据，避免异步问题
                        applyKeywordFilter(displayModels);
                        // 成功加载数据，清空错误消息
                        error.postValue("");
                    } else {
                        String errorMsg = "获取筛选结果失败";
                        if (data == null) {
                            errorMsg = "响应数据为空";
                        } else if (!data.isSuccess()) {
                            errorMsg = "服务器返回失败";
                        } else if (data.getOutfits() == null) {
                            errorMsg = "穿搭列表为空";
                        }
                        android.util.Log.e("HomeViewModel", errorMsg);
                        error.postValue(errorMsg);
                    }
                } catch (Exception e) {
                    android.util.Log.e("HomeViewModel", "Error processing filtered outfit data", e);
                    error.postValue("处理数据时出错: " + e.getMessage());
                }
                loading.postValue(false);
            }

            @Override
            public void onError(String errorMsg) {
                android.util.Log.e("HomeViewModel", "loadWithFilters onError: " + errorMsg);
                loading.postValue(false);
                error.postValue(errorMsg);
            }
        });
    }

    /**
     * 映射筛选值（将UI显示的值映射为API需要的值）
     */
    private String mapFilterValue(String filterKey, String value) {
        // 季节映射
        if ("season".equals(filterKey)) {
            if ("春季".equals(value)) return "春";
            if ("夏季".equals(value)) return "夏";
            if ("秋季".equals(value)) return "秋";
            if ("冬季".equals(value)) return "冬";
        }
        // 性别映射（如果从UI传入的是中文，需要转换为API格式）
        if ("sex".equals(filterKey)) {
            if ("男".equals(value)) return "male";
            if ("女".equals(value)) return "female";
            if ("中性".equals(value)) return "unisex";
            // 如果已经是API格式，直接返回
            if ("male".equals(value) || "female".equals(value) || "unisex".equals(value)) {
                return value;
            }
        }
        // 其他筛选条件保持原样，或者根据需要添加映射
        return value;
    }

    private String buildFilterString(Set<String> filterSet, String filterKey) {
        if (filterSet == null || filterSet.isEmpty()) {
            return null;
        }
        // 映射每个值，然后用逗号连接
        List<String> mappedValues = new ArrayList<>();
        for (String value : filterSet) {
            String mapped = mapFilterValue(filterKey, value);
            if (mapped != null && !mapped.isEmpty()) {
                mappedValues.add(mapped);
            }
        }
        if (mappedValues.isEmpty()) {
            return null;
        }
        String result = String.join(",", mappedValues);
        android.util.Log.d("HomeViewModel", "buildFilterString for " + filterKey + ": " + filterSet + " -> " + result);
        return result;
    }

    /**
     * 更新收藏状态
     */
    private void updateFavoriteState(int outfitId, boolean isFavorited, int newLikes) {
        List<OutfitDisplayModel> all = allOutfits.getValue();
        if (all == null) return;
        for (OutfitDisplayModel model : all) {
            if (model.getOutfitId() == outfitId) {
                model.setFavorite(isFavorited);
                model.setLikes(newLikes);
                break;
            }
        }
        applyFilters();
    }

    /**
     * 从OutfitListItem转换为OutfitDisplayModel
     * 显示整体风格、适用场合、适合天气、性别标签
     */
    private OutfitDisplayModel toDisplayModelFromListItem(OutfitListResponse.OutfitListItem item) {
        OutfitTags tags = item.getTags();
        List<String> mergedTags = new ArrayList<>();

        if (tags != null) {
            // 只显示整体风格、适用场合、适合天气
            addLimited(mergedTags, tags.getOverallStyle(), 2);
            addLimited(mergedTags, tags.getOccasion(), 1);
            addLimited(mergedTags, tags.getWeather(), 1);
            // 删除季节和颜色标签
        }

        // 添加性别标签
        String sexDisplay = getSexDisplayText(item);
        if (sexDisplay != null && !sexDisplay.isEmpty()) {
            mergedTags.add(sexDisplay);
        }

        String owner = "用户 " + item.getUserId();
        int likes = item.getLikes();
        boolean isFavorited = item.getIsFavorited() != null && item.getIsFavorited();

        OutfitDisplayModel model = new OutfitDisplayModel(item.getImageUrl(), mergedTags, owner, likes);
        model.setOutfitId(item.getId());
        model.setFavorite(isFavorited);
        return model;
    }

    /**
     * 获取性别的显示文本（将API格式转换为中文）
     * 优先使用item.getSex()，如果为空则使用tags.getSex()
     */
    private String getSexDisplayText(OutfitListResponse.OutfitListItem item) {
        // 优先使用item的sex字段（String类型）
        String sex = item.getSex();
        if (sex != null && !sex.isEmpty()) {
            return mapSexToDisplay(sex);
        }
        
        // 如果item的sex为空，尝试从tags中获取
        OutfitTags tags = item.getTags();
        if (tags != null && tags.getSex() != null && !tags.getSex().isEmpty()) {
            // tags.getSex()返回List<String>，取第一个
            String sexFromTags = tags.getSex().get(0);
            if (sexFromTags != null && !sexFromTags.isEmpty()) {
                return mapSexToDisplay(sexFromTags);
            }
        }
        
        return null;
    }

    /**
     * 将API的sex值（male/female/unisex）转换为中文显示（男/女/中性）
     */
    private String mapSexToDisplay(String sex) {
        if (sex == null || sex.isEmpty()) {
            return null;
        }
        switch (sex.toLowerCase()) {
            case "male":
                return "男";
            case "female":
                return "女";
            case "unisex":
                return "中性";
            default:
                // 如果已经是中文，直接返回
                if (sex.equals("男") || sex.equals("女") || sex.equals("中性")) {
                    return sex;
                }
                return sex; // 其他情况直接返回原值
        }
    }

    /**
     * 旧版loadData方法（保持兼容性）
     * 根据登录状态选择API：
     * - 游客：使用 /api/outfits（不需要token）
     * - 正常登录：如果有个人偏好，使用 /api/discover/outfits（需要token）
     */
    public void loadData() {
        // 检查登录状态
        com.example.outfitchanges.utils.SharedPrefManager prefManager = 
            new com.example.outfitchanges.utils.SharedPrefManager(getApplication());
        
        // 如果是游客，直接使用默认接口（不需要token）
        if (prefManager.isGuestMode() || !prefManager.isLoggedIn()) {
            loadDiscoverOutfits();
        } else {
            // 正常登录，如果有个人偏好会自动应用（通过ProfileFragment的applyPreferencesToHome）
            // 如果没有个人偏好，使用默认接口
            loadDiscoverOutfits();
        }
    }

    public void updateKeyword(String keyword) {
        this.keyword = keyword == null ? "" : keyword.trim().toLowerCase(Locale.getDefault());
        applyFilters();
    }

    public void toggleFilter(String key, String value) {
        if (!selectedFilters.containsKey(key)) return;
        Set<String> values = selectedFilters.get(key);
        if (values.contains(value)) {
            values.remove(value);
        } else {
            values.add(value);
        }
        // 立即清空列表，避免显示旧数据
        filteredOutfits.postValue(new ArrayList<>());
        // 重新加载数据（带筛选）
        loadDiscoverOutfits(0);
    }

    public void clearFilters() {
        for (Set<String> set : selectedFilters.values()) {
            set.clear();
        }
        // 立即清空列表，避免显示旧数据
        filteredOutfits.postValue(new ArrayList<>());
        // 重新加载数据（无筛选）
        loadDiscoverOutfits(0);
    }

    /**
     * 应用个人喜好筛选
     * @param preferences 个人喜好设置
     */
    public void applyUserPreferences(com.example.outfitchanges.auth.model.ProfileResponse.Preferences preferences) {
        if (preferences == null) {
            return;
        }
        
        // 清空现有筛选（但保留天气筛选和性别筛选，因为天气筛选是自动的，性别筛选是用户基础信息）
        Set<String> weatherFilter = selectedFilters.get("weather");
        String savedWeather = null;
        if (weatherFilter != null && !weatherFilter.isEmpty()) {
            // 保存当前的天气筛选
            savedWeather = weatherFilter.iterator().next();
        }
        
        Set<String> sexFilter = selectedFilters.get("sex");
        String savedSex = null;
        if (sexFilter != null && !sexFilter.isEmpty()) {
            // 保存当前的性别筛选
            savedSex = sexFilter.iterator().next();
        }
        
        // 清空除天气和性别外的其他筛选
        for (Map.Entry<String, Set<String>> entry : selectedFilters.entrySet()) {
            if (!"weather".equals(entry.getKey()) && !"sex".equals(entry.getKey())) {
                entry.getValue().clear();
            }
        }
        
        // 恢复天气筛选
        if (savedWeather != null && weatherFilter != null) {
            weatherFilter.clear();
            weatherFilter.add(savedWeather);
        }
        
        // 恢复性别筛选
        if (savedSex != null && sexFilter != null) {
            sexFilter.clear();
            sexFilter.add(savedSex);
        }
        
        // 应用偏好性别（从用户个人资料中获取）
        // 注意：性别偏好存储在Profile的gender字段，而不是Preferences中
        // 这里需要从Profile中获取gender，然后映射为sex筛选值
        
        // 应用偏好风格
        if (preferences.getPreferredStyles() != null && preferences.getPreferredStyles().length > 0) {
            Set<String> styleSet = selectedFilters.get("style");
            if (styleSet != null) {
                for (String style : preferences.getPreferredStyles()) {
                    styleSet.add(style);
                }
            }
        }
        
        // 应用偏好颜色
        if (preferences.getPreferredColors() != null && preferences.getPreferredColors().length > 0) {
            Set<String> colorSet = selectedFilters.get("color");
            if (colorSet != null) {
                for (String color : preferences.getPreferredColors()) {
                    colorSet.add(color);
                }
            }
        }
        
        // 应用偏好季节
        if (preferences.getPreferredSeasons() != null && preferences.getPreferredSeasons().length > 0) {
            Set<String> seasonSet = selectedFilters.get("season");
            if (seasonSet != null) {
                for (String season : preferences.getPreferredSeasons()) {
                    // 将API返回的季节值转换为UI显示的值
                    String uiSeason = mapSeasonToUI(season);
                    if (uiSeason != null) {
                        seasonSet.add(uiSeason);
                    }
                }
            }
        }
        
        // 立即清空列表，避免显示旧数据
        filteredOutfits.postValue(new ArrayList<>());
        // 重新加载数据（带个人喜好筛选）
        loadDiscoverOutfits(0);
    }
    
    /**
     * 应用用户性别偏好筛选
     * @param gender 用户性别（"男"、"女"、"其他"或API格式"male"、"female"、"unisex"）
     * @param skipReload 是否跳过重新加载（用于组合筛选时避免重复加载）
     */
    public void applyUserGender(String gender, boolean skipReload) {
        if (gender == null || gender.isEmpty()) {
            return;
        }
        
        // 将UI显示的性别值映射为API需要的值
        String sexValue = mapGenderToSex(gender);
        if (sexValue != null) {
            Set<String> sexSet = selectedFilters.get("sex");
            if (sexSet != null) {
                // 检查是否已经有相同的性别筛选
                if (sexSet.size() == 1 && sexSet.contains(sexValue)) {
                    // 如果性别筛选已经存在且相同，且不需要重新加载，直接返回
                    if (skipReload) {
                        return;
                    }
                    // 如果性别筛选已经存在且相同，但数据可能还没加载，检查是否需要加载
                    List<OutfitDisplayModel> currentData = allOutfits.getValue();
                    if (currentData != null && !currentData.isEmpty()) {
                        // 数据已加载，不需要重新加载
                        return;
                    }
                    // 数据未加载，继续加载
                }
                
                sexSet.clear();
                sexSet.add(sexValue);
                
                if (!skipReload) {
                    // 立即清空列表，避免显示旧数据
                    filteredOutfits.postValue(new ArrayList<>());
                    // 重新加载数据（带性别筛选，同时保留其他筛选条件如个人偏好）
                    loadDiscoverOutfits(0);
                }
            }
        }
    }
    
    /**
     * 应用用户性别偏好筛选（默认会重新加载数据）
     */
    public void applyUserGender(String gender) {
        applyUserGender(gender, false);
    }
    
    /**
     * 将用户性别（UI显示值）映射为API需要的sex值
     */
    private String mapGenderToSex(String gender) {
        if (gender == null) return null;
        switch (gender) {
            case "男":
                return "male";
            case "女":
                return "female";
            case "其他":
                return "unisex";
            default:
                // 如果已经是API格式，直接返回
                if (gender.equals("male") || gender.equals("female") || gender.equals("unisex")) {
                    return gender;
                }
                return null;
        }
    }
    
    /**
     * 将API返回的季节值映射为UI显示的值
     */
    private String mapSeasonToUI(String season) {
        if (season == null) return null;
        switch (season) {
            case "春":
                return "春季";
            case "夏":
                return "夏季";
            case "秋":
                return "秋季";
            case "冬":
                return "冬季";
            default:
                return season;
        }
    }
    
    /**
     * 根据温度判断天气类型
     * @param temp 温度值（字符串，可能包含"°C"）
     * @return 天气类型：寒冷、凉爽、温暖、炎热
     */
    private String getWeatherTypeFromTemp(String temp) {
        if (temp == null || temp.isEmpty()) {
            return null;
        }
        
        try {
            // 移除可能的"°C"后缀和空格
            String tempStr = temp.replace("°C", "").replace("°", "").trim();
            double temperature = Double.parseDouble(tempStr);
            
            if (temperature < 10) {
                return "寒冷";
            } else if (temperature < 20) {
                return "凉爽";
            } else if (temperature < 28) {
                return "温暖";
            } else {
                return "炎热";
            }
        } catch (NumberFormatException e) {
            android.util.Log.e("HomeViewModel", "无法解析温度: " + temp, e);
            return null;
        }
    }
    
    /**
     * 根据天气数据自动应用天气筛选
     * @param nowWeather 当前天气数据
     */
    public void applyWeatherFilter(WeatherResponse.NowWeather nowWeather) {
        if (nowWeather == null) {
            return;
        }
        
        String temp = nowWeather.getTemp();
        String weatherType = getWeatherTypeFromTemp(temp);
        
        if (weatherType != null) {
            // 更新天气筛选（不清空其他筛选条件，如个人偏好）
            Set<String> weatherSet = selectedFilters.get("weather");
            if (weatherSet != null) {
                // 检查是否已经有相同的天气类型，避免重复加载
                if (weatherSet.size() == 1 && weatherSet.contains(weatherType)) {
                    // 天气类型没有变化，不需要重新加载
                    return;
                }
                
                weatherSet.clear();
                weatherSet.add(weatherType);
                
                // 立即清空列表，避免显示旧数据
                filteredOutfits.postValue(new ArrayList<>());
                // 重新加载数据（带天气筛选，同时保留其他筛选条件如个人偏好）
                loadDiscoverOutfits(0);
            }
        }
    }

    /**
     * 切换收藏状态（通过imageUrl，兼容旧代码）
     * 新代码应该使用toggleFavoriteOutfit(int outfitId, boolean isCurrentlyFavorited)
     */
    public void toggleFavorite(String imageUrl) {
        // 尝试通过imageUrl找到对应的outfitId
        List<OutfitDisplayModel> all = allOutfits.getValue();
        if (all != null) {
            for (OutfitDisplayModel model : all) {
                if (model.getImageUrl().equals(imageUrl)) {
                    // 如果找到了，使用新的API
                    toggleFavoriteOutfit(model.getOutfitId(), model.isFavorite());
                    return;
                }
            }
        }
        // 如果没找到，使用旧的本地逻辑（兼容性）
        if (favoriteIds.contains(imageUrl)) {
            favoriteIds.remove(imageUrl);
            adjustLikes(imageUrl, -1);
        } else {
            favoriteIds.add(imageUrl);
            adjustLikes(imageUrl, 1);
        }
        refreshFavoriteState();
    }

    private void adjustLikes(String imageUrl, int delta) {
        List<OutfitDisplayModel> all = allOutfits.getValue();
        if (all == null) return;
        for (OutfitDisplayModel model : all) {
            if (model.getImageUrl().equals(imageUrl)) {
                model.adjustLikes(delta);
            }
        }
    }

    // 移除天气相关的自动筛选功能
    // public void applyWeatherDefaults(com.example.outfitchanges.ui.weather.model.WeatherResponse.NowWeather nowWeather) {
    //     // 不再自动应用天气筛选
    // }

    private void refreshFavoriteState() {
        List<OutfitDisplayModel> all = allOutfits.getValue();
        if (all == null) return;
        for (OutfitDisplayModel model : all) {
            model.setFavorite(favoriteIds.contains(model.getImageUrl()));
        }
        applyFilters();
    }

    /**
     * 只应用关键词过滤（用于筛选后的数据，因为服务器已经筛选过了）
     */
    private void applyKeywordFilter() {
        applyKeywordFilter(allOutfits.getValue());
    }

    /**
     * 只应用关键词过滤（使用指定的数据源）
     */
    private void applyKeywordFilter(List<OutfitDisplayModel> source) {
        if (source == null) {
            android.util.Log.w("HomeViewModel", "applyKeywordFilter: source is null, setting empty list");
            filteredOutfits.postValue(new ArrayList<>());
            return;
        }
        List<OutfitDisplayModel> result = new ArrayList<>();
        for (OutfitDisplayModel model : source) {
            if (keywordMatch(model)) {
                result.add(model);
            }
        }
        android.util.Log.d("HomeViewModel", "applyKeywordFilter: " + source.size() + " -> " + result.size() + " (keyword: '" + keyword + "')");
        filteredOutfits.postValue(result);
    }

    /**
     * 应用所有筛选条件（关键词 + 本地筛选，用于默认列表）
     */
    private void applyFilters() {
        applyFilters(allOutfits.getValue());
    }

    /**
     * 应用所有筛选条件（使用指定的数据源）
     */
    private void applyFilters(List<OutfitDisplayModel> source) {
        if (source == null) {
            android.util.Log.w("HomeViewModel", "applyFilters: source is null, setting empty list");
            filteredOutfits.postValue(new ArrayList<>());
            return;
        }
        List<OutfitDisplayModel> result = new ArrayList<>();
        for (OutfitDisplayModel model : source) {
            if (keywordMatch(model) && filterMatch(model)) {
                result.add(model);
            }
        }
        android.util.Log.d("HomeViewModel", "applyFilters: " + source.size() + " -> " + result.size());
        filteredOutfits.postValue(result);
    }

    private boolean keywordMatch(OutfitDisplayModel model) {
        if (keyword.isEmpty()) return true;
        StringBuilder builder = new StringBuilder();
        builder.append(model.getOwner().toLowerCase(Locale.getDefault()));
        builder.append(" ");
        for (String tag : model.getTags()) {
            builder.append(tag.toLowerCase(Locale.getDefault())).append(" ");
        }
        return builder.toString().contains(keyword);
    }

    private boolean filterMatch(OutfitDisplayModel model) {
        Map<String, Set<String>> filters = selectedFilters;
        if (isFilterEmpty(filters)) return true;

        Set<String> tags = new HashSet<>(model.getTags());

        if (!match(filters.get("season"), tags)) return false;
        if (!match(filters.get("weather"), tags)) return false;
        if (!match(filters.get("scene"), tags)) return false;
        if (!match(filters.get("style"), tags)) return false;
        if (!match(filters.get("category"), tags)) return false;
        return match(filters.get("color"), tags);
    }

    private boolean isFilterEmpty(Map<String, Set<String>> filters) {
        for (Set<String> set : filters.values()) {
            if (!set.isEmpty()) return false;
        }
        return true;
    }

    private boolean match(Set<String> filterValues, Set<String> targetTags) {
        if (filterValues == null || filterValues.isEmpty()) return true;
        for (String value : filterValues) {
            if (targetTags.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private OutfitDisplayModel toDisplayModel(OutfitItem item, int index) {
        OutfitTags tags = item.getTags();
        List<String> mergedTags = new ArrayList<>();

        addLimited(mergedTags, tags.getSeason(), 1);
        addLimited(mergedTags, tags.getOverallStyle(), 2);
        addLimited(mergedTags, tags.getOccasion(), 1);
        addLimited(mergedTags, tags.getAllCategories(), 2);
        addLimited(mergedTags, tags.getAllColors(), 1);
        addLimited(mergedTags, tags.getWeather(), 1);

        String owner = "穿搭达人 " + (index + 1);
        int likes = 80 + (index * 7) % 200;

        OutfitDisplayModel model = new OutfitDisplayModel(item.getImageUrl(), mergedTags, owner, likes);
        model.setFavorite(favoriteIds.contains(model.getImageUrl()));
        return model;
    }

    private void addLimited(List<String> target, List<String> source, int limit) {
        if (source == null || source.isEmpty() || limit <= 0) return;
        int count = 0;
        for (String s : source) {
            if (count >= limit) break;
            target.add(s);
            count++;
        }
    }

    /**
     * 收藏/取消收藏穿搭（通过outfitId）
     */
    public void toggleFavoriteOutfit(int outfitId, boolean isCurrentlyFavorited) {
        if (isCurrentlyFavorited) {
            repository.unfavoriteOutfit(outfitId, new OutfitRepository.OutfitCallback<FavoriteActionResponse>() {
                @Override
                public void onSuccess(FavoriteActionResponse data) {
                    // 更新本地状态
                    updateFavoriteState(outfitId, false, data.getLikes());
                }

                @Override
                public void onError(String errorMsg) {
                    error.postValue(errorMsg);
                }
            });
        } else {
            repository.favoriteOutfit(outfitId, new OutfitRepository.OutfitCallback<FavoriteActionResponse>() {
                @Override
                public void onSuccess(FavoriteActionResponse data) {
                    // 更新本地状态
                    updateFavoriteState(outfitId, true, data.getLikes());
                }

                @Override
                public void onError(String errorMsg) {
                    error.postValue(errorMsg);
                }
            });
        }
    }
}
