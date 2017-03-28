package me.robin.wx.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import me.robin.wx.Constants;
import me.robin.wx.util.*;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuanlubin on 2017/3/23.
 * 公众号请求任务调度
 */
@ManagedBean
public class GZHRequestService implements Runnable, Closeable {

    private static final Logger logger = LoggerFactory.getLogger(GZHRequestService.class);

    private LinkedBlockingQueue<ImmutablePair<String, String>> requestQueue = new LinkedBlockingQueue<>();

    private GZHUinCookieInterceptor gzhUinCookieInterceptor = new GZHUinCookieInterceptor();

    private Thread deamon;

    private OkHttpClient client = new OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(gzhUinCookieInterceptor)
            .addNetworkInterceptor(new GZHRedirectInterceptor())
            .build();

    private volatile boolean shutdown = false;

    private Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            String url = call.request().url().toString();
            logger.error("公众号历史请求失败:{}", url, e);
            String biz = GZHAnalyse.getBizFromUrl(url);
            String fromMsgId = GZHAnalyse.getFromMsgIdFromUrl(url);
            BizQueueManager.INS.offerNewTask(biz, fromMsgId);
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {

            HttpUrl url = call.request().url();
            String biz = GZHAnalyse.getBizFromUrl(url);
            String responseContent;
            try {
                if ("deflate".equalsIgnoreCase(response.header("Content-Encoding"))) {
                    responseContent = InflaterUtil.tranInflaterInputStream(response.body().bytes());
                } else {
                    responseContent = response.body().string();
                }
            } catch (IOException e) {
                logger.error("公众号请求内容异常", e);

                String fromMsgId = GZHAnalyse.getFromMsgIdFromUrl(url);
                BizQueueManager.INS.offerNewTask(biz, fromMsgId);
                return;
            } finally {
                IOUtils.closeQuietly(response);
            }

            if (Constants.REFRESH_BIZ.equalsIgnoreCase(biz)) {
                return;
            }

            if (GZHAnalyse.analyseRsp(responseContent, url)) {
                String uin = GZHAnalyse.getUinFromUrl(url);
                logger.warn("uin:{} 获取历史cookie失效", uin);
                GZHUinClientBinder.lock(uin);
                gzhUinCookieInterceptor.clear(uin);
            }
        }
    };


    public void submit(String location, String headers) {
        requestQueue.offer(new ImmutablePair<>(location, headers));
    }


    @Override
    public void run() {
        while (!shutdown) {
            ImmutablePair<String, String> requestInfo;
            try {
                requestInfo = requestQueue.take();
            } catch (InterruptedException e) {
                continue;
            }
            JSONObject headers = JSON.parseObject(requestInfo.getRight());
            String url = requestInfo.getLeft();
            Request.Builder builder;
            if (requestInfo.getLeft().contains("&frommsgid=")) {
                builder = GZHHistoryUrl.process(url);
            } else {
                builder = new Request.Builder().url(GZHHistoryUrl.GZH_MP_HOST + url);
            }
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                builder.header(entry.getKey(), TypeUtils.castToString(entry.getValue()));
            }
            Request request = builder.get().build();
            logger.info("请求公众号历史:{} {}", GZHAnalyse.getBizFromUrl(request.url()), GZHAnalyse.getFromMsgIdFromUrl(request.url()));
            client.newCall(request).enqueue(callback);
        }
    }

    @Override
    public void close() throws IOException {
        this.shutdown = true;
        this.deamon.interrupt();
        try {
            this.deamon.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @PostConstruct
    private void init() {
        this.deamon = new Thread(this);
        this.deamon.start();
    }
}
