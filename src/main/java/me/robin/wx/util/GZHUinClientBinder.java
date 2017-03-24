package me.robin.wx.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by xuanlubin on 2017/3/24.
 */
public class GZHUinClientBinder {
    private static final Map<String, String> uinMapClient = new ConcurrentHashMap<>();
    private static final Map<String, String> clientMapUin = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> hisRequestLock = new ConcurrentHashMap<>();


    public static void bind(String client, String uin) {
        if (StringUtils.isBlank(uin)) {
            return;
        }
        uinMapClient.put(uin, client);
        clientMapUin.put(client, uin);
    }

    public static String getClient(String uin) {
        if (StringUtils.isBlank(uin)) {
            return null;
        }
        return uinMapClient.get(uin);
    }

    public static String getUin(String client) {
        if (StringUtils.isBlank(client)) {
            return null;
        }
        return clientMapUin.get(client);
    }

    public static void lock(String uin) {
        if (StringUtils.isBlank(uin)) {
            return;
        }
        hisRequestLock.put(uin, true);
    }

    public static boolean isLocked(String client) {
        String uin = getUin(client);
        return null != uin && hisRequestLock.containsKey(uin);
    }

    public static void release(String uin) {
        if (StringUtils.isBlank(uin)) {
            return;
        }
        hisRequestLock.remove(uin);
    }
}
