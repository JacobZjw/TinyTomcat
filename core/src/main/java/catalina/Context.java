package catalina;

import classLoader.WebappClassLoader;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import exception.BadRequestException;
import exception.ServletException;
import filter.Filter;
import http.Cookie;
import http.HttpSession;
import http.Response;
import lombok.Data;
import util.Constant;
import util.XmlUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JwZheng
 * @date 2021/7/23 16:22
 */
@Data
public class Context {
    private Host parent;

    private String path;
    private String docBase;
    private Boolean reloadable;

    private WebappClassLoader classLoader;
    private Map<String, ServletWrapper> servletWrapperMap;
    private Map<String, FilterWrapper> filterWrapperMap;

    private Map<String, HttpSession> sessionMap;
    private Map<String, Object> attributes;

    public Context(Host parent, String path, String docBase, Boolean reloadable) {
        this.parent = parent;
        this.path = path;
        this.docBase = docBase;
        this.reloadable = reloadable;

        this.filterWrapperMap = new ConcurrentHashMap<>();
        this.servletWrapperMap = new ConcurrentHashMap<>();
        this.sessionMap = new ConcurrentHashMap<>();
        this.attributes = new ConcurrentHashMap<>();
    }

    public void init() throws Exception {
        createClassLoader();
        createWrapper();
        loadOnStartup();
    }

    private void createWrapper() throws Exception {
        File webXml = new File(docBase, Constant.WEB_XML);
        if (!webXml.exists()) {
            throw new FileNotFoundException("找不到配置文件:" + webXml);
        }
        XmlUtil.parseServlet(this, webXml, servletWrapperMap);
        XmlUtil.parseFilter(this, webXml, filterWrapperMap);
    }


    private void loadOnStartup() throws Exception {
        Collection<ServletWrapper> servletWrappers = servletWrapperMap.values();
        TreeMap<Integer, ArrayList<ServletWrapper>> map = new TreeMap<>();
        for (ServletWrapper servletWrapper : servletWrappers) {
            Integer loadOnStartup = servletWrapper.getLoadOnStartup();
            if (loadOnStartup < 0) {
                continue;
            }
            ArrayList<ServletWrapper> list = map.computeIfAbsent(loadOnStartup, k -> new ArrayList<>());
            list.add(servletWrapper);
        }
        for (ArrayList<ServletWrapper> list : map.values()) {
            for (ServletWrapper servletWrapper : list) {
                servletWrapper.load();
            }
        }
    }

    private void createClassLoader() {
        File classPath = new File(docBase, Constant.WEB_CLASSES);
        File libClassPath = new File(docBase, Constant.WEB_LIB_CLASSES);
        List<URL> urlList = new ArrayList<>();
        try {
            urlList.add(classPath.toURI().toURL());
            List<File> libFiles = FileUtil.loopFiles(libClassPath);
            for (File file : libFiles) {
                urlList.add(file.toURI().toURL());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        URL[] urls = urlList.toArray(new URL[0]);
        classLoader = new WebappClassLoader(urls, Context.class.getClassLoader());
    }

    public ServletWrapper findServletWrapper(String url) throws BadRequestException {
        if (StrUtil.isEmpty(url)) {
            throw new BadRequestException("url无效");
        }
        for (ServletWrapper servletWrapper : servletWrapperMap.values()) {
            if (match(url, servletWrapper.getUrlPattern())) {
                return servletWrapper;
            }
        }
        return null;
    }

    public List<Filter> getMatchFilters(String urlPattern) throws BadRequestException {
        if (StrUtil.isEmpty(urlPattern)) {
            throw new BadRequestException("url无效");
        }
        List<Filter> list = new ArrayList<>();
        for (FilterWrapper wrapper : filterWrapperMap.values()) {
            if (match(urlPattern, wrapper.getUrlPattern())) {
                list.add(wrapper.getFilter());
            }
        }
        return list;
    }


    /**
     * 根据urlPattern判断是否匹配，参考https://juejin.cn/post/6844903604109885447
     *
     * @param url     url
     * @param pattern url-pattern
     * @return 结果
     */
    private boolean match(String url, String pattern) {
        //精确匹配
        if (StrUtil.equals(url, pattern)) {
            return true;
        }
        int index = pattern.indexOf("*");
        if (index != -1) {
            //路径匹配
            String s = pattern.substring(0, index);
            if (!StrUtil.isEmpty(s) && url.startsWith(s)) {
                return true;
            }
            //扩展名匹配
            s = pattern.substring(index + 1);
            if (!StrUtil.isEmpty(s)) {
                String urlExt = StrUtil.subAfter(url, ".", false);
                if (StrUtil.equals(s, urlExt)) {
                    return true;
                }
            }
        }
        //缺省匹配
        return StrUtil.equals(pattern, "/");
    }


    public void destroy() throws ServletException {
        for (ServletWrapper wrapper : servletWrapperMap.values()) {
            wrapper.getServlet().destroy();
        }
        for (FilterWrapper wrapper : filterWrapperMap.values()) {
            wrapper.getFilter().destroy();
        }
        servletWrapperMap.clear();
        filterWrapperMap.clear();
        sessionMap.clear();
        attributes.clear();
    }

    public void invalidateSession(String sessionId) {
        sessionMap.remove(sessionId);
    }

    public HttpSession getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

    public HttpSession createSession(Response response) {
        HttpSession session = new HttpSession(UUID.randomUUID().toString().toUpperCase(Locale.ROOT), this);
        sessionMap.put(session.getId(), session);
        response.addCookie(new Cookie("JSESSIONID", session.getId()));
        return session;
    }

    @Override
    public String toString() {
        return "Context{" +
                "path='" + path + '\'' +
                ", docBase='" + docBase + '\'' +
                ", reloadable=" + reloadable +
                '}';
    }
}

