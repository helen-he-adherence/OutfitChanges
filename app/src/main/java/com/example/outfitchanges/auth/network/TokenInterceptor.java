//自动为请求添加 Authorization: Bearer <token>
//在 App 发起的每一个网络请求中，自动加上用户的登录凭证（即 Authorization: Bearer <token> 头部），除非明确不需要。
package com.example.outfitchanges.auth.network;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
//Interceptor：OkHttp 提供的拦截器接口，允许你在请求发出前或响应返回后“拦截”并修改它。
//Request / Response：代表 HTTP 请求和响应对象。
//IOException：网络操作可能抛出的异常。

/**
 * 这是一个自定义拦截器，实现了 OkHttp 的 Interceptor 接口。
 * 目标：自动注入认证 Token，避免每次调用 API 都手动加 header
 */
public class TokenInterceptor implements Interceptor {
    private String token; //存储当前用户的登录令牌（比如 JWT）。初始为 null，表示未登录。

    public TokenInterceptor() {
        this.token = null;
    }//初始化时，Token 为空（未登录状态）。

    public void setToken(String token) {
        this.token = token;
    }//登录成功后，把服务器返回的 token 保存进来；
    public void clearToken() {
        this.token = null;
    } //用户退出登录时清空 token；

    public String getToken() {
        return token;
    } //供外部读取当前 token（调试或特殊用途）。

    @Override
    public Response intercept(Chain chain) throws IOException {
//  这是拦截器的核心，每次发起网络请求都会执行这里：
        Request original = chain.request(); //chain.request()：拿到即将发出的原始 HTTP 请求。
        
        // 如果请求已经包含 Authorization header，则不添加
        if (original.header("Authorization") != null) {
            return chain.proceed(original);
        }

        // 如果请求标记了 X-Skip-Auth: true，跳过认证
        //// 示例：在 Repository 中调用登录时不带 token
        //Request.Builder builder = new Request.Builder()
        //    .url("https://...")
        //    .header("X-Skip-Auth", "true");
        //或者
        //@Headers("X-Skip-Auth: true")
        //@POST("api/auth/login")
        //Call<LoginResponse> login(@Body LoginRequest request);
        if ("true".equalsIgnoreCase(original.header("X-Skip-Auth"))) {
            return chain.proceed(original);
        }

        String url = original.url().toString();
        
        // 对于公开端点（登录可选），如果服务器在处理带 token 的请求时有问题，
        // 可以选择不添加 token。但为了获取个性化内容，我们仍然尝试添加 token。
        // 如果遇到 500 错误，可以在 Repository 层进行重试（不带 token）
        
        // 如果有 token，添加到请求头
        if (token != null && !token.isEmpty()) {
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Authorization", "Bearer " + token);
            return chain.proceed(requestBuilder.build());
        }
        //如果没有 token，就直接发送原始请求
        return chain.proceed(original);
    }
}

