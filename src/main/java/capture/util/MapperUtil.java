package capture.util;

import org.springframework.context.ApplicationContext;

public class MapperUtil {
    private static ApplicationContext applicationContext = null;


    /**
     * 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
     */
    public static <T> T getBean(Class<T> requiredType) {
        return applicationContext.getBean(requiredType);
    }

}
