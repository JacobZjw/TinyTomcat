package util;

import java.util.List;

/**
 * @author JwZheng
 * @date 2021/7/22 14:56
 */
public class Constant {

    public static final String WEB_XML = "WEB-INF/web.xml";
    public static final String WEB_CLASSES = "WEB-INF/classes";
    public static final String WEB_LIB_CLASSES = "WEB-INF/lib";
    public static final String BLANK = " ";
    public static final String CRLF = "\r\n";
    public static final Integer POLLER_THREAD_COUNT = Math.min(4, Runtime.getRuntime().availableProcessors());
    public static final List<String> STATIC_RESOURCE_SUFFIX = List.of(".html", ".ico", ".png", ".js", ".css", ".jpg", ".ico");
    public static final String SERVER_XML = "conf/server.xml";
    public static final Integer DEFAULT_LOAD_ON_STARTUP = -1;

    public static String CATALINA_BASE = "C:\\Users\\Jv____\\IdeaProjects\\TinyTomcat\\core\\src\\main\\resources\\";
    public static String DEFAULT_CONTENT_TYPE = "text/html; charset=UTF-8";
    public static String ERROR_PAGE = "/error.html";

}
