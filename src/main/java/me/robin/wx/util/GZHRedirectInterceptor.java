package me.robin.wx.util;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

/**
 * Created by xuanlubin on 2017/3/23.
 * 公众号UinCookie处理
 */
public class GZHRedirectInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(GZHRedirectInterceptor.class);

    @Override
    public Response intercept(Chain chain) throws IOException {
        Object tag = chain.request().tag();
        Request request = chain.request();
        if ((tag instanceof Request) && !request.url().toString().contains("key=")) {
            logger.info("公众号请求重定向:{} -> {}", ((Request) tag).url(), request.url());
            Request.Builder builder = request.newBuilder();
            HttpUrl.Builder urlBuilder = request.url().newBuilder();
            HttpUrl originalUrl = ((Request) tag).url();
            Set<String> parameterNames = originalUrl.queryParameterNames();
            for (String parameterName : parameterNames) {
                if ("__biz".equals(parameterName)) {
                    continue;
                }
                String value = originalUrl.queryParameter(parameterName);
                urlBuilder.addQueryParameter(parameterName, value);
            }
            return chain.proceed(builder.url(urlBuilder.build()).build());
        } else {
            return chain.proceed(chain.request());
        }
    }

}
