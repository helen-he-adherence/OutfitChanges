package com.example.outfitchanges.ui.weather.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.OnConflictStrategy;

import java.util.List;

@Dao
public interface CityDao {
    @Query("SELECT DISTINCT province FROM cities ORDER BY province")
    List<String> getAllProvinces();

    @Query("SELECT DISTINCT city FROM cities WHERE province = :province ORDER BY city")
    List<String> getCitiesByProvince(String province);

    @Query("SELECT DISTINCT district FROM cities WHERE province = :province AND city = :city ORDER BY district")
    List<String> getDistrictsByProvinceAndCity(String province, String city);

    @Query("SELECT * FROM cities WHERE province = :province AND city = :city AND district = :district LIMIT 1")
    CityEntity getCityByProvinceCityDistrict(String province, String city, String district);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CityEntity> cities);

    @Query("SELECT COUNT(*) FROM cities")
    int getCityCount();

    @Query("DELETE FROM cities")
    void deleteAll();
}
