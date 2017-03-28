package me.robin.wx.web.servlet;

import me.robin.wx.service.BizQueueManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by xuanlubin on 2017/3/24.
 * 添加公众号Biz
 */
@Controller("/add_biz")
public class AddBizServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String biz = req.getParameter("biz");
        if (StringUtils.isNotBlank(biz)) {
            BizQueueManager.INS.offerNewTask(biz);
        }
    }
}
