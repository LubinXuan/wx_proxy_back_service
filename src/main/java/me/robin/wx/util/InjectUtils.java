package me.robin.wx.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;
import java.lang.reflect.Field;

/**
 * Created by xuanlubin on 2017/3/28.
 */
public class InjectUtils {
    public static void inject(Object object, ApplicationContext context) throws Exception {
        Class clazz = object.getClass();
        while (null != clazz && !clazz.isInterface()) {
            Field[] fields = clazz.getDeclaredFields();
            if (null != fields) {
                for (Field field : fields) {
                    Resource resource = field.getAnnotation(Resource.class);
                    if (null != resource) {
                        Object bean;
                        if (StringUtils.isNotBlank(resource.name())) {
                            bean = context.getBean(resource.name(), field.getType());
                        } else {
                            bean = context.getBean(field.getType());
                        }
                        if (null == bean) {
                            throw new RuntimeException("required bean not found " + field.getName());
                        } else {
                            try {
                                if (field.isAccessible()) {
                                    field.set(object, bean);
                                } else {
                                    field.setAccessible(true);
                                    field.set(object, bean);
                                    field.setAccessible(false);
                                }
                            } catch (Exception e) {
                                throw new RuntimeException("bean init failure", e);
                            }
                        }
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
}
