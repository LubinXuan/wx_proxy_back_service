package me.robin.wx.util;

import okhttp3.Request;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by xuanlubin on 2017/3/23.
 */
public class GZHHistoryUrl {

    public static final String GZH_MP_HOST = "https://mp.weixin.qq.com";

    private static final String pattern = "/mp/getmasssendmsg?__biz={__biz}&uin={uin}&key={key}&f=json&frommsgid={frommsgid}&count=10&uin={uin}&key={key}&pass_ticket={pass_ticket}&wxtoken=&x5=1&f=json";
    private static final String refer = " https://mp.weixin.qq.com/mp/getmasssendmsg?__biz={__biz}&uin={uin}&key={key}&devicetype=android-23&version=26050630&lang=zh_CN&nettype=WIFI&ascene=7&pass_ticket={pass_ticket}&wx_header=1";

    public static Request.Builder process(String url) {
        String queryString = StringUtils.substringAfter(url, "?");
        url = pattern;
        String ref = refer;
        String paramPairArr[] = StringUtils.split(queryString, "&");
        for (String paramPair : paramPairArr) {
            String[] p = StringUtils.split(paramPair, "=", 2);
            url = StringUtils.replace(url, "{" + p[0] + "}", p.length == 2 ? p[1] : "");
            ref = StringUtils.replace(ref, "{" + p[0] + "}", p.length == 2 ? p[1] : "");
        }
        Request.Builder builder = new Request.Builder().url(GZH_MP_HOST+url);
        builder.addHeader("referer",ref);
        builder.addHeader("x-requested-with", "XMLHttpRequest");
        return builder;
    }
}
