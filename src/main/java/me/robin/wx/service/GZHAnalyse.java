package me.robin.wx.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.util.TypeUtils;
import okhttp3.HttpUrl;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

public class GZHAnalyse {

    private static final Logger logger = LoggerFactory.getLogger(GZHAnalyse.class);

    private static final ThreadLocal<DateFormat> DATE_FORMAT_THREAD_LOCAL = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyyMMddHH");
        }
    };

    private static final Map<String, ImmutablePair<AtomicLong, AtomicLong>> recordMap = new ConcurrentHashMap<>();


    private static String decodeUrlParamter(String val) {
        try {
            return URLDecoder.decode(val, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return val;
        }
    }

    public static String getUinFromUrl(String url) {
        return decodeUrlParamter(StringUtils.substringBetween(url, "uin=", "&"));
    }

    public static String getUinFromUrl(HttpUrl url) {
        return url.queryParameter("uin");
    }

    public static String getBizFromUrl(String location) {
        return decodeUrlParamter(StringUtils.substringBetween(location, "__biz=", "&"));
    }

    public static String getBizFromUrl(HttpUrl location) {
        return location.queryParameter("__biz");
    }

    public static String getFromMsgIdFromUrl(String location) {
        return StringUtils.substringBetween(location, "frommsgid=", "&");
    }

    public static String getFromMsgIdFromUrl(HttpUrl location) {
        return location.queryParameter("frommsgid");
    }

    /**
     * @param msgList
     * @param url
     * @return 返回true 表示cookie失效
     */
    public static boolean analyseRsp(String msgList, HttpUrl url) {
        String fromMsgId = null;
        String __biz = getBizFromUrl(url);

        if (StringUtils.contains(msgList, "no session")) {
            BizQueueManager.INS.offerNewTask(__biz, getFromMsgIdFromUrl(url));
            return true;
        }

        if (StringUtils.contains(msgList, "\"type\":\"Buffer\"")) {
            JSONArray dataArray = JSON.parseObject(msgList).getJSONArray("data");
            ByteBuffer buffer = ByteBuffer.allocate(dataArray.size());
            for (int i = 0; i < dataArray.size(); i++) {
                byte c = dataArray.getByte(i);
                buffer.put(c);
            }
            buffer.flip();
            msgList = getString(buffer);
            buffer.clear();
        }

        boolean isContinue, friend;

        if (StringUtils.contains(msgList, "<div class=\"profile_info appmsg\">")) {
            msgList = StringEscapeUtils.unescapeHtml3(msgList);
            friend = !StringUtils.contains(msgList, "<span class=\"tips\">关注公众帐号，接收更多消息</span>");
            isContinue = friend;
            msgList = StringUtils.substringBetween(msgList, "msgList = '", "';\n");
        } else if (StringUtils.contains(StringUtils.lowerCase(msgList), "<title>查看历史消息</title>")) {
            friend = "1".equals(StringUtils.substringBetween(msgList, "isFriend = \"", "\",\r\n"));
            isContinue = "1".equals(StringUtils.substringBetween(msgList, "isContinue = \"", "\",\r\n"));
            msgList = StringUtils.substringBetween(msgList, "msgList = ", ";\r\n");
        } else {
            //,"is_friend":0,"is_continue":1,"count":10,
            friend = "1".equals(StringUtils.substringBetween(msgList, "\"is_friend\":", ","));
            isContinue = "1".equals(StringUtils.substringBetween(msgList, "\"is_continue\":", ","));
        }
        fromMsgId = getListMinMsgId(msgList, __biz);

        logger.info("获取到公众号文章列表 biz:{} url:{}", __biz, url);
        if (friend && isContinue && StringUtils.isNotBlank(fromMsgId)) {
            BizQueueManager.INS.offerNewTask(__biz, fromMsgId);
        }
        return false;
    }

    private static String getListMinMsgId(String msgList, String biz) {
        if (StringUtils.contains(msgList, "general_msg_list")) {
            msgList = (String) JSONPath.read(msgList, "$.general_msg_list");
        }

        JSONArray list = (JSONArray) JSONPath.read(msgList, "$.list");

        if (null == list) {
            logger.warn("没有正常获取到公众号内容 biz:{} rsp:{}", biz, msgList);
            return null;
        }

        for (int i = 0; i < list.size(); i++) {
            JSONObject group = list.getJSONObject(i);
            logger.info("biz:{} msgId:{}", biz, JSONPath.eval(group, "comm_msg_info.id"));
        }

        ImmutablePair<AtomicLong, AtomicLong> record = recordMap.compute(biz, (s, atomicLongAtomicLongImmutablePair) -> {
            if (null == atomicLongAtomicLongImmutablePair) {
                atomicLongAtomicLongImmutablePair = new ImmutablePair<>(new AtomicLong(0), new AtomicLong(0));
            }
            return atomicLongAtomicLongImmutablePair;
        });

        record.getLeft().incrementAndGet();
        record.getRight().addAndGet(list.size());

        if (list.size() < 10) {
            return null;
        } else {
            return TypeUtils.castToString(JSONPath.eval(list.get(list.size() - 1), "comm_msg_info.id"));
        }
    }

    private static String getString(ByteBuffer buffer) {
        Charset charset = null;
        CharsetDecoder decoder = null;
        CharBuffer charBuffer = null;
        try {
            charset = Charset.forName("UTF-8");
            decoder = charset.newDecoder();
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
            return charBuffer.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "error";
        }
    }

}
