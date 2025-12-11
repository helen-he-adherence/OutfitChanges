package com.example.outfitchanges.ui.weather;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.outfitchanges.ui.weather.model.WeatherResponse;
import com.example.outfitchanges.ui.weather.repository.WeatherRepository;

public class WeatherViewModel extends AndroidViewModel {
    private WeatherRepository repository;
    private MutableLiveData<WeatherResponse.NowWeather> currentWeather = new MutableLiveData<>();
    private MutableLiveData<WeatherResponse.HourlyWeather[]> hourlyWeather = new MutableLiveData<>();
    private MutableLiveData<WeatherResponse.DailyWeather[]> dailyWeather = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public WeatherViewModel(Application application) {
        super(application);
        repository = new WeatherRepository(application);
    }

    public LiveData<WeatherResponse.NowWeather> getCurrentWeather() {
        return currentWeather;
    }

    public LiveData<WeatherResponse.HourlyWeather[]> getHourlyWeather() {
        return hourlyWeather;
    }

    public LiveData<WeatherResponse.DailyWeather[]> getDailyWeather() {
        return dailyWeather;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadWeatherData(String locationId) {
        isLoading.setValue(true);

        repository.getNowWeather(locationId, new WeatherRepository.WeatherCallback<WeatherResponse>() {
            @Override
            public void onSuccess(WeatherResponse data) {
                currentWeather.postValue(data.getNow());
                isLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.postValue(error);
                isLoading.postValue(false);
            }
        });

        repository.get24HourWeather(locationId, new WeatherRepository.WeatherCallback<WeatherResponse>() {
            @Override
            public void onSuccess(WeatherResponse data) {
                hourlyWeather.postValue(data.getHourly());
            }

            @Override
            public void onError(String error) {
                // 24小时数据加载失败不影响主流程
            }
        });

        repository.get7DayWeather(locationId, new WeatherRepository.WeatherCallback<WeatherResponse>() {
            @Override
            public void onSuccess(WeatherResponse data) {
                dailyWeather.postValue(data.getDaily());
            }

            @Override
            public void onError(String error) {
                // 7天数据加载失败不影响主流程
            }
        });
    }

    public void refreshWeather() {
        String locationId = repository.getSavedLocationId();
        loadWeatherData(locationId);
    }

    public String getSavedLocationId() {
        return repository.getSavedLocationId();
    }

    public String getSavedProvince() {
        return repository.getSavedProvince();
    }

    public String getSavedCity() {
        return repository.getSavedCity();
    }

    public String getSavedDistrict() {
        return repository.getSavedDistrict();
    }

    public void saveLocation(String locationId, String province, String city, String district) {
        repository.saveLocation(locationId, province, city, district);
    }

    public void lookupLocationId(String province, String city, String district, WeatherRepository.LocationCallback callback) {
        repository.lookupLocationId(province, city, district, callback);
    }

    public void lookupLocationByCoordinates(String lat, String lon, WeatherRepository.LocationCallback callback) {
        repository.lookupLocationByCoordinates(lat, lon, callback);
    }

    public void getCityCount(WeatherRepository.DataCallback<Integer> callback) {
        repository.getCityCount(callback);
    }

    public void loadCitiesFromJson(String jsonData, WeatherRepository.LoadCitiesCallback callback) {
        repository.loadCitiesFromJson(jsonData, callback);
    }
}
