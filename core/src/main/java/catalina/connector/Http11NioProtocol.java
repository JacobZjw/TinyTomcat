package catalina.connector;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author JwZheng
 * @date 2021/7/27 15:34
 */
@Data
@NoArgsConstructor
public class Http11NioProtocol {
    private Connector parent;
    private Endpoint endpoint;
    private Http11ConnectionHandler connectionHandler;

    private ScheduledExecutorService executor;

    public Http11NioProtocol(Connector parent) {
        Map<SocketChannel, SocketProcessor> socketProcessorMap = new ConcurrentHashMap<>();
        Map<SocketChannel, Http11NioProcessor> nioProcessorMap = new ConcurrentHashMap<>();
        this.connectionHandler = new Http11ConnectionHandler(nioProcessorMap, socketProcessorMap);
        this.endpoint = new Endpoint(this, socketProcessorMap, nioProcessorMap);
        this.parent = parent;
    }

    public void start() throws IOException {
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "ConnCleaner");
            }
        };
        executor = Executors.newSingleThreadScheduledExecutor(threadFactory);
        executor.scheduleWithFixedDelay(connectionHandler, 5, 5, TimeUnit.SECONDS);
        endpoint.start();
    }

    public Connector getConnector() {
        return parent;
    }

    public void shutdown() throws IOException {
        endpoint.shutdown();
        connectionHandler.closeALL();
    }

    @Override
    public String toString() {
        return "Http11NioProtocol{}";
    }
}
