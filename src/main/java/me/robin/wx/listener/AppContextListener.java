package me.robin.wx.listener;

import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebListener;

/**
 * Created by xuanlubin on 2017/3/24.
 * 初始化组件
 */
@WebListener
public class AppContextListener extends ContextLoaderListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        event.getServletContext().setInitParameter("contextConfigLocation", "classpath*:application.xml");
        super.contextInitialized(event);
    }
}
