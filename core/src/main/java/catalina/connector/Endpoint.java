package catalina.connector;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static util.Constant.POLLER_THREAD_COUNT;

/**
 * @author JwZheng
 * @date 2021/7/27 15:31
 */
@Data
@Slf4j
public class Endpoint {

    private Http11NioProtocol parent;

    /**请求处理线程池**/
    private Executor executor;
    /**
     * 负责接收 Socket 线程
     */
    private Acceptor acceptor;

    private List<Poller> pollerList;

    private volatile boolean isRunning;
    private ServerSocketChannel socketChannel;

    /**
     * Poller 轮询指针
     */
    private AtomicInteger pollerIndex = new AtomicInteger(0);

    private Map<SocketChannel, SocketProcessor> socketProcessorMap;
    private Map<SocketChannel, Http11NioProcessor> nioProcessorMap;

    public Endpoint(Http11NioProtocol parent, Map<SocketChannel, SocketProcessor> socketProcessorMap, Map<SocketChannel, Http11NioProcessor> nioProcessorMap) {
        this.parent = parent;
        this.socketProcessorMap = socketProcessorMap;
        this.nioProcessorMap = nioProcessorMap;
    }

    public Http11ConnectionHandler getConnectionHandler() {
        return parent.getConnectionHandler();
    }

    public Connector getConnector() {
        return parent.getConnector();
    }

    public CoyoteAdapter getCoyoteAdapter() {
        return getConnector().getCoyoteAdapter();
    }

    private void init() throws IOException {
        this.executor = new Executor();

        this.socketChannel = ServerSocketChannel.open();
        this.socketChannel.bind(new InetSocketAddress(getConnector().getPort()));
        this.socketChannel.configureBlocking(true);

        this.pollerList = new ArrayList<>(POLLER_THREAD_COUNT);
        for (int i = 0; i < POLLER_THREAD_COUNT; i++) {
            String pollerName = "Poller-" + i;
            Poller poller = new Poller(pollerName, this, nioProcessorMap, socketProcessorMap);
            Thread thread = new Thread(poller, pollerName);
            thread.setDaemon(true);
            thread.start();
            pollerList.add(poller);
        }

        this.acceptor = new Acceptor(this);
        Thread acceptor = new Thread(this.acceptor, "Acceptor");
        acceptor.setDaemon(true);
        acceptor.start();
    }

    public void execute(SocketProcessor processor) {
        executor.execute(processor);
    }

    public void start() throws IOException {
        this.isRunning = true;
        init();
    }

    public void shutdown() {
        isRunning = false;
        for (Poller poller : pollerList) {
            try {
                poller.close();
            } catch (IOException e) {
                log.error("{} 关闭失败",poller);
                e.printStackTrace();
            }
        }
        executor.shutdown();
        try {
            socketChannel.close();
        } catch (IOException e) {
            log.error("{} 关闭失败",socketChannel);
            e.printStackTrace();
        }
    }

    public SocketChannel accept() throws IOException {
        return socketChannel.accept();
    }

    public void registerToPoller(SocketChannel client) throws IOException {
        socketChannel.configureBlocking(false);
        getPoller().register(client, true);
        socketChannel.configureBlocking(true);
    }

    /**
     * 轮询 Poller
     *
     * @return Poller
     */
    private Poller getPoller() {
        int index = Math.abs(pollerIndex.incrementAndGet()) % pollerList.size();
        return pollerList.get(index);
    }

    public Long getConnectionTimeout() {
        return getConnector().getConnectionTimeout();
    }

    @Override
    public String toString() {
        return "Endpoint{}";
    }
}
