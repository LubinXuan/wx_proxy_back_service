package me.robin.wx.servlet;

import me.robin.wx.util.InjectUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Created by xuanlubin on 2017/3/28.
 */
public abstract class BaseIocServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            InjectUtils.inject(this, WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext()));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
