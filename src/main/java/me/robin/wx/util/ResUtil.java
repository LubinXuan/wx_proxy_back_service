package me.robin.wx.util;

import com.alibaba.fastjson.JSON;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by xuanlubin on 2017/3/20.
 */
public class ResUtil {
    public static void writeJson(HttpServletRequest req, HttpServletResponse rsp, Object data) throws IOException {
        String callback = req.getParameter("callback");
        if (StringUtils.isBlank(callback)) {
            IOUtils.write(JSON.toJSONString(data), rsp.getOutputStream(), Charset.forName("utf-8"));
        } else {
            IOUtils.write(callback + "(" + JSON.toJSONString(data) + ")", rsp.getOutputStream(), Charset.forName("utf-8"));
        }
    }
}
