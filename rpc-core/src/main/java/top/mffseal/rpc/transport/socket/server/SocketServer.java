package top.mffseal.rpc.transport.socket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.config.Config;
import top.mffseal.rpc.handler.RequestHandler;
import top.mffseal.rpc.provider.ServiceProvider;
import top.mffseal.rpc.provider.ServiceProviderImpl;
import top.mffseal.rpc.registry.NacosServiceRegistry;
import top.mffseal.rpc.registry.ServiceRegistry;
import top.mffseal.rpc.transport.RpcServer;
import top.mffseal.rpc.util.ThreadPoolFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
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
public class SocketServer implements RpcServer {
    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);


    private final ExecutorService threadPool;
    private final ServiceProvider serviceProvider;
    private final ServiceRegistry serviceRegistry;
    private final RequestHandler requestHandler = new RequestHandler();

    /**
     * 初始化工作线程池。
     */
    public SocketServer() {
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
        threadPool = ThreadPoolFactory.createDefaultThreadPool("SocketRpcServer");
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
                threadPool.execute(new RequestHandlerThread(socket, requestHandler));
            }
            threadPool.shutdown();
        } catch (IOException e) {
            logger.error("连接时发生错误: ", e);
        }
    }

    @Override
    public <T> void publishService(T service, Class<T> serviceClass) {
        serviceProvider.addServiceProvider(service, serviceClass);
        serviceRegistry.register(serviceClass.getCanonicalName(), new InetSocketAddress(Config.getServerHost(), Config.getServerPort()));
    }

}
