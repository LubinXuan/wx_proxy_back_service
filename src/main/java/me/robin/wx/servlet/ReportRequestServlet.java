package me.robin.wx.servlet;

import me.robin.wx.service.BizQueueManager;
import me.robin.wx.service.GZHAnalyse;
import me.robin.wx.service.GZHRequestService;
import me.robin.wx.util.GZHUinClientBinder;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

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
@WebServlet(name = "ReportRequestServlet", value = "/report")
public class ReportRequestServlet extends HttpServlet {

    private final GZHRequestService gzhRequestService = new GZHRequestService();

    private Thread monitorThread;

    @Override
    public void init() throws ServletException {
        super.init();
        monitorThread = new Thread(gzhRequestService);
        monitorThread.start();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String location = req.getParameter("url");
        String headers = req.getParameter("headers");
        if (StringUtils.isNotBlank(location)) {
            gzhRequestService.submit(location, headers);
            String biz = GZHAnalyse.getBizFromUrl(location);
            String fromMsgId = GZHAnalyse.getFromMsgIdFromUrl(location);
            BizQueueManager.INS.report(biz, fromMsgId);
            GZHUinClientBinder.bind(req.getHeader("client"), GZHAnalyse.getUinFromUrl(location));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    public void destroy() {
        IOUtils.closeQuietly(gzhRequestService);
        monitorThread.interrupt();
        super.destroy();
    }
}
