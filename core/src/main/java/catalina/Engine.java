package catalina;

import exception.ServletException;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @author JwZheng
 * @date 2021/7/26 14:49
 */
@Data
@AllArgsConstructor
public class Engine {
    private Service parent;
    private String name;
    private Host defaultHost;
    private List<Host> hosts;

    public Engine(Service parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public void start() throws Exception {
        for (Host host : hosts) {
            host.start();
        }
    }

    public void shutdown() throws ServletException {
        for (Host host : hosts) {
            host.shutdown();
        }
    }

    @Override
    public String toString() {
        return "Engine{" +
                "name='" + name + '\'' +
                '}';
    }
}
