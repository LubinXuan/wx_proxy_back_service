package me.robin.wx.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import me.robin.wx.util.GZHHistoryUrl;
import me.robin.wx.util.GZHUinClientBinder;
import me.robin.wx.util.GZHUinCookieInterceptor;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * Created by xuanlubin on 2017/3/23.
 * 公众号请求任务调度
 */
public class GZHRequestService implements Runnable, Closeable {

    private static final Logger logger = LoggerFactory.getLogger(GZHRequestService.class);

    private LinkedBlockingQueue<ImmutablePair<String, String>> requestQueue = new LinkedBlockingQueue<>();

    private OkHttpClient client = new OkHttpClient.Builder().readTimeout(60, TimeUnit.SECONDS).connectTimeout(60, TimeUnit.SECONDS).addInterceptor(new GZHUinCookieInterceptor()).build();

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
            try {
                String responseContent;
                if ("deflate".equalsIgnoreCase(response.header("Content-Encoding"))) {
                    responseContent = tranInflaterInputStream(response.body().bytes());
                } else {
                    responseContent = response.body().string();
                }
                if (GZHAnalyse.analyseRsp(responseContent, call.request().url().toString())) {
                    String uin = GZHAnalyse.getUinFromUrl(call.request().url().query());
                    logger.warn("uin:{} 获取历史cookie失效", uin);
                    GZHUinClientBinder.lock(uin);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(response);
            }
        }
    };


    public void submit(String location, String headers) {
        requestQueue.offer(new ImmutablePair<>(location, headers));
    }


    @Override
    public void run() {
        while (!shutdown) {
            ImmutablePair<String, String> request;
            try {
                request = requestQueue.take();
            } catch (InterruptedException e) {
                continue;
            }
            JSONObject headers = JSON.parseObject(request.getRight());
            String url = request.getLeft();
            Request.Builder builder;
            if (request.getLeft().contains("&frommsgid=")) {
                builder = GZHHistoryUrl.process(url);
            } else {
                builder = new Request.Builder().url(GZHHistoryUrl.GZH_MP_HOST + url);
            }
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                builder.header(entry.getKey(), TypeUtils.castToString(entry.getValue()));
            }

            client.newCall(builder.get().build()).enqueue(callback);
        }
    }

    @Override
    public void close() throws IOException {
        this.shutdown = true;
    }

    private boolean isZlibHeader(byte[] bytes) {
        //deal with java stupidity : convert to signed int before comparison
        char byte1 = (char) (bytes[0] & 0xFF);
        char byte2 = (char) (bytes[1] & 0xFF);

        return byte1 == 0x78 && (byte2 == 0x01 || byte2 == 0x9c || byte2 == 0xDA);
    }

    private String tranInflaterInputStream(byte[] encBytes) throws IOException {
        Inflater inflator = new Inflater(true);
        boolean isZlibHeader = isZlibHeader(encBytes);
        inflator.setInput(encBytes, isZlibHeader ? 2 : 0, isZlibHeader ? encBytes.length - 2 : encBytes.length);
        byte[] buf = new byte[4096];
        int nbytes = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        do {
            try {
                nbytes = inflator.inflate(buf);
                if (nbytes > 0) {
                    bos.write(buf, 0, nbytes);
                }
            } catch (DataFormatException e) {
                //handle error
            }
        } while (nbytes > 0);
        inflator.end();
        return bos.toString();
    }
}
