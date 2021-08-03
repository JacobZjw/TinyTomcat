package util;

import catalina.*;
import catalina.connector.Connector;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JwZheng
 * @date 2021/7/23 16:40
 */
public class XmlUtil {

    private static void parseServletOrFilter(Context parent, File webXML, Map<String, ServletWrapper> servletWrapperMap, Map<String, FilterWrapper> filterWrapperMap, String prefix) throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(webXML);

        Element root = document.getRootElement();
        List<Element> servlets = root.elements(prefix);
        servlets.forEach(element -> {
            //servlet-name和servlet-class
            String name = element.elementText(prefix + "-name");
            String className = element.elementText(prefix + "-class");

            //Init-Param
            Map<String, String> initParamMap = new HashMap<>();
            List<Element> initParams = element.elements("init-param");
            for (Element param : initParams) {
                initParamMap.put(param.elementText("param-name"), param.elementText("param-value"));
            }

            //Load-on-startup
            Integer loadOnStartup = Constant.DEFAULT_LOAD_ON_STARTUP;
            Element loadOnStartupEle = element.element("load-on-startup");
            if (loadOnStartupEle != null) {
                loadOnStartup = Integer.valueOf(loadOnStartupEle.getText());
            }
            if ("servlet".equals(prefix)) {
                servletWrapperMap.put(name, new ServletWrapper(parent, name, className, initParamMap, loadOnStartup));
            } else if ("filter".equals(prefix)) {
                filterWrapperMap.put(name, new FilterWrapper(parent, name, className, initParamMap));
            }
        });
        //servlet-mapping
        List<Element> mappings = root.elements(prefix + "-mapping");
        mappings.forEach(mapping -> {
            String urlPattern = mapping.elementText("url-pattern");
            String name = mapping.elementText(prefix + "-name");
            Wrapper wrapper = null;
            if ("servlet".equals(prefix)) {
                 wrapper = servletWrapperMap.get(name);
            } else if ("filter".equals(prefix)) {
                 wrapper = filterWrapperMap.get(name);
            }
            if (wrapper != null) {
                wrapper.setUrlPattern(urlPattern);
            }
        });
    }

    public static void parseServlet(Context parent, File webXML, Map<String, ServletWrapper> servletWrapperMap) throws DocumentException {
        parseServletOrFilter(parent, webXML, servletWrapperMap, null, "servlet");
    }

    public static void parseFilter(Context parent, File webXML, Map<String, FilterWrapper> filterWrapperMap) throws DocumentException {
        parseServletOrFilter(parent, webXML, null, filterWrapperMap, "filter");
    }

    public static Map<String, Service> parseService(Server parent, File serverXML) throws Exception {
        Map<String, Service> serviceMap = new HashMap<>();
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(serverXML);
        Element serverEle = document.getRootElement();
        List<Element> serviceEleList = serverEle.elements("Service");
        for (Element serviceEle : serviceEleList) {
            String name = serviceEle.attributeValue("name");
            Service service = new Service(parent, name);
            service.setConnectors(parseConnector(service, serviceEle));
            service.setEngine(parseEngine(service, serviceEle));
            serviceMap.put(name, service);
        }
        return serviceMap;
    }

    private static List<Connector> parseConnector(Service parent, Element element) {
        List<Connector> connList = new ArrayList<>();
        List<Element> connEleList = element.elements("Connector");
        for (Element connEle : connEleList) {
            String port = connEle.attributeValue("port");
            String connectionTimeout = connEle.attributeValue("connectionTimeout");
            Connector connector = new Connector(parent, Integer.valueOf(port));
            connector.setConnectionTimeout(Long.valueOf(connectionTimeout));
            connList.add(connector);
        }
        return connList;
    }

    private static Engine parseEngine(Service parent, Element element) throws Exception {
        Element engineEle = element.element("Engine");
        String name = engineEle.attributeValue("name");
        String defaultHostName = engineEle.attributeValue("defaultHost");
        Engine engine = new Engine(parent, name);
        List<Host> hosts = parseHost(engine, engineEle);
        engine.setHosts(hosts);
        if (StrUtil.isEmpty(defaultHostName) || CollectionUtil.isEmpty(hosts)) {
            throw new Exception("配置文件错误：未提供defaultHost");
        }
        Host defaultHost = null;
        for (Host host : hosts) {
            if (defaultHostName.equals(host.getName())) {
                defaultHost = host;
                break;
            }
        }
        if (defaultHost == null) {
            throw new Exception("配置文件错误：defaultHost不存在");
        }
        engine.setDefaultHost(defaultHost);
        return engine;
    }

    private static List<Host> parseHost(Engine parent, Element element) {
        List<Element> hostEleList = element.elements("Host");
        List<Host> hosts = new ArrayList<>();
        for (Element hostEle : hostEleList) {
            String name = hostEle.attributeValue("name");
            String appBase = hostEle.attributeValue("appBase");
            Host host = new Host(parent, name, appBase);
            host.setContextMap(parseContext(host, hostEle));
            hosts.add(host);
        }
        return hosts;
    }

    private static Map<String, Context> parseContext(Host parent, Element element) {
        List<Element> contextEleList = element.elements("Context");
        Map<String, Context> contextMap = new HashMap<>();
        for (Element contextEle : contextEleList) {
            String path = contextEle.attributeValue("path");
            String docBase = contextEle.attributeValue("docBase");
            String reloadable = contextEle.attributeValue("reloadable");
            contextMap.put(path, new Context(parent, path, docBase, Boolean.valueOf(reloadable)));
        }
        return contextMap;
    }

}
