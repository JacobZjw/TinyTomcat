package catalina.connector;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;

/**
 * @author JwZheng
 * @date 2021/7/27 19:46
 */
@AllArgsConstructor
@Slf4j
public class Http11ConnectionHandler implements Runnable {
    private Map<SocketChannel, Http11NioProcessor> nioProcessorMap;
    private Map<SocketChannel, SocketProcessor> socketProcessorMap;


    public void process(SocketProcessor socketProcessor) {
        Http11NioProcessor nioProcessor = nioProcessorMap.get(socketProcessor.getSocketChannel());
        if (nioProcessor == null) {
            return;
        }
        nioProcessor.process();
    }

    private synchronized void cleanTimeoutSockets() {
        for (Map.Entry<SocketChannel, SocketProcessor> entry : socketProcessorMap.entrySet()) {
            try {
                SocketChannel channel = entry.getKey();
                SocketProcessor processor = entry.getValue();
                if (!channel.isConnected()) {
                    log.debug(channel + "关闭连接");
                    socketProcessorMap.remove(channel);
                    nioProcessorMap.remove(channel);
                    continue;
                }
                if (processor.isWorking()) {
                    continue;
                }
                if (processor.isTimeout()) {
                    log.debug("{} KeepAlive 已过期", channel);
                    processor.close();
                    socketProcessorMap.remove(channel);
                    nioProcessorMap.remove(channel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void closeALL() throws IOException {
        for (SocketProcessor processor : socketProcessorMap.values()) {
            processor.close();
        }
    }

    @Override
    public void run() {
        cleanTimeoutSockets();
    }
}
