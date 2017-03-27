package me.robin.wx.util;

import okhttp3.Cookie;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xuanlubin on 2017/3/27.
 */
public class GZHUinCookieStore {
    private static final Map<String, Map<String, String>> uinCookieMap = new ConcurrentHashMap<>();

    public static boolean hasUin(String uin) {
        return uinCookieMap.containsKey(uin);
    }

    public static Map<String, String> getUinCookie(String uin) {
        return uinCookieMap.get(uin);
    }

    public static void deleteUinCookie(String uin) {
        uinCookieMap.remove(uin);
    }

    public static void saveUinCookie(String uin,List<Cookie> cookies){
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

    public static Map<String, String> randomUinCookie() {
        Optional<Map<String, String>> optional = uinCookieMap.values().stream().findAny();
        return optional.isPresent() ? optional.get() : null;
    }

    public static String buildCookie(Map<String, String> _cookieMap){
        StringBuilder _cookie = new StringBuilder();
        for (Map.Entry<String, String> entry : _cookieMap.entrySet()) {
            if (_cookie.length() > 0) {
                _cookie.append(";");
            }
            _cookie.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return _cookie.toString();
    }
}
