package top.mffseal.rpc.transport.socket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.config.RpcServerConfig;
import top.mffseal.rpc.factory.ThreadPoolFactory;
import top.mffseal.rpc.handler.ServerInvokeHandler;
import top.mffseal.rpc.hook.ShutdownHook;
import top.mffseal.rpc.provider.ServiceProviderImpl;
import top.mffseal.rpc.registry.NacosServiceRegistry;
import top.mffseal.rpc.transport.AbstractRpcServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * rpc服务端，基于原生Socket网络通讯，实现：
 * 使用一个ServerSocket监听某个端口，循环接收连接请求；
 * 对新请求创建一个线程，将请求包装成RequestHandlerThread；
 * 将RequestHandlerThread交给新线程处理。
 *
 * @author mffseal
 */
public class SocketServer extends AbstractRpcServer {
    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);


    private final ExecutorService threadPool;
    private final ServerInvokeHandler serverInvokeHandler = new ServerInvokeHandler();

    /**
     * 初始化工作线程池。
     */
    public SocketServer() {
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
        threadPool = ThreadPoolFactory.createDefaultThreadPool("SocketRpcServer");
        scanServices();
    }

    /**
     * 注册服务。
     */
    public void start() {
        // 关闭后注销服务
        ShutdownHook.getShutdownHock().addClearAllHock();

        try (ServerSocket serverSocket = new ServerSocket(RpcServerConfig.getPort())) {
            logger.info("服务器启动中...");
            Socket socket;
            logger.info("{}", serverSocket);
            // 每收到一个请求，就创建一个工作线程
            while ((socket = serverSocket.accept()) != null) {
                logger.info("客户端连接! 地址: " + socket.getInetAddress() + ":" + socket.getPort());
                threadPool.execute(new RequestHandlerThread(socket, serverInvokeHandler));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("连接时发生错误: ", e);
        }
    }
}
