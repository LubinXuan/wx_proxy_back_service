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

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl httpUrl = request.url();
        String uin = GZHAnalyse.getUinFromUrl(httpUrl);
        Response response;
        if (StringUtils.isNotBlank(uin)) {
            if (GZHUinCookieStore.hasUin(uin)) {
                Request.Builder requestBuilder = request.newBuilder();
                String cookie = request.header("cookie");
                Map<String, String> cookieMap = GZHUinCookieStore.getUinCookie(uin);
                Map<String, String> _cookieMap = new HashMap<>();
                if (StringUtils.isNotBlank(cookie)) {
                    String[] cookies = StringUtils.split(cookie, ";");
                    for (String str : cookies) {
                        String[] kv = StringUtils.split(str, "=", 2);
                        _cookieMap.put(kv[0], kv[1]);
                    }
                }
                _cookieMap.putAll(cookieMap);
                requestBuilder.header("cookie", GZHUinCookieStore.buildCookie(_cookieMap));
                response = chain.proceed(requestBuilder.build());
            } else {
                response = chain.proceed(chain.request());
            }
            List<Cookie> cookies = Cookie.parseAll(httpUrl, response.headers());
            if (!cookies.isEmpty()) {
                GZHUinCookieStore.saveUinCookie(uin, cookies);
                logger.info("find new cookie for uin:{}", uin);
                GZHUinClientBinder.release(uin);
            }
        } else {
            response = chain.proceed(chain.request());
        }
        return response;
    }

    public void clear(String uin) {
        GZHUinCookieStore.deleteUinCookie(uin);
    }
}
