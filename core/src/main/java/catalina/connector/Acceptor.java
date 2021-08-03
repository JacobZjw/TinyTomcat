package catalina.connector;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * 请求监听线程
 * @author JwZheng
 * @date 2021/7/27 17:23
 */
@AllArgsConstructor
public class Acceptor implements Runnable{
    private final Endpoint endpoint;

    /**
     * 接收客户端的 SocketChannel 并注册到 Poller 中
     */
    @Override
    public void run() {
        while (endpoint.isRunning()){
            SocketChannel client;
            try {
                client = endpoint.accept();
                if (client == null){
                    continue;
                }
                client.configureBlocking(false);
                endpoint.registerToPoller(client);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return "Acceptor{}";
    }
}
