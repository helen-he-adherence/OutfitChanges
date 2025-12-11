package com.example.outfitchanges.ui.weather.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import com.example.outfitchanges.ui.weather.api.WeatherApi;
import com.example.outfitchanges.ui.weather.database.AppDatabase;
import com.example.outfitchanges.ui.weather.database.CityDao;
import com.example.outfitchanges.ui.weather.database.CityEntity;
import com.example.outfitchanges.ui.weather.model.LocationResponse;
import com.example.outfitchanges.ui.weather.model.WeatherResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

public class WeatherRepository {
    private static final String API_KEY = "c751619c4ba74bb28e9acfeb81001625";
    private static final String PREF_NAME = "weather_prefs";
    private static final String KEY_LOCATION_ID = "location_id";
    private static final String KEY_PROVINCE = "province";
    private static final String KEY_CITY = "city";
    private static final String KEY_DISTRICT = "district";

    private WeatherApi weatherApi;
    private CityDao cityDao;
    private SharedPreferences sharedPreferences;
    private ExecutorService executorService;

    public WeatherRepository(Context context) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // 使用 HEADERS 级别，避免读取响应体导致 Gson 解析失败
        logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(chain -> {
                    // 仅补充必要的Accept头；不再手动设置Accept-Encoding，
                    // 让OkHttp自动处理gzip解压，避免Gson解析到压缩内容报错。
                    okhttp3.Request original = chain.request();
                    okhttp3.Request.Builder requestBuilder = original.newBuilder()
                            .header("Accept", "application/json");

                    // 如果URL中没有key参数，添加Header认证
                    if (!original.url().queryParameterNames().contains("key")) {
                        requestBuilder.header("X-QW-Api-Key", API_KEY);
                    }

                    okhttp3.Request request = requestBuilder
                            .method(original.method(), original.body())
                            .build();
                    return chain.proceed(request);
                })
                .build();

        // 创建 Gson 实例，设置 lenient 模式以处理可能的格式问题
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();
        
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(WeatherApi.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        weatherApi = retrofit.create(WeatherApi.class);
        cityDao = AppDatabase.getDatabase(context).cityDao();
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void getNowWeather(String locationId, WeatherCallback<WeatherResponse> callback) {
        weatherApi.getNowWeather(locationId, API_KEY, "zh")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("获取天气数据失败: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        callback.onError("网络错误: " + t.getMessage());
                    }
                });
    }

    public void get24HourWeather(String locationId, WeatherCallback<WeatherResponse> callback) {
        weatherApi.get24HourWeather(locationId, API_KEY, "zh")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("获取24小时天气数据失败: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        callback.onError("网络错误: " + t.getMessage());
                    }
                });
    }

    public void get7DayWeather(String locationId, WeatherCallback<WeatherResponse> callback) {
        weatherApi.get7DayWeather(locationId, API_KEY, "zh")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("获取7天天气数据失败: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<WeatherResponse> call, Throwable t) {
                        callback.onError("网络错误: " + t.getMessage());
                    }
                });
    }

    public void lookupLocationId(String province, String city, String district, LocationCallback callback) {
        android.util.Log.d("WeatherRepository", "查找位置 - 省份: " + province + ", 城市: " + city + ", 区县: " + district);
        
        // 首先尝试从数据库中获取locationId
        executorService.execute(() -> {
            CityEntity cityEntity = cityDao.getCityByProvinceCityDistrict(province, city, district);
            android.util.Log.d("WeatherRepository", "数据库查询结果: " + (cityEntity != null ? "找到" : "未找到"));
            if (cityEntity != null) {
                android.util.Log.d("WeatherRepository", "数据库记录 - province: " + cityEntity.getProvince() 
                    + ", city: " + cityEntity.getCity() 
                    + ", district: " + cityEntity.getDistrict()
                    + ", locationId: " + cityEntity.getLocationId());
            }
            
            if (cityEntity != null && cityEntity.getLocationId() != null && !cityEntity.getLocationId().isEmpty()) {
                String dbLocationId = cityEntity.getLocationId();
                android.util.Log.d("WeatherRepository", "数据库中的locationId: " + dbLocationId + ", 长度: " + dbLocationId.length());
                
                // 如果是9位数字，直接使用
                if (dbLocationId.length() == 9 && dbLocationId.matches("\\d+")) {
                    android.util.Log.d("WeatherRepository", "从数据库找到9位locationId: " + dbLocationId);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        callback.onSuccess(dbLocationId);
                    });
                    return;
                }
                // 如果是6位数字（区县code），使用它作为adcode查询locationId
                if (dbLocationId.length() == 6 && dbLocationId.matches("\\d+")) {
                    android.util.Log.d("WeatherRepository", "使用6位code作为adcode查询: " + dbLocationId);
                    new Handler(Looper.getMainLooper()).post(() -> {
                        // 使用6位code（adcode）查询locationId
                        tryLookupLocationById(dbLocationId, callback, () -> {
                            // 如果失败，尝试通过城市名称查找
                            android.util.Log.d("WeatherRepository", "使用adcode查询失败，尝试通过名称查找");
                            lookupLocationIdByApi(province, city, district, callback);
                        });
                    });
                    return;
                }
                android.util.Log.d("WeatherRepository", "数据库中的locationId格式不正确: " + dbLocationId);
            } else {
                android.util.Log.d("WeatherRepository", "数据库中未找到该城市记录");
            }
            
            // 数据库中未找到或格式不正确，通过API查找
            android.util.Log.d("WeatherRepository", "通过API查找locationId");
            new Handler(Looper.getMainLooper()).post(() -> {
                lookupLocationIdByApi(province, city, district, callback);
            });
        });
    }
    
    private void tryLookupLocationById(String adcode, LocationCallback callback, Runnable onFailure) {
        android.util.Log.d("WeatherRepository", "使用adcode查询locationId: " + adcode);
        
        // 使用 OkHttp 直接调用 API，手动解析 JSON，避免 Retrofit 的解析问题
        String url = WeatherApi.BASE_URL + "geo/v2/city/lookup?location=" + adcode + "&key=" + API_KEY + "&lang=zh&number=1";
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .header("Accept-Encoding", "gzip, deflate")
                .header("Accept", "application/json")
                .build();
        
        // 使用共享的 OkHttpClient 实例（从 Retrofit 配置中获取）
        // 让 OkHttp 自动处理 gzip，避免手动添加 Accept-Encoding
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        
        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                android.util.Log.e("WeatherRepository", "adcode查询网络错误: " + e.getMessage());
                onFailure.run();
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (!response.isSuccessful()) {
                    android.util.Log.d("WeatherRepository", "adcode查询HTTP错误: " + response.code());
                    onFailure.run();
                    return;
                }
                
                try {
                    String jsonString = readBodyAsString(response);
                    android.util.Log.d("WeatherRepository", "adcode查询原始JSON: " + jsonString.substring(0, Math.min(500, jsonString.length())));
                    
                    JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
                    String code = jsonObject.get("code").getAsString();
                    android.util.Log.d("WeatherRepository", "adcode查询响应 code: " + code);
                    
                    if (code != null && code.equals("200")) {
                        JsonArray locationArray = jsonObject.getAsJsonArray("location");
                        if (locationArray != null && locationArray.size() > 0) {
                            JsonObject firstLocation = locationArray.get(0).getAsJsonObject();
                            String foundLocationId = firstLocation.get("id").getAsString();
                            String foundName = firstLocation.get("name").getAsString();
                            android.util.Log.d("WeatherRepository", "使用adcode查询成功: " + foundName + ", locationId: " + foundLocationId);
                            new Handler(Looper.getMainLooper()).post(() -> {
                                callback.onSuccess(foundLocationId);
                            });
                            return;
                        }
                    }
                    
                    // 查询失败，尝试下一个方法
                    onFailure.run();
                } catch (Exception e) {
                    android.util.Log.e("WeatherRepository", "解析adcode查询响应出错: " + e.getMessage(), e);
                    e.printStackTrace();
                    onFailure.run();
                }
            }
        });
    }

    private String readBodyAsString(okhttp3.Response response) throws IOException {
        okhttp3.ResponseBody body = response.body();
        if (body == null) return "";
        String encoding = response.header("Content-Encoding", "");
        InputStream stream = body.byteStream();
        if (encoding != null && encoding.toLowerCase().contains("gzip")) {
            stream = new GZIPInputStream(stream);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
    
    private void lookupLocationIdByApi(String province, String city, String district, LocationCallback callback) {
        // 和风天气API支持多种查询格式，按优先级尝试
        // 1. 先尝试使用adm参数查询区县（最精确）
        tryLookupLocationWithAdm(district, city, callback, () -> {
            // 2. 如果失败，尝试使用adm参数查询区县，adm为省份
            tryLookupLocationWithAdm(district, province, callback, () -> {
                // 3. 如果还失败，尝试只查询区县名称
                tryLookupLocation(district, 5, callback, () -> {
                    // 4. 尝试查询城市名称
                    tryLookupLocation(city, 5, callback, () -> {
                        // 5. 最后尝试查询省份名称
                        tryLookupLocation(province, 5, callback, () -> {
                            callback.onError("未找到该位置，请检查城市名称是否正确。尝试查询: " + district);
                        });
                    });
                });
            });
        });
    }
    
    private void tryLookupLocation(String location, Integer number, LocationCallback callback, Runnable onFailure) {
        android.util.Log.d("WeatherRepository", "尝试查找位置(手动解析): " + location);
        String url = WeatherApi.BASE_URL + "geo/v2/city/lookup?location=" + location +
                "&key=" + API_KEY + "&lang=zh&number=" + (number != null ? number : 10);

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                android.util.Log.e("WeatherRepository", "网络错误(手动解析): " + e.getMessage());
                onFailure.run();
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (!response.isSuccessful()) {
                    android.util.Log.d("WeatherRepository", "HTTP错误(手动解析): " + response.code());
                    onFailure.run();
                    return;
                }

                try {
                    String jsonString = readBodyAsString(response);
                    android.util.Log.d("WeatherRepository", "手动解析原始JSON: " + jsonString.substring(0, Math.min(500, jsonString.length())));

                    JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
                    String code = jsonObject.get("code").getAsString();
                    android.util.Log.d("WeatherRepository", "手动解析 code: " + code);

                    if ("200".equals(code)) {
                        JsonArray locationArray = jsonObject.getAsJsonArray("location");
                        if (locationArray != null && locationArray.size() > 0) {
                            JsonObject firstLocation = locationArray.get(0).getAsJsonObject();
                            String foundLocationId = firstLocation.get("id").getAsString();
                            String foundName = firstLocation.get("name").getAsString();
                            android.util.Log.d("WeatherRepository", "手动解析成功: " + foundName + ", locationId: " + foundLocationId);
                            new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(foundLocationId));
                            return;
                        }
                    }

                    // 查询失败，尝试下一个格式
                    onFailure.run();
                } catch (Exception e) {
                    android.util.Log.e("WeatherRepository", "手动解析响应出错: " + e.getMessage(), e);
                    onFailure.run();
                }
            }
        });
    }
    
    private void tryLookupLocationWithAdm(String location, String adm, LocationCallback callback, Runnable onFailure) {
        android.util.Log.d("WeatherRepository", "尝试查找位置(带adm，手动解析): " + location + ", adm: " + adm);
        String url = WeatherApi.BASE_URL + "geo/v2/city/lookup?location=" + location +
                "&adm=" + adm + "&key=" + API_KEY + "&lang=zh&number=5";

        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .build();

        okHttpClient.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                android.util.Log.e("WeatherRepository", "网络错误(带adm，手动解析): " + e.getMessage());
                onFailure.run();
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (!response.isSuccessful()) {
                    android.util.Log.d("WeatherRepository", "HTTP错误(带adm，手动解析): " + response.code());
                    onFailure.run();
                    return;
                }

                try {
                    String jsonString = readBodyAsString(response);
                    android.util.Log.d("WeatherRepository", "带adm手动解析原始JSON: " + jsonString.substring(0, Math.min(500, jsonString.length())));

                    JsonObject jsonObject = new JsonParser().parse(jsonString).getAsJsonObject();
                    String code = jsonObject.get("code").getAsString();
                    android.util.Log.d("WeatherRepository", "带adm手动解析 code: " + code);

                    if ("200".equals(code)) {
                        JsonArray locationArray = jsonObject.getAsJsonArray("location");
                        if (locationArray != null && locationArray.size() > 0) {
                            JsonObject firstLocation = locationArray.get(0).getAsJsonObject();
                            String foundLocationId = firstLocation.get("id").getAsString();
                            String foundName = firstLocation.get("name").getAsString();
                            android.util.Log.d("WeatherRepository", "带adm手动解析成功: " + foundName + ", locationId: " + foundLocationId);
                            new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(foundLocationId));
                            return;
                        }
                    }

                    // 查询失败，尝试下一个格式
                    onFailure.run();
                } catch (Exception e) {
                    android.util.Log.e("WeatherRepository", "带adm手动解析响应出错: " + e.getMessage(), e);
                    onFailure.run();
                }
            }
        });
    }

    public void lookupLocationByCoordinates(String lat, String lon, LocationCallback callback) {
        String location = lon + "," + lat; // 和风天气API格式：经度,纬度
        weatherApi.lookupLocationByCoordinates(location, API_KEY, "zh")
                .enqueue(new Callback<LocationResponse>() {
                    @Override
                    public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            LocationResponse.Location[] locations = response.body().getLocation();
                            if (locations != null && locations.length > 0) {
                                callback.onSuccess(locations[0].getId());
                            } else {
                                callback.onError("未找到该位置");
                            }
                        } else {
                            callback.onError("查找位置失败: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<LocationResponse> call, Throwable t) {
                        callback.onError("网络错误: " + t.getMessage());
                    }
                });
    }

    public void loadCitiesFromJson(String jsonData, LoadCitiesCallback callback) {
        executorService.execute(() -> {
            try {
                Gson gson = new Gson();
                List<CityEntity> cities = new ArrayList<>();

                // 首先尝试解析为对象格式（pcas-code.json格式）
                try {
                    JsonObject jsonObject = gson.fromJson(jsonData, JsonObject.class);
                    for (Map.Entry<String, com.google.gson.JsonElement> provinceEntry : jsonObject.entrySet()) {
                        String province = provinceEntry.getKey();
                        JsonObject citiesObj = provinceEntry.getValue().getAsJsonObject();

                        for (Map.Entry<String, com.google.gson.JsonElement> cityEntry : citiesObj.entrySet()) {
                            String city = cityEntry.getKey();
                            JsonObject districtsObj = cityEntry.getValue().getAsJsonObject();

                            for (Map.Entry<String, com.google.gson.JsonElement> districtEntry : districtsObj.entrySet()) {
                                String district = districtEntry.getKey();
                                String locationId = districtEntry.getValue().getAsString();
                                cities.add(new CityEntity(province, city, district, locationId));
                            }
                        }
                    }
                } catch (Exception e1) {
                    // 如果对象格式解析失败，尝试数组格式（pcas-code.json格式）
                    try {
                        com.google.gson.JsonArray jsonArray = gson.fromJson(jsonData, com.google.gson.JsonArray.class);
                        for (com.google.gson.JsonElement provinceElement : jsonArray) {
                            JsonObject provinceObj = provinceElement.getAsJsonObject();
                            String province = provinceObj.get("name").getAsString();
                            com.google.gson.JsonArray citiesArray = provinceObj.getAsJsonArray("children");

                            if (citiesArray != null) {
                                for (com.google.gson.JsonElement cityElement : citiesArray) {
                                    JsonObject cityObj = cityElement.getAsJsonObject();
                                    String city = cityObj.get("name").getAsString();
                                    com.google.gson.JsonArray districtsArray = cityObj.getAsJsonArray("children");

                                    if (districtsArray != null) {
                                        for (com.google.gson.JsonElement districtElement : districtsArray) {
                                            JsonObject districtObj = districtElement.getAsJsonObject();
                                            String district = districtObj.get("name").getAsString();
                                            // 使用区县的code（6位数字），这是行政区划代码
                                            // 注意：这不是和风天气的locationId，需要通过API查找
                                            String code = districtObj.get("code").getAsString();
                                            // 暂时使用code，后续通过API查找真正的locationId
                                            String locationId = code;
                                            
                                            // 如果城市名是"市辖区"，使用省份名作为城市名
                                            String finalCity = city;
                                            if ("市辖区".equals(city)) {
                                                finalCity = province;
                                            }
                                            
                                            cities.add(new CityEntity(province, finalCity, district, locationId));
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e2) {
                        throw new Exception("无法解析数据格式: " + e1.getMessage() + ", " + e2.getMessage());
                    }
                }

                if (cities.isEmpty()) {
                    throw new Exception("解析后的城市数据为空");
                }

                cityDao.deleteAll();
                cityDao.insertAll(cities);

                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onSuccess();
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onError("加载城市数据失败: " + e.getMessage());
                });
            }
        });
    }

    public void getAllProvinces(DataCallback<List<String>> callback) {
        executorService.execute(() -> {
            List<String> provinces = cityDao.getAllProvinces();
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.onSuccess(provinces);
            });
        });
    }

    public void getCitiesByProvince(String province, DataCallback<List<String>> callback) {
        executorService.execute(() -> {
            List<String> cities = cityDao.getCitiesByProvince(province);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.onSuccess(cities);
            });
        });
    }

    public void getDistrictsByProvinceAndCity(String province, String city, DataCallback<List<String>> callback) {
        executorService.execute(() -> {
            List<String> districts = cityDao.getDistrictsByProvinceAndCity(province, city);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.onSuccess(districts);
            });
        });
    }

    public void getCityByProvinceCityDistrict(String province, String city, String district, DataCallback<CityEntity> callback) {
        executorService.execute(() -> {
            CityEntity cityEntity = cityDao.getCityByProvinceCityDistrict(province, city, district);
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.onSuccess(cityEntity);
            });
        });
    }

    public void saveLocation(String locationId, String province, String city, String district) {
        sharedPreferences.edit()
                .putString(KEY_LOCATION_ID, locationId)
                .putString(KEY_PROVINCE, province)
                .putString(KEY_CITY, city)
                .putString(KEY_DISTRICT, district)
                .apply();
    }

    public String getSavedLocationId() {
        return sharedPreferences.getString(KEY_LOCATION_ID, "101010100"); // 默认北京
    }

    public String getSavedProvince() {
        return sharedPreferences.getString(KEY_PROVINCE, "北京");
    }

    public String getSavedCity() {
        return sharedPreferences.getString(KEY_CITY, "北京");
    }

    public String getSavedDistrict() {
        return sharedPreferences.getString(KEY_DISTRICT, "东城区");
    }

    public void getCityCount(DataCallback<Integer> callback) {
        executorService.execute(() -> {
            int count = cityDao.getCityCount();
            new Handler(Looper.getMainLooper()).post(() -> {
                callback.onSuccess(count);
            });
        });
    }

    public interface WeatherCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    public interface LocationCallback {
        void onSuccess(String locationId);
        void onError(String error);
    }

    public interface LoadCitiesCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface DataCallback<T> {
        void onSuccess(T data);
    }
}
