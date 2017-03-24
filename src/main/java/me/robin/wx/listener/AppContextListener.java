package me.robin.wx.listener;

import me.robin.wx.service.GZHRequestService;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Created by xuanlubin on 2017/3/24.
 * 初始化组件
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    private final GZHRequestService gzhRequestService = new GZHRequestService();

    private Thread monitorThread;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        monitorThread = new Thread(gzhRequestService);
        monitorThread.start();
        sce.getServletContext().setAttribute(GZHRequestService.class.getName(),gzhRequestService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        IOUtils.closeQuietly(gzhRequestService);
        monitorThread.interrupt();
    }
}
