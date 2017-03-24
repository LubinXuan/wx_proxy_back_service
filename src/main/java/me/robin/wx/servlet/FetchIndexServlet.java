package me.robin.wx.servlet;

import me.robin.wx.service.BizQueueManager;
import me.robin.wx.util.GZHUinClientBinder;
import me.robin.wx.util.ResUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by xuanlubin on 2017/2/14.
 * 微信客户端获取公众号列表key,pass_ticket任务
 */
@WebServlet(name = "FetchIndexServlet", value = "/fetch")
public class FetchIndexServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(FetchIndexServlet.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userAgent = req.getHeader("user-agent");
        if (!StringUtils.contains(userAgent, "MicroMessenger")) {
            resp.setStatus(403);
            return;
        }

        MutableTriple<String, Long, String> biz = BizQueueManager.INS.fetchNextBiz(!GZHUinClientBinder.isLocked(req.getHeader("client")));
        ResUtil.writeJson(req, resp, new HashMap<String, String>() {{
            if (null != biz) {
                logger.info("dispatch biz:{}   fromMsgId:{}", biz.getLeft(), biz.getRight());
                put("biz", biz.getLeft());
                put("fromMsgId", biz.getRight());
            }
        }});
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

}
