package catalina.connector;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author JwZheng
 * @date 2021/7/27 16:33
 */
@Slf4j
@Data
public class Poller implements Runnable {
    private String name;
    private Endpoint endpoint;

    private Map<SocketChannel, Http11NioProcessor> nioProcessorMap;
    private Map<SocketChannel, SocketProcessor> socketProcessorMap;

    private Selector selector;
    private Queue<PollerEvent> events;

    public Poller(String name, Endpoint endpoint, Map<SocketChannel, Http11NioProcessor> nioProcessorMap, Map<SocketChannel, SocketProcessor> socketProcessorMap) throws IOException {
        this.name = name;
        this.endpoint = endpoint;
        this.nioProcessorMap = nioProcessorMap;
        this.socketProcessorMap = socketProcessorMap;

        this.selector = Selector.open();
        this.events = new ConcurrentLinkedQueue<>();
    }

    public CoyoteAdapter getCoyoteAdapter() {
        return endpoint.getCoyoteAdapter();
    }

    @Override
    public void run() {
        while (endpoint.isRunning()) {
            try {
                events();
                if (selector.select() <= 0) {
                    continue;
                }
                for (Iterator<SelectionKey> iterator = selector.selectedKeys().iterator(); iterator.hasNext(); ) {
                    SelectionKey key = iterator.next();
                    if (key.isValid() && key.isReadable()) {
                        SocketProcessor processor = (SocketProcessor) key.attachment();
                        if (processor != null) {
                            executeProcessor(processor);
                        }
                    }
                    iterator.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClosedSelectorException e) {
                log.info("{} 对应的selector 已关闭", this.name);
            }

        }
    }

    private void executeProcessor(SocketProcessor processor) {
        processor.setWorking(true);
        endpoint.execute(processor);
    }

    public void close() throws IOException {
        for (SocketProcessor socketProcessor : socketProcessorMap.values()) {
            socketProcessor.close();
        }
        events.clear();
        selector.close();
    }

    private boolean events() {
        boolean res = false;
        PollerEvent pollerEvent;
        while (!events.isEmpty()) {
            pollerEvent = events.poll();
            if (pollerEvent == null) {
                break;
            }
            pollerEvent.run();
            res = true;
        }
        return res;
    }


    /**
     * 将 socket 注册到 Poller 中,并初始化或重置 waitBegin
     *
     * @param socketChannel socketChannel
     * @param isNewSocket   是否是新的 socket
     */
    public void register(SocketChannel socketChannel, boolean isNewSocket) {
        SocketProcessor socketProcessor;
        if (isNewSocket) {
            socketProcessor = new SocketProcessor(endpoint, socketChannel, this, isNewSocket);
            Http11NioProcessor nioProcessor = new Http11NioProcessor(socketProcessor, getCoyoteAdapter());
            nioProcessorMap.put(socketChannel, nioProcessor);
            socketProcessorMap.put(socketChannel, socketProcessor);
        } else {
            socketProcessor = socketProcessorMap.get(socketChannel);
            socketProcessor.setWorking(false);
        }
        socketProcessor.setWaitBegin(System.currentTimeMillis());
        events.offer(new PollerEvent(socketProcessor));
        selector.wakeup();
    }

    @Data
    @AllArgsConstructor
    private static class PollerEvent implements Runnable {
        private SocketProcessor socketProcessor;

        @Override
        public void run() {
            try {
                socketProcessor.getSocketChannel().register(socketProcessor.getPoller().getSelector(), SelectionKey.OP_READ, socketProcessor);
            } catch (ClosedChannelException e) {
                log.error("Socket{} 已经被关闭，无法注册到 Poller", socketProcessor.getSocketChannel());
            }
        }
    }

    @Override
    public String toString() {
        return "Poller{" +
                "name='" + name + '\'' +
                '}';
    }
}
