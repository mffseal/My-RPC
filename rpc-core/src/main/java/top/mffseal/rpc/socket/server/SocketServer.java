package top.mffseal.rpc.socket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.RequestHandler;
import top.mffseal.rpc.RpcServer;
import top.mffseal.rpc.config.Config;
import top.mffseal.rpc.registry.ServiceRegistry;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * rpc服务端，基于原生Socket网络通讯，实现：
 * 使用一个ServerSocket监听某个端口，循环接收连接请求；
 * 对新请求创建一个线程，将请求包装成RequestHandlerThread；
 * 将RequestHandlerThread交给新线程处理。
 *
 * @author mffseal
 */
public class SocketServer implements RpcServer {
    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);

    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 50;
    private static final int KEEP_ALIVE_TIME = 60;
    private static final int BLOCKING_QUEUE_CAPACITY = 100;
    private final ExecutorService threadPool;
    private final ServiceRegistry serviceRegistry;
    private final RequestHandler requestHandler = new RequestHandler();

    /**
     * 初始化工作线程池。
     */
    public SocketServer(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;

        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(100);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, CORE_POOL_SIZE, TimeUnit.SECONDS, workQueue, threadFactory);
    }

    /**
     * 注册服务。
     */
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(Config.getServerPort())) {
            logger.info("服务器启动中...");
            Socket socket;
            logger.info("{}", serverSocket);
            // 每收到一个请求，就创建一个工作线程
            while ((socket = serverSocket.accept()) != null) {
                logger.info("客户端连接! 地址: " + socket.getInetAddress() + ":" + socket.getPort());
                threadPool.execute(new RequestHandlerThread(socket, requestHandler, serviceRegistry));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("连接时发生错误: ", e);
        }
    }

}
