import classLoader.CommonClassLoader;
import cn.hutool.core.util.ReflectUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import static util.Constant.*;

/**
 * @author JwZheng
 * @date 2021/7/22 13:14
 */
@Data
@Slf4j
public class Bootstrap {
    private static Properties props;

    static {
        loadProps();
    }

    public static void main(String[] args) {
        run();
    }

    public static void run() {
        CATALINA_BASE = props.getProperty("CATALINA_BASE");
        if (CATALINA_BASE == null) {
            throw new IllegalArgumentException("CATALINA_BASE 不存在");
        }
        String errorPage = props.getProperty("ERROR_PAGE");
        if (errorPage != null) {
            ERROR_PAGE = errorPage;
        }
        try {
            CommonClassLoader commonClassLoader = new CommonClassLoader();
            Thread.currentThread().setContextClassLoader(commonClassLoader);
            Class<?> serverClass = commonClassLoader.loadClass("catalina.Server");
            File file = new File(CATALINA_BASE, SERVER_XML);
            if (!file.exists() || file.isDirectory()) {
                file = new File(Bootstrap.class.getResource(SERVER_XML).getFile());
                if (!file.exists() || file.isDirectory())
                    throw new FileNotFoundException();
            }
            Object server = ReflectUtil.newInstance(serverClass, file);
            Method method = serverClass.getMethod("start");
            method.invoke(server);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            log.error("服务器启动失败——无法加载启动类");
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            log.error("服务器启动失败——未找到 Server.xml 配置文件");
            e.printStackTrace();
        }
    }

    private static void loadProps() {
        log.info("开始加载 server.properties");
        props = new Properties();
        try {
            props.load(Bootstrap.class.getResourceAsStream("server.properties"));
        } catch (FileNotFoundException e) {
            log.error("server.properties文件未找到");
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("server.properties 加载完成");
    }
}
