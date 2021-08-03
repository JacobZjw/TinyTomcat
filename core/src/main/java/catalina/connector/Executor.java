package catalina.connector;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author JwZheng
 * @date 2021/7/27 17:29
 */
@Slf4j

public class Executor implements java.util.concurrent.Executor {
    private final ThreadPoolExecutor pool;

    public Executor() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count;

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "Executor-" + count++);
            }
        };
        this.pool = new ThreadPoolExecutor(100, 100, 1, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(200), threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public void shutdown() {
        pool.shutdown();
    }

    @Override
    public void execute(Runnable command) {
        pool.execute(command);
    }
}
