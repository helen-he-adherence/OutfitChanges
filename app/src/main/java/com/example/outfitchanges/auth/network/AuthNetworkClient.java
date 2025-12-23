//创建并管理 Retrofit 实例，提供全局访问入口
//“App 与后端服务器之间的唯一官方联络人”。
package com.example.outfitchanges.auth.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;
//OkHttpClient：底层 HTTP 客户端，Retrofit 基于它；
//HttpLoggingInterceptor：用于打印请求/响应日志（开发调试神器）；
//Retrofit + GsonConverterFactory：将 JSON 自动转为 Java 对象（反之亦然）；
//TimeUnit：设置超时时间单位（秒）。

public class AuthNetworkClient {
    private static final String BASE_URL = "https://luckyhe.fun/"; //后端 API 的根地址（必须以 / 结尾，Retrofit 要求）
    private static AuthNetworkClient instance; //用于实现单例模式（全局只有一个实例）
    private final AuthApiService apiService; //由 Retrofit 生成的 AuthApiService 实现对象
    private final TokenInterceptor tokenInterceptor; //用于动态管理登录 Token
    private Retrofit retrofit; //Retrofit 核心实例，负责网络请求调度

    private AuthNetworkClient() {
        //1.创建日志拦截器
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        //打印完整的请求 URL、Header、Body 和响应内容；
        //仅用于开发阶段！发布版本应移除或设为 NONE（避免泄露敏感信息）。
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        //2.创建 Token 拦截器  //用于自动注入 Authorization: Bearer <token>
        tokenInterceptor = new TokenInterceptor();

        //3. 构建 OkHttpClient
        //拦截器顺序很重要！
        //TokenInterceptor 在前：先加 Token；
        //LoggingInterceptor 在后：能打印出带 Token 的完整请求（方便调试）。
        //超时设置：30 秒连接/读/写超时，避免用户长时间等待。
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(tokenInterceptor) // 先添加 token 拦截器
                .addInterceptor(logging) // 再添加日志拦截器
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();


        //4. 构建 Retrofit
        //指定 HTTPS 地址（安全！）；
        //使用自定义的 OkHttpClient（带拦截器）
        //自动用 Gson 解析 JSON。
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // 5. 创建 API 服务接口
        //Retrofit 在运行时动态生成 AuthApiService 的实现类；
        //你调用 apiService.login(...) 时，背后是 Retrofit + OkHttp 发起真实网络请求。
        apiService = retrofit.create(AuthApiService.class);
    }

    //单例模式
    //避免重复创建 Retrofit、OkHttpClient（它们是重量级对象）；
    //确保全局使用同一个 TokenInterceptor（Token 状态一致）；
    //节省内存，提升性能。
    public static synchronized AuthNetworkClient getInstance() {
        if (instance == null) {
            instance = new AuthNetworkClient();
        }
        return instance;
    }

    //对外提供的方法
    //1.获取API服务 业务代码通过它调用所有接口：AuthNetworkClient.getInstance().getApiService().login(request);
    public AuthApiService getApiService() {
        return apiService;
    }

    //设置 token，后续请求会自动添加 Authorization header
    //登录成功后，把服务器返回的 token 保存到拦截器中；
    //后续所有需要认证的请求自动带上 token。
    public void setToken(String token) {
        tokenInterceptor.setToken(token);
    }

    // 清除 token
    public void clearToken() {
        tokenInterceptor.clearToken();
    }
}

