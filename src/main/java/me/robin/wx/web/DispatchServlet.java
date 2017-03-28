package me.robin.wx.web;

import me.robin.wx.web.listener.AppContextListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by xuanlubin on 2017/3/28.
 */
@WebServlet(name = "DispatchServlet", value = "/*", loadOnStartup = 1)
public class DispatchServlet extends HttpServlet {

    private AbstractApplicationContext wac;

    @Override
    public void init() throws ServletException {
        super.init();
        this.wac = new ClassPathXmlApplicationContext(new String[]{"classpath*:application-web.xml"}, (ApplicationContext) getServletContext().getAttribute(AppContextListener.class.getName()));
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        String path = ((HttpServletRequest) req).getRequestURI();
        String servletPath = StringUtils.substringAfter(path, ((HttpServletRequest) req).getContextPath());
        Servlet servlet = this.wac.getBean(servletPath, Servlet.class);
        servlet.service(req, res);
    }

    @Override
    public void destroy() {
        this.wac.close();
    }
}
