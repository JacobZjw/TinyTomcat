package http;

import catalina.Context;
import catalina.connector.Http11NioProcessor;
import cn.hutool.core.util.StrUtil;
import exception.BadRequestException;
import lombok.Data;
import util.RequestMethod;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 通过InputStream输入流把请求中的信息封装成Request对象
 *
 * @author JwZheng
 * @date 2021/7/22 13:56
 */
@Data
public class Request {

    /**
     * 完整的请求体
     */
    private String requestStr;

    /**
     * 请求的字节流形式
     */
    private byte[] data;

    /**
     * 请求HTTP方式
     */
    private RequestMethod method;

    /**
     * 请求资源路径
     */
    private String url;

    /**
     * 请求参数
     */
    private Map<String, String[]> paramMap;

    /**
     * 请求头
     */
    private Map<String, String> headerMap;

    private Map<String, Object> attributes;

    private Cookie[] cookies;
    private HttpSession session;
    private RequestDispatcher requestDispatcher;
    private Context context;

    private Http11NioProcessor nioProcessor;

    public Request(byte[] data, Http11NioProcessor nioProcessor) throws BadRequestException {
        this.data = data;
        this.requestStr =
                URLDecoder.decode(new String(data, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        if (StrUtil.isEmpty(requestStr)) {
            throw new BadRequestException("无法获取请求体");
        }
        this.nioProcessor = nioProcessor;
        this.paramMap = new HashMap<>();
        this.headerMap = new HashMap<>();
        this.attributes = new HashMap<>();
        this.requestDispatcher = new RequestDispatcher(nioProcessor.getCoyoteAdapter());
        parseHeader();
    }

    private void parseHeader() {
        String[] lines = requestStr.split("\r\n");
        //1、解析方法
        this.method = RequestMethod.valueOf(StrUtil.subBefore(lines[0], " ", false));
        //2、解析 URL
        String rawUrl = StrUtil.subBetween(lines[0], " ", " ");
        String[] rawUrlSplits = rawUrl.split("\\?");
        this.url = rawUrlSplits[0];
        //3、解析参数
        if (rawUrlSplits.length > 1) {
            parseParams(rawUrlSplits[1]);
        }

        //4、解析其他 header 项
        for (int i = 1; i < lines.length; i++) {
            if (StrUtil.isEmpty(lines[i])) {
                break;
            }
            int index = StrUtil.indexOf(lines[i], ':');
            String key = lines[i].substring(0, index);
            String value = lines[i].substring(index + 2);
            this.headerMap.put(key, value);
        }

        //5、解析 Cookie
        String cookieStr = headerMap.get("Cookie");
        if (!StrUtil.isEmpty(cookieStr)) {
            String[] cookieSplits = cookieStr.split(";");
            this.cookies = new Cookie[cookieSplits.length];
            for (int i = 0; i < cookieSplits.length; i++) {
                String[] entry = cookieSplits[i].split("=");
                this.cookies[i] = new Cookie(entry[0].trim(), entry[1].trim());
            }
            headerMap.remove("Cookie");
        } else {
            this.cookies = new Cookie[0];
        }

        //6、解析 body
        String s = headerMap.get("Content-Length");
        if (s != null && !"0".equals(s)) {
            parseBody(lines[lines.length - 1]);
        }
    }

    private void parseParams(String params) {
        String[] paramSplits = params.split("&");
        for (String paramStr : paramSplits) {
            String[] entry = paramStr.split("=");
            String[] values = entry[1].split(",");
            this.paramMap.put(entry[0], values);
        }
    }

    private void parseBody(String str) {
        parseParams(str);
    }


    public String getHost() {
        String host = headerMap.get("Host");
        if (StrUtil.isEmpty(host)) {
            return null;
        }
        int index = host.indexOf(":");
        if (index < 0) {
            return host;
        }
        return host.substring(0, index);
    }

    public RequestDispatcher getRequestDispatcher(String url) {
        this.url = url;
        return requestDispatcher;
    }


    public HttpSession getSession(boolean createIfNotExists) {
        if (session != null) {
            return session;
        }
        for (Cookie cookie : cookies) {
            if ("JSESSIONID".equals(cookie.getKey())) {
                session = context.getSession(cookie.getValue());
                if (session != null) {
                    return session;
                }
            }
        }
        if (!createIfNotExists) {
            return null;
        }
        session = context.createSession(nioProcessor.getResponse());
        return session;
    }

    @Override
    public String toString() {
        String sessionId = null;
        if (session != null) {
            sessionId = session.getId();
        } else {
            for (Cookie cookie : cookies) {
                if ("JSESSIONID".equals(cookie.getKey())) {
                    sessionId = cookie.getValue();
                }
            }
        }
        return "Request : " +
                "Method = " + method +
                ", URL = '" + url + "\'" +
                ", JSESSIONID = " + sessionId;
    }

    public HttpSession getSession() {
        return getSession(false);
    }

    public String getParameter(String name) {
        return paramMap.get(name)[0];
    }

    public void setAttribute(String name, Object value) {
        attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }
}
