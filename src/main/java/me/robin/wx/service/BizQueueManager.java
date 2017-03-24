package me.robin.wx.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutableTriple;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by xuanlubin on 2017/2/14.
 * 公众号任务管理
 */
public enum BizQueueManager {

    INS;

    private BlockingQueue<MutableTriple<String, Long, String>> bizQueue = new LinkedBlockingQueue<>();

    private BlockingQueue<MutableTriple<String, Long, String>> dispatchTime = new LinkedBlockingQueue<>();

    private Map<String, MutableTriple<String, Long, String>> pairMapping = new ConcurrentHashMap<>();

    //60秒
    final Long LIMIT = 60000L;

    BizQueueManager() {
        new Thread(() -> {
            while (true) {
                try {
                    MutableTriple<String, Long, String> triple = dispatchTime.take();
                    if (triple.getMiddle() + LIMIT < System.currentTimeMillis()) {
                        bizQueue.offer(triple);
                    } else {
                        dispatchTime.offer(triple);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public MutableTriple<String, Long, String> fetchNextBiz() {
        MutableTriple<String, Long, String> triple = bizQueue.poll();
        if (null != triple) {
            triple.setMiddle(System.currentTimeMillis());
            dispatchTime.offer(triple);
            pairMapping.put(triple.getLeft() + "#" + triple.getRight(), triple);
        }
        return triple;
    }

    public void offerNewTask(String biz) {
        offerNewTask(biz, null);
    }

    public void report(String biz, String fromMsgId) {
        MutableTriple<String, Long, String> triple = pairMapping.remove(biz + "#" + fromMsgId);
        if (null != triple) {
            dispatchTime.remove(triple);
        }
    }

    public void offerNewTask(String biz, String fromMsgId) {
        if (StringUtils.isBlank(biz)) {
            return;
        }
        bizQueue.offer(new MutableTriple<>(biz, null, fromMsgId));
    }
}