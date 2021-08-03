package catalina.connector;

import lombok.Data;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @author JwZheng
 * @date 2021/7/27 16:39
 */
@Data
public class SocketProcessor implements Runnable {
    private final Endpoint endpoint;
    private final SocketChannel socketChannel;
    private final Poller poller;
    private final boolean isNewSocket;
    private volatile long waitBegin;
    private volatile boolean isWorking;


    public SocketProcessor(Endpoint endpoint, SocketChannel socketChannel, Poller poller, boolean isNewSocket) {
        this.endpoint = endpoint;
        this.socketChannel = socketChannel;
        this.poller = poller;
        this.isNewSocket = isNewSocket;
        this.isWorking = false;
    }

    @Override
    public synchronized void run() {
        isWorking = true;
        getConnectionHandler().process(this);
        isWorking = false;
    }

    public void close() throws IOException {
        socketChannel.keyFor(poller.getSelector()).cancel();
        isWorking = false;
        socketChannel.close();
    }

    public int read(ByteBuffer buffer) throws IOException {
        if (!isConnected()) {
            return -1;
        }
        return socketChannel.read(buffer);
    }

    public void write(ByteBuffer[] byteBuffers) throws IOException {
        if (!isConnected()) {
            return;
        }
        for (ByteBuffer buffer : byteBuffers) {
            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
        }
    }

    public void register(boolean isNewSocket) {
        poller.register(socketChannel, isNewSocket);
    }

    public Http11ConnectionHandler getConnectionHandler() {
        return endpoint.getConnectionHandler();
    }

    public boolean isTimeout() {
        return System.currentTimeMillis() - waitBegin > getConnectionTimeout();
    }

    public boolean isConnected() {
        return socketChannel.isConnected();
    }

    public boolean isWorking() {
        return isWorking;
    }

    public long getConnectionTimeout() {
        return endpoint.getConnectionTimeout();
    }

    @Override
    public String toString() {
        return "SocketProcessor{" +
                "socketChannel=" + socketChannel +
                '}';
    }
}
