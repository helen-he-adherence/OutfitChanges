package com.example.outfitchanges.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.outfitchanges.ui.home.data.OutfitRepository;
import com.example.outfitchanges.ui.home.model.OutfitDisplayModel;
import com.example.outfitchanges.ui.home.model.OutfitItem;
import com.example.outfitchanges.ui.home.model.OutfitTags;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class HomeViewModel extends ViewModel {

    private final OutfitRepository repository = new OutfitRepository();

    private final MutableLiveData<List<OutfitDisplayModel>> allOutfits = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<OutfitDisplayModel>> filteredOutfits = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>("");

    private final Map<String, Set<String>> selectedFilters = new HashMap<>();
    private final Set<String> favoriteIds = new HashSet<>();
    private String keyword = "";
    private boolean weatherApplied = false;

    public HomeViewModel() {
        selectedFilters.put("season", new HashSet<>());
        selectedFilters.put("weather", new HashSet<>());
        selectedFilters.put("scene", new HashSet<>());
        selectedFilters.put("style", new HashSet<>());
        selectedFilters.put("category", new HashSet<>());
        selectedFilters.put("color", new HashSet<>());
    }

    public LiveData<List<OutfitDisplayModel>> getOutfits() {
        return filteredOutfits;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadData() {
        loading.postValue(true);
        repository.loadOutfits(new OutfitRepository.LoadCallback() {
            @Override
            public void onSuccess(List<OutfitItem> items) {
                List<OutfitDisplayModel> displayModels = new ArrayList<>();
                int index = 0;
                for (OutfitItem item : items) {
                    displayModels.add(toDisplayModel(item, index));
                    index++;
                }
                allOutfits.postValue(displayModels);
                applyFilters();
                loading.postValue(false);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
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
        applyFilters();
    }

    public void clearFilters() {
        for (Set<String> set : selectedFilters.values()) {
            set.clear();
        }
        applyFilters();
    }

    public void toggleFavorite(String imageUrl) {
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

    public void applyWeatherDefaults(com.example.outfitchanges.ui.weather.model.WeatherResponse.NowWeather nowWeather) {
        if (nowWeather == null || weatherApplied || !isFilterEmpty(selectedFilters)) return;

        String text = nowWeather.getText() == null ? "" : nowWeather.getText();
        String tempStr = nowWeather.getTemp();
        int temp = 20;
        try {
            temp = Integer.parseInt(tempStr);
        } catch (NumberFormatException ignored) {
        }

        Set<String> weatherSet = selectedFilters.get("weather");
        if (text.contains("雨")) weatherSet.add("下雨");
        else if (text.contains("雪")) weatherSet.add("下雪");
        else if (text.contains("云") || text.contains("阴")) weatherSet.add("多云");
        else weatherSet.add("晴天");

        Set<String> seasonSet = selectedFilters.get("season");
        if (temp >= 28) seasonSet.add("夏季");
        else if (temp >= 20) seasonSet.add("春季");
        else if (temp >= 10) seasonSet.add("秋季");
        else seasonSet.add("冬季");

        weatherApplied = true;
        applyFilters();
    }

    private void refreshFavoriteState() {
        List<OutfitDisplayModel> all = allOutfits.getValue();
        if (all == null) return;
        for (OutfitDisplayModel model : all) {
            model.setFavorite(favoriteIds.contains(model.getImageUrl()));
        }
        applyFilters();
    }

    private void applyFilters() {
        List<OutfitDisplayModel> source = allOutfits.getValue();
        if (source == null) return;
        List<OutfitDisplayModel> result = new ArrayList<>();
        for (OutfitDisplayModel model : source) {
            if (keywordMatch(model) && filterMatch(model)) {
                result.add(model);
            }
        }
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
}
