package me.robin.wx.service;

import me.robin.wx.util.GZHResponceReadUtil;
import me.robin.wx.util.GZHUinCookieStore;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuanlubin on 2017/3/27.
 */
public class GZHCountPatchService {
    //https://mp.weixin.qq.com/mp/getappmsgext?__biz=MTIzNDg3NzY2MA==&mid=2653018969&sn=0de347db5d32d68142ff8a9db8af51eb&idx=1&scene=0&devicetype=android-23&f=json&x5=1&f=json&is_need_ad=0

    private OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .build();

    public void patch(String url, Callback callback) {
        Map<String, String> cookie = GZHUinCookieStore.randomUinCookie();
        if (null != cookie) {
            patch(url, GZHUinCookieStore.buildCookie(cookie), callback);
        }
    }


    public void patch(String url, String cookie, Callback callback) {

        HttpUrl gzhContentUrl = HttpUrl.parse(url);

        HttpUrl.Builder urlBuilder = new HttpUrl.Builder();
        urlBuilder.scheme(gzhContentUrl.scheme());
        urlBuilder.host(gzhContentUrl.host());
        urlBuilder.encodedPath("/mp/getappmsgext");
        urlBuilder.addEncodedQueryParameter("__biz", gzhContentUrl.queryParameter("__biz"));
        urlBuilder.addEncodedQueryParameter("mid", gzhContentUrl.queryParameter("mid"));
        urlBuilder.addEncodedQueryParameter("sn", gzhContentUrl.queryParameter("sn"));
        urlBuilder.addEncodedQueryParameter("idx", gzhContentUrl.queryParameter("idx"));
        urlBuilder.addEncodedQueryParameter("is_need_ad", "0");

        Request.Builder requestBuilder = new Request.Builder().url(urlBuilder.build());
        requestBuilder.header("referer", url);
        requestBuilder.header("Content-Type", "application/x-www-form-urlencoded");
        requestBuilder.header("user-agent", "Mozilla/5.0 (Linux; Android 6.0.1; MI NOTE LTE Build/MMB29M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043115 Safari/537.36 MicroMessenger/6.5.6.1020 NetType/WIFI Language/zh_CN");
        requestBuilder.header("cookie", cookie);

        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("is_only_read", "1");

        client.newCall(requestBuilder.post(formBuilder.build()).build()).enqueue(callback);
    }


    public static void main(String[] args) throws InterruptedException {
        GZHCountPatchService patchService = new GZHCountPatchService();
        String url = "http://mp.weixin.qq.com/s?__biz=MTIzNDg3NzY2MA==&mid=2653018969&idx=1&sn=0de347db5d32d68142ff8a9db8af51eb&scene=0";
        String cookie = "wap_sid=CKiyxfQCEkAzX01wOGNWUVVNeUxUUDNGTXNRS2hOQ1Q3UU1DVHJTemhQMTZYNXlodzY3ZmtIZ1ktTlEyRUNKcFVPZGxfR2Z5GAQg/BEo3PnqzAQw06fjxgU=; wap_sid2=CKiyxfQCElxEM1gxUjh6T3hnZ0xoSnNxdW5GcUU4ekVzc2ZVUmNMQWJjNUZRMm40VGtISlVoYUJieEtmMk1IQThkTXJuQm9NT1FWVzYzY1ROaHZaSEhIZnVFb0QtWUVEQUFBfg==";
        patchService.patch(url, cookie, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.exit(-1);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println(GZHResponceReadUtil.read(response));
                System.exit(0);
            }
        });
        TimeUnit.HOURS.sleep(1);
    }
}
