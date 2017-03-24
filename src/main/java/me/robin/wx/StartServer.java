package me.robin.wx;

import me.robin.wx.service.BizQueueManager;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.*;

/**
 * Created by xuanlubin on 2017/1/5.
 */
public class StartServer {
    public static void main(String[] args) {
        try {

            //BizQueueManager.INS.offerNewTask("MjM5MTgyODQ2Mw==");
            //BizQueueManager.INS.offerNewTask("MzAxMzM4MTk2Nw==");
            //BizQueueManager.INS.offerNewTask("MzAxMDQ2OTA1OQ==");
            //BizQueueManager.INS.offerNewTask("MTIzNDg3NzY2MA==");

            // 服务器的监听端口
            Server server = new Server(8080);
            // 关联一个已经存在的上下文
            WebAppContext context = new WebAppContext();
            // 设置Web内容上下文路径
            context.setResourceBase("E:\\project\\Bullbat-Crawl\\wx_proxy_back_service\\src\\main\\webapp");
            // 设置描述符位置
            context.setDescriptor("./webapp/WEB-INF/web.xml");
            context.setConfigurations(new Configuration[]{
                    new AnnotationConfiguration(), new WebXmlConfiguration(),
                    new WebInfConfiguration(),
                    new PlusConfiguration(), new MetaInfConfiguration(),
                    new FragmentConfiguration(), new EnvConfiguration()
            });
            context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",".*");
            // 设置上下文路径
            context.setContextPath("/wx_proxy");
            context.setParentLoaderPriority(true);
            server.setHandler(context);
            // 启动
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
