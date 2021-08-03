package http;

import catalina.Context;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author JwZheng
 * @date 2021/7/29 16:14
 */
public class HttpSession {
    @Getter
    private final String id;
    private Map<String,Object> attributes;

    @Getter
    private boolean isValid;

    @Getter
    private Instant lastAccessed;

    @Getter
    private final Context context;

    public HttpSession(String id,Context context) {
        this.id = id;
        this.context = context;
        this.attributes = new ConcurrentHashMap<>();
        this.isValid = true;
        this.lastAccessed = Instant.now();
    }

    public Object getAttribute(String key){
        if (isValid){
            this.lastAccessed = Instant.now();
            return attributes.get(key);
        }
        throw new IllegalStateException("Session has invalidated");
    }

    public void setAttribute(String key,Object value){
        if (isValid){
            this.lastAccessed = Instant.now();
            attributes.put(key,value);
            return;
        }
        throw new IllegalStateException("Session has invalidated");
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }


    public void invalidate(){
        this.isValid = false;
        this.attributes.clear();
        context.invalidateSession(id);
    }

    @Override
    public String toString() {
        return "HttpSession{" +
                "id='" + id + '\'' +
                '}';
    }
}
