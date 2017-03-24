package me.robin.wx.util;

import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xuanlubin on 2017/3/23.
 * 公众号UinCookie处理
 */
public class GZHUinCookieInterceptor implements Interceptor {
    private final Map<String, Map<String, String>> uinCookieMap = new ConcurrentHashMap<>();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl httpUrl = request.url();
        String uin = StringUtils.substringBetween(httpUrl.query(), "uin=", "&");
        Response response;
        if (StringUtils.isNotBlank(uin)) {
            if (uinCookieMap.containsKey(uin)) {
                Request.Builder requestBuilder = request.newBuilder();
                String cookie = request.header("cookie");
                Map<String, String> cookieMap = uinCookieMap.get(uin);
                StringBuilder _cookie = new StringBuilder();
                if (StringUtils.isNotBlank(cookie)) {
                    _cookie.append(cookie);
                }
                for (Map.Entry<String, String> entry : cookieMap.entrySet()) {
                    if (_cookie.length() > 0) {
                        _cookie.append(";");
                    }
                    _cookie.append(entry.getKey()).append("=").append(entry.getValue());
                }
                requestBuilder.header("cookie", _cookie.toString());
                response = chain.proceed(requestBuilder.build());
            } else {
                response = chain.proceed(chain.request());
            }
            List<Cookie> cookies = Cookie.parseAll(httpUrl, response.headers());
            if (!cookies.isEmpty()) {
                for (Cookie cookie : cookies) {
                    uinCookieMap.compute(uin, (s, stringStringMap) -> {
                        if (null == stringStringMap) {
                            stringStringMap = new HashMap<>();
                        }
                        stringStringMap.put(cookie.name(), cookie.value());
                        return stringStringMap;
                    });
                }
            }
        } else {
            response = chain.proceed(chain.request());
        }
        return response;
    }
}
