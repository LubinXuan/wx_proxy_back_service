package me.robin.wx.util;

import okhttp3.Response;

import java.io.IOException;

/**
 * Created by xuanlubin on 2017/3/27.
 */
public class GZHResponceReadUtil {
    public static String read(Response response) throws IOException {
        if ("deflate".equalsIgnoreCase(response.header("Content-Encoding"))) {
            return InflaterUtil.tranInflaterInputStream(response.body().bytes());
        } else {
            return response.body().string();
        }
    }
}
