package me.robin.wx;

import me.robin.wx.service.BizQueueManager;
import org.eclipse.jetty.annotations.*;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.Decorator;
import org.eclipse.jetty.webapp.*;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by xuanlubin on 2017/1/5.
 */
public class StartServer {
    public static void main(String[] args) {
        try {


            //循环测试
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (BizQueueManager.INS.isEmpty()) {
                        BizQueueManager.INS.offerNewTask("MjM5MTgyODQ2Mw==");
                        BizQueueManager.INS.offerNewTask("MzAxMzM4MTk2Nw==");
                        BizQueueManager.INS.offerNewTask("MzAxMDQ2OTA1OQ==");
                        BizQueueManager.INS.offerNewTask("MTIzNDg3NzY2MA==");
                    }
                }
            }, 0, 20000);


            // 服务器的监听端口
            Server server = new Server(8080);
            // 关联一个已经存在的上下文
            WebAppContext context = new WebAppContext();
            // 设置Web内容上下文路径
            context.setResourceBase("E:\\project\\Bullbat-Crawl\\wx_proxy_back_service\\src\\main\\webapp");
            // 设置描述符位置
            context.setDescriptor("./webapp/WEB-INF/web.xml");


            context.setConfigurations(new Configuration[]{
                    new WebXmlConfiguration(),
                    new WebInfConfiguration(),
                    new PlusConfiguration(),
                    new MetaInfConfiguration(),
                    new FragmentConfiguration(),
                    new EnvConfiguration(),new AnnotationConfiguration()
                    /*new AnnotationConfiguration() {
                        @Override
                        public void configure(WebAppContext context) throws Exception {
                            super.configure(context);
                            context.getObjectFactory().clear();
                            context.getObjectFactory().addDecorator(new Decorator() {
                                private AnnotationIntrospector _introspector = new AnnotationIntrospector();

                                {
                                    this._introspector.registerHandler(new PostConstructAnnotationHandler(context));
                                    this._introspector.registerHandler(new PreDestroyAnnotationHandler(context));
                                }

                                <T> void introspect(T o) {
                                    this._introspector.introspect(o.getClass());
                                }

                                public <T> T decorate(T o) {
                                    this.introspect(o);
                                    return o;
                                }

                                @Override
                                public void destroy(Object o) {

                                }
                            });
                        }
                    }*/
            });
            context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern", ".*");
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
