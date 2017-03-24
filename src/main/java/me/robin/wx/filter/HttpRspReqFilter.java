package me.robin.wx.filter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by xuanlubin on 2017/1/5.
 */
@WebFilter(value = "/*")
public class HttpRspReqFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(HttpRspReqFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        response.setCharacterEncoding("utf-8");
        if (response instanceof HttpServletResponse) {
            ((HttpServletResponse) response).setHeader("Cache-control", "no-cache");
            String client = ((HttpServletRequest) request).getHeader("client");
            if (StringUtils.contains(client, "ffff:")) {
                client = StringUtils.substringAfter(client, "ffff:");
            }
            if (StringUtils.isBlank(client)) {
                client = request.getRemoteAddr();
            }
            logger.info("客户端请求  {}  {}", client, ((HttpServletRequest) request).getServletPath());
        }

        request.setCharacterEncoding("utf-8");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
