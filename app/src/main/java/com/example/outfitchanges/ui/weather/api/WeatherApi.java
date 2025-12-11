package com.example.outfitchanges.ui.weather.api;

import com.example.outfitchanges.ui.weather.model.LocationResponse;
import com.example.outfitchanges.ui.weather.model.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    String BASE_URL = "https://nx7bygjncn.re.qweatherapi.com/";

    @GET("v7/weather/now")
    Call<WeatherResponse> getNowWeather(
            @Query("location") String location,
            @Query("key") String key,
            @Query("lang") String lang
    );

    @GET("v7/weather/24h")
    Call<WeatherResponse> get24HourWeather(
            @Query("location") String location,
            @Query("key") String key,
            @Query("lang") String lang
    );

    @GET("v7/weather/7d")
    Call<WeatherResponse> get7DayWeather(
            @Query("location") String location,
            @Query("key") String key,
            @Query("lang") String lang
    );

    @GET("geo/v2/city/lookup")
    Call<LocationResponse> lookupLocation(
            @Query("location") String location,
            @Query("key") String key,
            @Query("lang") String lang,
            @Query("number") Integer number
    );
    
    @GET("geo/v2/city/lookup")
    Call<LocationResponse> lookupLocationWithAdm(
            @Query("location") String location,
            @Query("adm") String adm,
            @Query("key") String key,
            @Query("lang") String lang,
            @Query("number") Integer number
    );

    @GET("geo/v2/city/lookup")
    Call<LocationResponse> lookupLocationByCoordinates(
            @Query("location") String location,
            @Query("key") String key,
            @Query("lang") String lang
    );
}
