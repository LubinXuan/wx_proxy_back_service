package me.robin.wx.web;

import me.robin.wx.web.listener.AppContextListener;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.*;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xuanlubin on 2017/3/28.
 */
@WebServlet(name = "DispatchServlet", value = "/*", loadOnStartup = 1)
public class DispatchServlet extends HttpServlet {

    //spring web context
    private AbstractApplicationContext wac;

    private Map<String, Boolean> init = new ConcurrentHashMap<>();

    //servlet config cache map
    private Map<String, ServletConfig> configMap = new ConcurrentHashMap<>();

    @Override
    public void init() throws ServletException {
        super.init();
        this.wac = new ClassPathXmlApplicationContext(new String[]{"classpath*:application-web.xml"}, (ApplicationContext) getServletContext().getAttribute(AppContextListener.class.getName()));
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        String path = ((HttpServletRequest) req).getRequestURI();
        String servletPath = StringUtils.substringAfter(path, ((HttpServletRequest) req).getContextPath());
        try {
            Servlet servlet = findServlet(servletPath);
            servlet.service(req, res);
        } catch (NoSuchBeanDefinitionException e) {
            ((HttpServletResponse) res).sendError(404);
        }
    }

    private Servlet findServlet(String servletPath) throws ServletException {
        Servlet servlet = this.wac.getBean(servletPath, Servlet.class);
        if (!this.wac.isSingleton(servletPath)) {
            initServlet(servlet);
        } else if (!init.containsKey(servletPath)) {
            initServlet(servlet);
            init.put(servletPath, true);
        }
        return servlet;
    }

    private void initServlet(Servlet servlet) throws ServletException {
        ServletConfig servletConfig = configMap.computeIfAbsent(servlet.getClass().getName(), s -> {
            WebInitParam[] initParams = servlet.getClass().getAnnotationsByType(WebInitParam.class);
            ConcurrentHashMap<String, WebInitParam> parameterMap = new ConcurrentHashMap<>();
            if (null != initParams && initParams.length > 0) {
                for (WebInitParam initParam : initParams) {
                    parameterMap.put(initParam.name(), initParam);
                }
            }
            return new ServletConfig() {
                @Override
                public String getServletName() {
                    return servlet.getClass().getSimpleName();
                }

                @Override
                public ServletContext getServletContext() {
                    return getServletConfig().getServletContext();
                }

                @Override
                public String getInitParameter(String name) {
                    return parameterMap.get(name).name();
                }

                @Override
                public Enumeration<String> getInitParameterNames() {
                    return parameterMap.keys();
                }
            };
        });

        servlet.init(servletConfig);
    }


    @Override
    public void destroy() {
        this.wac.close();
    }
}
