package me.robin.wx.util;

import me.robin.wx.service.GZHAnalyse;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(GZHUinCookieInterceptor.class);

    private final Map<String, Map<String, String>> uinCookieMap = new ConcurrentHashMap<>();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl httpUrl = request.url();
        String uin = GZHAnalyse.getUinFromUrl(httpUrl.query());
        Response response;
        if (StringUtils.isNotBlank(uin)) {
            if (uinCookieMap.containsKey(uin)) {
                Request.Builder requestBuilder = request.newBuilder();
                String cookie = request.header("cookie");
                Map<String, String> cookieMap = uinCookieMap.get(uin);
                Map<String, String> _cookieMap = new HashMap<>();
                if (StringUtils.isNotBlank(cookie)) {
                    String[] cookies = StringUtils.split(cookie, ";");
                    for (String str : cookies) {
                        String[] kv = StringUtils.split(str, "=", 2);
                        _cookieMap.put(kv[0], kv[1]);
                    }
                }
                _cookieMap.putAll(cookieMap);

                StringBuilder _cookie = new StringBuilder();
                for (Map.Entry<String, String> entry : _cookieMap.entrySet()) {
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
                logger.info("find new cookie for uin:{}", uin);
                GZHUinClientBinder.release(uin);
            }
        } else {
            response = chain.proceed(chain.request());
        }
        return response;
    }
}
