package catalina.connector;

import catalina.Engine;
import catalina.Service;
import lombok.Data;

import java.io.IOException;

/**
 * @author JwZheng
 * @date 2021/7/26 14:49
 */
@Data
public class Connector {
    private Service parent;
    private Integer port;
    private Long connectionTimeout = 10000L;

    private Http11NioProtocol http11NioProtocol;
    private CoyoteAdapter coyoteAdapter;

    public Connector(Service parent, Integer port) {
        this.parent = parent;
        this.port = port;
        this.coyoteAdapter = new CoyoteAdapter(this);
        this.http11NioProtocol = new Http11NioProtocol(this);
    }

    public void start() throws IOException {
        http11NioProtocol.start();
    }

    public void shutdown() throws IOException {
        http11NioProtocol.shutdown();
    }

    @Override
    public String toString() {
        return "Connector{" +
                "port=" + port +
                ", connectionTimeout=" + connectionTimeout +
                '}';
    }

    public Engine getEngine() {
        return parent.getEngine();
    }
}
