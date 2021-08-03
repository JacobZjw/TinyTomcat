package catalina.connector;

import cn.hutool.core.util.StrUtil;
import exception.ServletException;
import http.Request;
import http.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import util.Handler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * 与客户端的数据读写
 *
 * @author JwZheng
 * @date 2021/7/28 16:55
 */
@AllArgsConstructor
@Data
@Slf4j
public class Http11NioProcessor {

    private final CoyoteAdapter coyoteAdapter;
    private Request request;
    private Response response;
    private volatile boolean isFinished;
    private SocketProcessor socketProcessor;

    public Http11NioProcessor(SocketProcessor socketProcessor, CoyoteAdapter coyoteAdapter) {
        this.socketProcessor = socketProcessor;
        this.coyoteAdapter = coyoteAdapter;
    }

    public synchronized void process() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            isFinished = false;
            int len;
            while ((len = socketProcessor.read(buffer)) > 0) {
                buffer.flip();
                bos.write(buffer.array(), 0, len);
                //坑：clear() 并不会擦除数据，只是重置位置
                buffer.clear();
            }
            bos.close();
            if (bos.size() == 0) {
                return;
            }
            byte[] bytes = bos.toByteArray();
            this.request = new Request(bytes, this);
            //BUG记录:在构造器初始化 Response 导致多线程写入Response 出现数据错乱
            this.response = new Response();
            log.info(request.toString());
            coyoteAdapter.doDispatch(request, response);
            isFinished = true;
        } catch (ServletException e) {
            Handler.handleException(e, response);
            isFinished = true;
        } catch (IOException ignored) {

        } finally {
            flush();
        }
    }

    /**
     * 向客户端写入数据，并判断是否需要关闭连接，默认KEEPALIVE
     */
    public void flush() {
        if (!isFinished) {
            return;
        }
        ByteBuffer[] byteBuffer = response.getResponseByteBuffer();
        try {
            socketProcessor.write(byteBuffer);
            String connection = request.getHeaderMap().get("Connection");
            if (StrUtil.isNotEmpty(connection) && connection.equals("close") || !socketProcessor.isConnected()) {
                log.debug("CLOSE: 客户端连接{} 已关闭", socketProcessor.getSocketChannel());
                socketProcessor.close();
            } else {
                log.debug("KEEPALIVE: 客户端连接{} 重新注册到Poller中", socketProcessor.getSocketChannel());
                socketProcessor.register(false);
            }
        } catch (IOException e) {
            log.error(e.toString());
        } finally {
            isFinished = false;
        }
    }

    public void close() throws IOException {
        flush();
        socketProcessor.close();
    }

    @Override
    public String toString() {
        return "Http11NioProcessor{}";
    }
}
