# OutfitChanges - 智能穿搭推荐系统

## 1. 系统功能

### 1.1 功能模块图

```
OutfitChanges 智能穿搭推荐系统
├── 用户认证管理系统
│   ├── 用户注册登录模块
│   ├── 密码找回模块
│   └── Token管理模块
├── 穿搭广场系统
│   ├── 穿搭浏览展示模块
│   ├── 智能筛选搜索模块
│   ├── 收藏管理模块
│   └── 个人偏好应用模块
├── 天气服务系统
│   ├── 天气查询显示模块
│   ├── 城市选择模块
│   ├── 穿搭建议生成模块
│   └── 天气自动筛选模块
├── 内容发布系统
│   ├── 图片上传模块
│   ├── AI智能识别模块
│   ├── 标签编辑模块
│   └── 穿搭发布模块
├── 虚拟试衣系统
│   ├── 人像上传模块
│   ├── 穿搭选择模块
│   ├── 试衣任务管理模块
│   └── 试衣结果展示模块
└── 个人中心系统
    ├── 个人资料管理模块
    ├── 我的收藏模块
    ├── 我的穿搭模块
    └── 偏好设置模块
```

### 1.2 主要功能模块介绍

#### 1.2.1 用户认证管理系统
- **用户注册登录模块**：支持用户注册、登录功能，提供游客模式，无需登录即可浏览穿搭广场
- **密码找回模块**：支持忘记密码、重置密码功能
- **Token管理模块**：统一管理用户认证Token，自动添加到所有网络请求中，支持Token持久化存储

#### 1.2.2 穿搭广场系统
- **穿搭浏览展示模块**：以瀑布流形式展示穿搭图片，显示标签（包括性别标签）、作者、点赞数，支持下拉刷新和分页加载
- **智能筛选搜索模块**：支持多维度筛选（季节、场景、风格、类别、颜色、天气、性别），支持关键词搜索
- **收藏管理模块**：用户可以收藏喜欢的穿搭，实时更新收藏状态和点赞数
- **个人偏好应用模块**：根据用户设置的偏好（风格、颜色、季节）和用户性别自动筛选推荐穿搭

#### 1.2.3 天气服务系统
- **天气查询显示模块**：显示当前天气、24小时天气预报、7天天气预报
- **城市选择模块**：支持自动定位和手动选择城市，使用Room数据库存储城市数据
- **穿搭建议生成模块**：根据当前温度、天气状况生成穿搭建议
- **天气自动筛选模块**：根据当前温度自动筛选适合的穿搭（寒冷、凉爽、温暖、炎热）

#### 1.2.4 内容发布系统
- **图片上传模块**：支持从相册选择图片上传
- **AI智能识别模块**：上传图片后，AI自动识别穿搭标签（季节、风格、场景、类别、颜色等）
- **标签编辑模块**：用户可以查看和修改AI识别的标签
- **穿搭发布模块**：编辑完成后发布穿搭到广场

#### 1.2.5 虚拟试衣系统
- **人像上传模块**：支持上传人像图片
- **穿搭选择模块**：从收藏列表中选择穿搭进行试衣
- **试衣任务管理模块**：支持异步试衣任务，自动轮询任务状态
- **试衣结果展示模块**：展示试衣后的效果图

#### 1.2.6 个人中心系统
- **个人资料管理模块**：查看和编辑个人资料
- **我的收藏模块**：查看所有收藏的穿搭
- **我的穿搭模块**：查看自己发布的所有穿搭
- **偏好设置模块**：设置个人偏好（风格、颜色、季节），自动应用到穿搭广场推荐

## 2. 系统设计

### 2.1 架构设计

本系统采用 **MVVM（Model-View-ViewModel）架构模式**，实现了数据与UI的分离，提高了代码的可维护性和可测试性。

#### 2.1.1 架构层次

```
┌─────────────────────────────────────┐
│          View Layer (UI)             │
│  - Fragment / Activity               │
│  - Layout XML                        │
│  - Adapter (RecyclerView)            │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      ViewModel Layer                 │
│  - HomeViewModel                     │
│  - ProfileViewModel                  │
│  - WeatherViewModel                  │
│  - PublishViewModel                  │
│  - VirtualTryOnViewModel             │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Repository Layer                │
│  - OutfitRepository                  │
│  - ProfileRepository                 │
│  - WeatherRepository                 │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Network Layer                   │
│  - Retrofit + OkHttp                 │
│  - NetworkClient (单例模式)           │
│  - API Service                       │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│      Data Layer                      │
│  - Room Database (城市数据)           │
│  - SharedPreferences (用户数据)      │
└─────────────────────────────────────┘
```

### 2.2 核心组件

#### 2.2.1 网络请求组件

**Retrofit + OkHttp**
- 使用Retrofit进行RESTful API调用
- 使用OkHttp作为HTTP客户端，配置超时时间和日志拦截器
- 实现Token拦截器，自动为所有请求添加Authorization头

**网络客户端设计**：
- `AuthNetworkClient`：用户认证相关API
- `OutfitNetworkClient`：穿搭相关API
- `PublishNetworkClient`：发布相关API
- `VirtualTryOnNetworkClient`：虚拟试衣相关API

所有NetworkClient采用单例模式，通过`TokenManager`统一管理Token。

#### 2.2.2 数据管理组件

**ViewModel + LiveData**
- 使用ViewModel存储UI相关的数据
- 使用LiveData实现响应式编程，自动更新UI
- ViewModel在Activity/Fragment生命周期中保持数据，避免数据丢失

**Repository模式**
- `OutfitRepository`：管理穿搭数据的获取和缓存
- `ProfileRepository`：管理用户资料数据
- `WeatherRepository`：管理天气数据和城市数据

#### 2.2.3 数据持久化组件

**Room Database**
- 用于存储城市数据（省市区三级联动）
- 使用`CityEntity`实体类，`CityDao`数据访问对象

**SharedPreferences**
- 使用`SharedPrefManager`封装SharedPreferences操作
- 存储用户Token、登录状态、用户名、位置信息等

#### 2.2.4 UI组件

**Material Design组件**
- `MaterialCardView`：卡片式布局
- `Chip` / `ChipGroup`：筛选标签
- `BottomNavigationView`：底部导航栏
- `SwipeRefreshLayout`：下拉刷新
- `DrawerLayout`：侧边抽屉筛选面板

**RecyclerView + Adapter**
- 使用`StaggeredGridLayoutManager`实现瀑布流布局
- 自定义`HomeAdapter`适配器，支持图片懒加载

**Glide图片加载库**
- 用于加载网络图片和本地图片
- 支持图片缓存和占位符

### 2.3 模块设计

#### 2.3.1 穿搭广场模块

**组件组成**：
- `HomeFragment`：主界面Fragment
- `HomeViewModel`：数据管理ViewModel
- `HomeAdapter`：RecyclerView适配器
- `OutfitRepository`：数据仓库
- `OutfitNetworkClient`：网络客户端

**数据流**：
```
用户操作 → HomeFragment → HomeViewModel → OutfitRepository → OutfitNetworkClient → API
                                                                    ↓
UI更新 ← LiveData ← HomeViewModel ← OutfitRepository ← 响应数据
```

#### 2.3.2 天气模块

**组件组成**：
- `WeatherFragment`：天气显示Fragment
- `WeatherViewModel`：天气数据管理
- `WeatherRepository`：天气数据仓库
- `CityPickerBottomSheetDialog`：城市选择对话框
- `AppDatabase` / `CityDao`：Room数据库

**数据流**：
```
定位/选择城市 → WeatherFragment → WeatherViewModel → WeatherRepository → 天气API
                                                                    ↓
UI更新 ← LiveData ← WeatherViewModel ← WeatherRepository ← 天气数据
```

#### 2.3.3 发布模块

**组件组成**：
- `PublishFragment`：发布界面Fragment
- `EditOutfitActivity`：编辑标签Activity
- `PublishViewModel`：发布数据管理
- `PublishNetworkClient`：发布网络客户端

**数据流**：
```
选择图片 → PublishFragment → 上传图片 → AI识别 → EditOutfitActivity → 编辑标签 → 发布
```

#### 2.3.4 虚拟试衣模块

**组件组成**：
- `VirtualTryOnFragment`：试衣界面Fragment
- `TryOnResultActivity`：结果展示Activity
- `VirtualTryOnViewModel`：试衣数据管理
- `VirtualTryOnNetworkClient`：试衣网络客户端

**数据流**：
```
上传人像 + 选择穿搭 → VirtualTryOnViewModel → 提交试衣任务 → 轮询任务状态 → 获取结果
```

## 3. 重点难点实现

### 3.1 Token统一管理

**问题**：多个NetworkClient需要共享Token，登录后需要同步到所有客户端。

**解决方案**：实现`TokenManager`单例类，统一管理所有NetworkClient的Token。

**关键代码**：
```java
public class TokenManager {
    public void setToken(String token) {
        // 保存到 SharedPreferences
        prefManager.setToken(token);
        
        // 同步到所有 NetworkClient
        AuthNetworkClient.getInstance().setToken(token);
        OutfitNetworkClient.setToken(token);
        PublishNetworkClient.setToken(token);
        VirtualTryOnNetworkClient.setToken(token);
    }
    
    public void restoreToken() {
        // 应用启动时恢复Token
        String token = prefManager.getToken();
        if (token != null && !token.isEmpty()) {
            setToken(token);
        }
    }
}
```

**实现要点**：
- 使用单例模式确保全局唯一
- 登录成功后调用`TokenManager.setToken()`
- 应用启动时调用`TokenManager.restoreToken()`
- 退出登录时调用`TokenManager.clearToken()`

### 3.2 天气自动筛选穿搭

**问题**：根据当前天气温度自动筛选适合的穿搭，需要将温度转换为天气类型（寒冷、凉爽、温暖、炎热）。

**解决方案**：在`HomeViewModel`中实现`applyWeatherFilter()`方法，根据温度自动设置天气筛选条件。

**关键代码**：
```java
public void applyWeatherFilter(WeatherResponse.NowWeather nowWeather) {
    String temp = nowWeather.getTemp();
    String weatherType = getWeatherTypeFromTemp(temp);
    
    if (weatherType != null) {
        Set<String> weatherSet = selectedFilters.get("weather");
        if (weatherSet != null) {
            // 避免重复加载
            if (weatherSet.size() == 1 && weatherSet.contains(weatherType)) {
                return;
            }
            
            weatherSet.clear();
            weatherSet.add(weatherType);
            
            // 重新加载数据
            loadDiscoverOutfits(0);
        }
    }
}

private String getWeatherTypeFromTemp(String temp) {
    double temperature = Double.parseDouble(temp.replace("°C", "").trim());
    
    if (temperature < 10) return "寒冷";
    else if (temperature < 20) return "凉爽";
    else if (temperature < 28) return "温暖";
    else return "炎热";
}
```

**实现要点**：
- 在`MainActivity`中预加载天气数据
- 观察天气数据变化，自动调用`applyWeatherFilter()`
- 避免重复加载：检查天气类型是否变化
- 保留其他筛选条件（如个人偏好）

### 3.3 个人偏好和性别筛选自动应用

**问题**：用户设置个人偏好和性别后，需要自动应用到穿搭广场的推荐中。性别筛选需要与个人偏好筛选组合使用。

**解决方案**：在`MainActivity`预加载个人资料后，同时应用用户性别筛选和个人偏好筛选。

**关键代码**：
```java
// MainActivity.java
profileViewModel.getProfile().observe(this, profile -> {
    if (profile != null) {
        // 同时应用用户性别筛选和个人偏好筛选
        boolean hasGender = profile.getGender() != null && !profile.getGender().isEmpty();
        boolean hasPreferences = profile.getPreferences() != null && 
            (preferences.getPreferredStyles() != null || ...);
        
        if (hasGender || hasPreferences) {
            // 先应用性别筛选（不重新加载）
            if (hasGender) {
                homeViewModel.applyUserGender(profile.getGender(), true);
            }
            // 再应用个人偏好筛选（会保留性别筛选，并触发加载）
            if (hasPreferences) {
                homeViewModel.applyUserPreferences(profile.getPreferences());
            } else if (hasGender) {
                homeViewModel.applyUserGender(profile.getGender(), false);
            }
        }
    }
});

// HomeViewModel.java
public void applyUserGender(String gender, boolean skipReload) {
    // 将UI显示的性别值（"男"、"女"、"其他"）映射为API需要的值（"male"、"female"、"unisex"）
    String sexValue = mapGenderToSex(gender);
    Set<String> sexSet = selectedFilters.get("sex");
    sexSet.clear();
    sexSet.add(sexValue);
    
    if (!skipReload) {
        loadDiscoverOutfits(0);
    }
}

public void applyUserPreferences(ProfileResponse.Preferences preferences) {
    // 保留天气筛选和性别筛选
    // 只清空其他筛选，然后应用个人偏好
    // 应用偏好风格、颜色、季节...
    loadDiscoverOutfits(0);
}
```

**实现要点**：
- 在`MainActivity`预加载个人资料后自动应用性别和偏好筛选
- 性别筛选优先应用，个人偏好筛选会保留性别筛选
- 保留天气筛选，组合应用性别筛选和个人偏好筛选
- 使用ViewModel共享，确保数据同步
- 游客模式不应用性别筛选，只应用天气筛选

### 3.4 虚拟试衣异步任务轮询

**问题**：虚拟试衣是异步任务，需要轮询任务状态直到完成。

**解决方案**：使用Handler实现定时轮询，观察任务状态变化。

**关键代码**：
```java
private void startPollingTaskStatus(String taskId) {
    stopPolling(); // 停止之前的轮询
    
    currentTaskId = taskId;
    pollingHandler = new Handler(Looper.getMainLooper());
    
    pollingRunnable = new Runnable() {
        @Override
        public void run() {
            if (currentTaskId != null && currentTaskId.equals(taskId)) {
                viewModel.checkTaskStatus(taskId, token);
                // 延迟2秒后继续轮询
                pollingHandler.postDelayed(this, 2000);
            }
        }
    };
    
    // 观察任务状态变化
    viewModel.getTaskStatus().observe(getViewLifecycleOwner(), status -> {
        if ("completed".equals(status)) {
            stopPolling();
        }
    });
    
    // 开始轮询
    pollingHandler.postDelayed(pollingRunnable, 2000);
}
```

**实现要点**：
- 使用Handler在主线程定时执行
- 观察任务状态，完成后停止轮询
- Fragment销毁时停止轮询，避免内存泄漏

### 3.5 筛选条件管理

**问题**：多个筛选条件（季节、场景、风格、类别、颜色、天气、性别）需要统一管理，支持组合筛选。

**解决方案**：使用`Map<String, Set<String>>`存储筛选条件，根据是否有筛选条件选择不同的API。

**关键代码**：
```java
private final Map<String, Set<String>> selectedFilters = new HashMap<>();
// 初始化筛选条件
selectedFilters.put("season", new HashSet<>());
selectedFilters.put("scene", new HashSet<>());
selectedFilters.put("style", new HashSet<>());
selectedFilters.put("category", new HashSet<>());
selectedFilters.put("color", new HashSet<>());
selectedFilters.put("weather", new HashSet<>());
selectedFilters.put("sex", new HashSet<>()); // 性别筛选

public void loadDiscoverOutfits(int offset) {
    if (hasFilters()) {
        // 有筛选条件，使用 /api/discover/outfits 接口
        loadWithFilters(offset);
    } else {
        // 没有筛选条件，使用 /api/outfits 接口
        loadDefaultOutfits(offset);
    }
}

private String buildFilterString(Set<String> filterSet, String filterKey) {
    // 将筛选值映射为API需要的格式
    List<String> mappedValues = new ArrayList<>();
    for (String value : filterSet) {
        String mapped = mapFilterValue(filterKey, value);
        mappedValues.add(mapped);
    }
    return String.join(",", mappedValues);
}

// 性别值映射（UI显示值 → API值）
private String mapFilterValue(String filterKey, String value) {
    if ("sex".equals(filterKey)) {
        if ("男".equals(value)) return "male";
        if ("女".equals(value)) return "female";
        if ("中性".equals(value)) return "unisex";
        return value; // 如果已经是API格式，直接返回
    }
    // 其他筛选条件的映射...
}
```

**实现要点**：
- 使用Set存储每个筛选类型的多个值
- 筛选值需要映射（如"春季"→"春"，"男"→"male"）
- 有筛选条件时使用发现接口，无筛选条件时使用默认接口
- 性别筛选支持三种值：male（男）、female（女）、unisex（中性）

### 3.6 性别信息显示和JSON数据兼容

**问题**：API返回的数据中，`raw_tags`字段的`occasion`、`season`、`weather`等字段可能返回字符串或数组两种格式，导致JSON解析失败。同时，穿搭列表需要显示性别标签。

**解决方案**：
1. 创建`StringListTypeAdapter`处理字符串/数组兼容问题
2. 在数据转换时将sex字段转换为中文显示

**关键代码**：
```java
// StringListTypeAdapter.java - 处理字符串或数组两种格式
public class StringListTypeAdapter extends TypeAdapter<List<String>> {
    @Override
    public List<String> read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.BEGIN_ARRAY) {
            // 数组格式
            in.beginArray();
            while (in.hasNext()) {
                result.add(in.nextString());
            }
            in.endArray();
        } else if (in.peek() == JsonToken.STRING) {
            // 字符串格式，转换为数组
            String value = in.nextString();
            if (value != null && !value.isEmpty()) {
                result.add(value);
            }
        }
        return result;
    }
}

// OutfitTags.java - 使用TypeAdapter
@SerializedName("occasion")
@JsonAdapter(StringListTypeAdapter.class)
private List<String> occasion;

@SerializedName("season")
@JsonAdapter(StringListTypeAdapter.class)
private List<String> season;

@SerializedName("weather")
@JsonAdapter(StringListTypeAdapter.class)
private List<String> weather;

// HomeViewModel.java - 显示性别标签
private String getSexDisplayText(OutfitListResponse.OutfitListItem item) {
    String sex = item.getSex(); // 优先使用item的sex字段
    if (sex == null) {
        // 如果为空，从tags中获取
        sex = item.getTags().getSex().get(0);
    }
    return mapSexToDisplay(sex); // 转换为中文：male→男，female→女，unisex→中性
}
```

**实现要点**：
- 使用`@JsonAdapter`注解为可能有两种格式的字段指定TypeAdapter
- 在`toDisplayModelFromListItem`方法中将sex字段转换为中文显示
- 支持从`item.getSex()`或`tags.getSex()`获取性别信息

### 3.7 城市数据加载和管理

**问题**：需要支持全国城市数据，实现省市区三级联动选择。

**解决方案**：从GitHub加载城市JSON数据，使用Room数据库存储，实现快速查询。

**关键代码**：
```java
// 从GitHub加载城市数据
private void loadCityDataFromUrl(String url) {
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(url).build();
    
    client.newCall(request).enqueue(new Callback() {
        @Override
        public void onResponse(Call call, Response response) {
            String jsonData = response.body().string();
            viewModel.loadCitiesFromJson(jsonData, callback);
        }
    });
}

// Room数据库查询
@Query("SELECT DISTINCT province FROM cities ORDER BY province")
LiveData<List<String>> getAllProvinces();

@Query("SELECT DISTINCT city FROM cities WHERE province = :province ORDER BY city")
LiveData<List<String>> getCitiesByProvince(String province);
```

**实现要点**：
- 首次启动时从网络加载城市数据
- 使用Room数据库持久化存储
- 实现省市区三级联动查询
- 支持备用数据源，提高可靠性

## 4. 系统实现

### 4.1 用户认证模块

**功能**：用户注册、登录、密码找回

**实现截图说明**：
- 启动页面：显示"开始"按钮，支持游客模式和登录/注册
- 登录页面：输入邮箱和密码，支持忘记密码
- 注册页面：输入用户名、邮箱、密码和确认密码
- 忘记密码页面：输入邮箱，发送验证码，重置密码

**关键技术**：
- 使用Retrofit进行API调用
- Token自动管理，登录成功后保存Token
- 支持游客模式，无需登录即可浏览

### 4.2 穿搭广场模块

**功能**：浏览穿搭、搜索筛选、收藏管理

**实现截图说明**：
- 主界面：瀑布流展示穿搭图片，显示标签、作者、点赞数
- 筛选面板：侧边抽屉，支持季节、场景、风格、类别、颜色筛选
- 搜索功能：顶部搜索框，支持关键词搜索
- 收藏功能：点击心形图标收藏/取消收藏

**关键技术**：
- StaggeredGridLayoutManager实现瀑布流
- 多维度筛选，支持组合条件（季节、场景、风格、类别、颜色、天气、性别）
- 下拉刷新和分页加载
- 天气自动筛选、性别筛选和个人偏好应用
- 穿搭卡片显示性别标签（男/女/中性）

### 4.3 天气服务模块

**功能**：天气查询、城市选择、穿搭建议

**实现截图说明**：
- 天气主界面：显示当前位置、当前温度、天气状况
- 24小时预报：横向滚动显示未来24小时天气
- 7天预报：显示未来7天天气趋势
- 穿搭建议：根据当前天气生成穿搭建议
- 城市选择：省市区三级联动选择城市

**关键技术**：
- 和风天气API获取天气数据
- Room数据库存储城市数据
- Google Play Services定位服务
- 根据温度自动筛选穿搭

### 4.4 内容发布模块

**功能**：图片上传、AI识别、标签编辑、发布穿搭

**实现截图说明**：
- 发布界面：点击上传区域选择图片
- 上传进度：显示上传进度条
- 编辑界面：显示AI识别的标签，支持修改
- 发布结果：显示发布成功或失败

**关键技术**：
- 图片选择器（ActivityResultLauncher）
- 文件上传（Multipart）
- AI标签识别和编辑
- 标签数据映射和验证
- 性别标签编辑（支持male/female/unisex）
- 标签更新接口支持sex字段

### 4.5 虚拟试衣模块

**功能**：人像上传、穿搭选择、虚拟试衣

**实现截图说明**：
- 试衣界面：上传人像图片，选择收藏的穿搭
- 加载状态：显示试衣进度
- 结果展示：显示试衣后的效果图

**关键技术**：
- 异步任务提交和状态轮询
- Handler定时轮询任务状态
- 图片处理和展示

### 4.6 个人中心模块

**功能**：个人资料、我的收藏、我的穿搭、偏好设置

**实现截图说明**：
- 个人中心：显示用户名、头像（待实现）
- 我的收藏：列表展示所有收藏的穿搭
- 我的穿搭：列表展示自己发布的所有穿搭
- 偏好设置：对话框设置风格、颜色、季节偏好

**关键技术**：
- 个人资料API调用
- 收藏和穿搭列表展示
- 偏好设置和应用

## 5. 版本兼容问题

### 5.1 Android版本兼容

**目标版本**：
- `minSdk = 29` (Android 10)
- `targetSdk = 36` (Android 14+)
- `compileSdk = 36`

### 5.2 存储权限兼容

**问题**：Android 13 (API 33) 及以上版本，存储权限从`READ_EXTERNAL_STORAGE`改为`READ_MEDIA_IMAGES`。

**解决方案**：根据SDK版本动态选择权限。

**关键代码**：
```java
private void requestStoragePermissionAndPickImage() {
    String permission;
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        // Android 13+ 使用新权限
        permission = Manifest.permission.READ_MEDIA_IMAGES;
    } else {
        // Android 12及以下使用旧权限
        permission = Manifest.permission.READ_EXTERNAL_STORAGE;
    }
    
    if (ContextCompat.checkSelfPermission(requireContext(), permission)
            != PackageManager.PERMISSION_GRANTED) {
        requestPermissionLauncher.launch(permission);
    } else {
        pickImageFromGallery();
    }
}
```

**AndroidManifest.xml配置**：
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="28" />
```

### 5.3 定位权限处理

**问题**：需要获取用户位置用于天气查询，但不同版本权限处理方式不同。

**解决方案**：运行时请求权限，权限被拒绝时使用默认位置。

**关键代码**：
```java
private void getCurrentLocation() {
    // 检查权限
    if (ContextCompat.checkSelfPermission(requireContext(), 
            Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
        // 请求权限
        requestPermissions(new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        }, LOCATION_PERMISSION_REQUEST_CODE);
        return;
    }
    
    // 获取位置
    fusedLocationClient.getLastLocation()
        .addOnSuccessListener(location -> {
            if (location != null) {
                handleLocation(location);
            } else {
                // 使用默认位置（北京）
                useDefaultLocation();
            }
        });
}

@Override
public void onRequestPermissionsResult(int requestCode, 
        @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
        if (grantResults.length > 0 && 
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            // 权限被拒绝，使用默认位置
            useDefaultLocation();
        }
    }
}
```

### 5.4 网络请求兼容

**问题**：Android 9 (API 28) 及以上版本默认禁止HTTP明文传输。

**解决方案**：在AndroidManifest.xml中允许明文传输（仅用于开发环境）。

**AndroidManifest.xml配置**：
```xml
<application
    android:usesCleartextTraffic="true"
    ...>
</application>
```

**注意**：生产环境应使用HTTPS，或配置网络安全配置。

### 5.5 ActivityResultLauncher兼容

**问题**：AndroidX Activity 1.2.0+ 推荐使用`ActivityResultLauncher`替代`startActivityForResult()`。

**解决方案**：使用`ActivityResultLauncher`处理图片选择和权限请求。

**关键代码**：
```java
// 注册图片选择器
imagePickerLauncher = registerForActivityResult(
    new ActivityResultContracts.StartActivityForResult(),
    result -> {
        if (result.getResultCode() == Activity.RESULT_OK && 
            result.getData() != null) {
            selectedImageUri = result.getData().getData();
            displaySelectedImage();
        }
    }
);

// 注册权限请求
requestPermissionLauncher = registerForActivityResult(
    new ActivityResultContracts.RequestPermission(),
    isGranted -> {
        if (isGranted) {
            pickImageFromGallery();
        } else {
            Toast.makeText(getContext(), "需要存储权限", 
                Toast.LENGTH_SHORT).show();
        }
    }
);
```

## 6. 课程心得

### 6.1 实践心得

在完成OutfitChanges系统的开发过程中，我深刻体会到了Android开发的复杂性和系统性。主要收获如下：

1. **架构设计的重要性**：采用MVVM架构模式，将业务逻辑与UI分离，使代码结构清晰，易于维护和测试。ViewModel的生命周期管理确保了数据在配置变更时不会丢失。

2. **网络请求的统一管理**：通过实现TokenManager统一管理所有NetworkClient的Token，避免了Token不同步的问题。使用Retrofit和OkHttp简化了网络请求的实现。

3. **响应式编程的优势**：使用LiveData实现响应式编程，当数据变化时UI自动更新，减少了手动更新UI的代码，提高了开发效率。

4. **版本兼容的挑战**：Android系统版本众多，不同版本的API和行为差异较大。通过条件判断和适配，确保了应用在不同版本上的正常运行。

5. **异步任务的处理**：虚拟试衣功能需要异步处理，通过Handler实现任务状态轮询，学会了如何优雅地处理长时间运行的异步任务。

6. **用户体验的优化**：实现了下拉刷新、分页加载、自动筛选等功能，提升了用户体验。预加载数据、缓存机制等优化措施提高了应用的响应速度。

### 6.2 对本课程的建议

1. **理论与实践结合**：希望课程能够更多地结合实际项目案例，让学生在实践中学习理论知识，加深理解。

2. **版本兼容性讲解**：Android版本更新频繁，建议增加版本兼容性相关的讲解，帮助学生更好地处理不同版本的适配问题。

3. **架构模式深入**：MVVM、MVP、MVI等架构模式各有优劣，建议深入讲解各种架构模式的适用场景和实现细节。

4. **性能优化专题**：Android应用性能优化是一个重要话题，建议增加性能优化相关的专题，包括内存优化、网络优化、UI优化等。

5. **测试驱动开发**：建议引入单元测试和UI测试的内容，培养学生测试驱动开发的意识。

6. **代码审查机制**：建议建立代码审查机制，通过代码审查帮助学生发现和改正问题，提高代码质量。

### 6.3 对老师的建议

1. **项目指导**：希望老师能够在项目开发过程中提供更多的指导和帮助，特别是在遇到技术难点时。

2. **代码示例**：希望老师能够提供更多的代码示例和最佳实践，帮助学生更好地理解和应用知识。

3. **技术分享**：建议定期组织技术分享会，让学生分享自己的开发经验和遇到的问题，促进学习交流。

4. **反馈机制**：建议建立更完善的反馈机制，及时了解学生的学习情况和遇到的问题，提供针对性的帮助。

5. **资源推荐**：希望老师能够推荐一些优质的学习资源和工具，帮助学生更好地学习和开发。

---

## 附录

### 技术栈

- **开发语言**：Java
- **最低支持版本**：Android 10 (API 29)
- **目标版本**：Android 14+ (API 36)
- **架构模式**：MVVM
- **网络库**：Retrofit 2.9.0 + OkHttp 4.12.0
- **图片加载**：Glide 4.16.0
- **数据库**：Room 2.6.1
- **UI框架**：Material Design Components
- **依赖注入**：无（可考虑引入Dagger/Hilt）

### 项目结构

```
app/src/main/java/com/example/outfitchanges/
├── auth/                    # 用户认证模块
│   ├── AuthViewModel.java
│   ├── LoginActivity.java
│   ├── RegisterActivity.java
│   ├── network/             # 网络请求
│   └── model/              # 数据模型
├── ui/                      # UI模块
│   ├── home/               # 穿搭广场
│   ├── weather/            # 天气服务
│   ├── publish/            # 内容发布
│   ├── virtual/            # 虚拟试衣
│   └── profile/            # 个人中心
├── utils/                   # 工具类
│   ├── TokenManager.java
│   └── SharedPrefManager.java
├── MainActivity.java        # 主Activity
└── StartActivity.java      # 启动Activity
```

### 主要API接口

- **用户认证**：`/api/auth/login`, `/api/auth/register`
- **穿搭列表**：`/api/outfits`, `/api/discover/outfits?season=...&weather=...&sex=...`（支持性别筛选参数）
- **收藏管理**：`/api/outfits/{id}/favorite`
- **天气查询**：和风天气API
- **图片上传**：`POST /api/outfits`（multipart/form-data，支持modified_tags包含sex字段）
- **更新穿搭**：`PUT /api/outfits/{id}`（支持tags中包含sex字段）
- **虚拟试衣**：`/api/virtual-tryon/submit`

**API筛选参数说明**：
- `sex`：性别筛选，支持 `male`（男）、`female`（女）、`unisex`（中性），可多个值用逗号分隔
- 游客模式：调用 `/api/discover/outfits` 时不传token（使用X-Skip-Auth header）
- 登录用户：调用 `/api/discover/outfits` 时自动携带token进行认证

---

**项目完成时间**：2024年

**开发者**：[您的姓名]

**联系方式**：[您的邮箱]

