package top.mffseal.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * rpc服务端，使用一个ServerSocket监听某个端口，循环接收连接请求，
 * 如果发来了请求就创建一个线程，在新线程中处理调用。
 * @author mffseal
 */
public class RpcServer {
    private final ExecutorService threadPool;
    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    /**
     * 初始化工作线程池。
     */
    public RpcServer() {
        int corePoolSize = 5;
        int maximumPoolSize = 50;
        int keepAliveTime = 60;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(100);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue, threadFactory);
    }

    /**
     * 注册服务。
     * @param service 要注册的具体服务对象
     * @param port 端口
     */
    public void register(Object service, int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("服务器启动中...");
            Socket socket;
            logger.info("{}", serverSocket);
            // 每收到一个请求，就创建一个工作线程
            while ((socket = serverSocket.accept()) != null) {
                logger.info("客户端连接! IP: " + socket.getInetAddress());
                threadPool.execute(new WorkerThread(socket, service));
            }
        } catch (IOException e) {
            logger.error("连接时发生错误: ", e);
        }
    }



}
