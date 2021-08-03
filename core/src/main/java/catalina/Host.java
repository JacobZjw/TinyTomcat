package catalina;

import exception.ServletException;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * @author JwZheng
 * @date 2021/7/26 14:49
 */
@Data
@AllArgsConstructor
public class Host {
    private Engine parent;
    private String name;
    private String appBase;
    // path——context
    private Map<String,Context> contextMap;

    public Host(Engine parent, String name, String appBase) {
        this.parent = parent;
        this.name = name;
        this.appBase = appBase;
    }

    public void start() throws Exception {
        for (Context context : contextMap.values()) {
            context.init();
        }
    }

    public void shutdown() throws ServletException {
        for (Context context : contextMap.values()) {
            context.destroy();
        }
    }

    @Override
    public String toString() {
        return "Host{" +
                "name='" + name + '\'' +
                ", appBase='" + appBase + '\'' +
                '}';
    }
}
