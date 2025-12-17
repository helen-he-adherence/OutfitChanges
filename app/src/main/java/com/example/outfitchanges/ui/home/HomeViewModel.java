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
        
        // 初始化筛选器
        selectedFilters.put("season", new HashSet<>());
        selectedFilters.put("weather", new HashSet<>());
        selectedFilters.put("scene", new HashSet<>());
        selectedFilters.put("style", new HashSet<>());
        selectedFilters.put("category", new HashSet<>());
        selectedFilters.put("color", new HashSet<>());
    }

    private final MutableLiveData<List<OutfitDisplayModel>> allOutfits = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<OutfitDisplayModel>> filteredOutfits = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>("");
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

        android.util.Log.d("HomeViewModel", "loadWithFilters called with offset: " + offset);
        repository.discoverOutfits(season, weather, occasion, style, category, color,
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
     */
    private OutfitDisplayModel toDisplayModelFromListItem(OutfitListResponse.OutfitListItem item) {
        OutfitTags tags = item.getTags();
        List<String> mergedTags = new ArrayList<>();

        if (tags != null) {
            addLimited(mergedTags, tags.getSeason(), 1);
            addLimited(mergedTags, tags.getOverallStyle(), 2);
            addLimited(mergedTags, tags.getOccasion(), 1);
            addLimited(mergedTags, tags.getAllCategories(), 2);
            addLimited(mergedTags, tags.getAllColors(), 1);
            addLimited(mergedTags, tags.getWeather(), 1);
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
     * 旧版loadData方法（保持兼容性）
     */
    public void loadData() {
        // 默认使用穿搭广场API
        loadDiscoverOutfits();
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
