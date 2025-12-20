package com.example.outfitchanges.ui.home.network;

import com.example.outfitchanges.auth.network.TokenInterceptor;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

/**
 * 简单的网络客户端，暂时容忍自签名证书，便于拉取自建服务数据。
 * 如需上线请替换为正规证书并移除不安全配置。
 */
public class OutfitNetworkClient {

    private static final String BASE_URL = "https://luckyhe.fun/";
    private static Retrofit retrofit;
    private static TokenInterceptor tokenInterceptor = new TokenInterceptor();

    private OutfitNetworkClient() {
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(buildUnsafeClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    
    /**
     * 设置 token，后续请求会自动添加 Authorization header
     */
    public static void setToken(String token) {
        tokenInterceptor.setToken(token);
    }

    /**
     * 清除 token
     */
    public static void clearToken() {
        tokenInterceptor.clearToken();
    }

    /**
     * 获取当前 token（用于临时保存和恢复）
     */
    public static String getToken() {
        return tokenInterceptor.getToken();
    }

    private static OkHttpClient buildUnsafeClient() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            X509TrustManager trustManager = (X509TrustManager) trustAllCerts[0];

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            HostnameVerifier allHostsValid = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                    .hostnameVerifier(allHostsValid)
                    .addInterceptor(tokenInterceptor) // 先添加 token 拦截器
                    .addInterceptor(loggingInterceptor) // 再添加日志拦截器
                    .connectTimeout(30, TimeUnit.SECONDS) // 连接超时30秒
                    .readTimeout(30, TimeUnit.SECONDS) // 读取超时30秒
                    .writeTimeout(30, TimeUnit.SECONDS) // 写入超时30秒
                    .build();
        } catch (Exception e) {
            return new OkHttpClient();
        }
    }
}

