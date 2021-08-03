package http;

import catalina.Context;
import cn.hutool.core.util.ZipUtil;
import lombok.Data;
import util.HttpStatus;

import java.nio.ByteBuffer;
import java.util.*;

import static util.Constant.*;

/**
 * 封装Response对象
 *
 * @author JwZheng
 * @date 2021/7/22 13:56
 */
@Data
public class Response {
    private HttpStatus status;
    private String contentType;
    private boolean isGzip;

    private byte[] body;

    private List<Cookie> cookies;
    private Map<String, String> headerMap;
    private StringBuilder headerBuilder;

    private Context context;
    private boolean isRedirect;


    public Response(HttpStatus status, String contentType) {
        this.status = status;
        this.contentType = contentType;
        this.headerBuilder = new StringBuilder();
        this.headerMap = new HashMap<>();
        this.cookies = new ArrayList<>();
        this.body = new byte[0];
    }

    public Response(HttpStatus status) {
        this(status, DEFAULT_CONTENT_TYPE);
    }

    public Response() {
        this(HttpStatus.ACCEPTED);
    }

    public void addHeader(String name, String value) {
        headerMap.put(name, value);
    }

    public String getHeader() {
        return headerBuilder.toString();
    }


    private void buildHeader() {
        headerBuilder.append("HTTP/1.1").append(BLANK).append(status.getCode()).append(BLANK).append(status).append(CRLF);
        headerBuilder.append("Date:").append(BLANK).append(new Date()).append(CRLF);
        headerBuilder.append("Content-Type:").append(BLANK).append(contentType).append(CRLF);
        for (Cookie cookie : cookies) {
            headerBuilder.append("Set-Cookie:").append(BLANK).append(cookie.getKey()).append("=").append(cookie.getValue()).append(CRLF);
        }
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            headerBuilder.append(entry.getKey()).append(":").append(BLANK).append(entry.getValue()).append(CRLF);
        }
        headerBuilder.append("Content-Length:").append(BLANK).append(body.length).append(CRLF).append(CRLF);
    }

    public void setBody(byte[] bytes, boolean isGzip) {
        this.isGzip = isGzip;
        if (isGzip) {
            addHeader("Content-Encoding", "gzip");
            bytes = ZipUtil.gzip(bytes);
        }
        this.body = bytes;
    }

    public void setBody(byte[] bytes) {
        setBody(bytes, isGzip);
    }

    public ByteBuffer[] getResponseByteBuffer() {
        buildHeader();
        return new ByteBuffer[]{ByteBuffer.wrap(getHeader().getBytes()), ByteBuffer.wrap(body)};
    }

    public void sendRedirect(String url) {
        setStatus(HttpStatus.MOVED_TEMP);
        addHeader("Location", url);
        buildHeader();
        isRedirect = true;
    }

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

}
