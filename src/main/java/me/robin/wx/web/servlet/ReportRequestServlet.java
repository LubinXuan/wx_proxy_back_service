package me.robin.wx.web.servlet;

import me.robin.wx.service.BizQueueManager;
import me.robin.wx.service.GZHAnalyse;
import me.robin.wx.service.GZHRequestService;
import me.robin.wx.util.GZHUinClientBinder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by xuanlubin on 2017/2/14.
 * 代理提交微信客户端处理好的url,以及请求头信息
 */
@Controller("/report")
public class ReportRequestServlet extends HttpServlet {

    @Resource
    private GZHRequestService gzhRequestService;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String location = req.getParameter("url");
        String headers = req.getParameter("headers");
        if (StringUtils.isNotBlank(location)) {
            gzhRequestService.submit(location, headers);
            String biz = GZHAnalyse.getBizFromUrl(location);
            String fromMsgId = GZHAnalyse.getFromMsgIdFromUrl(location);
            BizQueueManager.INS.report(biz, fromMsgId);
            GZHUinClientBinder.bind((String) req.getAttribute("client"), GZHAnalyse.getUinFromUrl(location));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }
}
