package catalina;

import catalina.connector.Connector;
import exception.ServletException;
import lombok.Data;

import java.io.IOException;
import java.util.List;

/**
 * @author JwZheng
 * @date 2021/7/26 14:49
 */
@Data
public class Service {
    private String name;
    private Engine engine;
    private List<Connector> connectors;
    private Server parent;

    public Service(Server parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public void start() throws Exception {
        for (Connector connector : connectors) {
            connector.start();
        }
        engine.start();
    }

    public void shutdown() throws IOException, ServletException {
        for (Connector connector : connectors) {
            connector.shutdown();
        }
        engine.shutdown();
    }

    @Override
    public String toString() {
        return "Service{" +
                "name='" + name + '\'' +
                '}';
    }
}
