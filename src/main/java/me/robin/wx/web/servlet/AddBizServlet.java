package me.robin.wx.web.servlet;

import me.robin.wx.service.BizQueueManager;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by xuanlubin on 2017/3/24.
 * 添加公众号Biz
 */
@WebServlet(name = "AddBizServlet", value = "/add_biz")
public class AddBizServlet extends BaseIocServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String biz = req.getParameter("biz");
        if (StringUtils.isNotBlank(biz)) {
            BizQueueManager.INS.offerNewTask(biz);
        }
    }
}
