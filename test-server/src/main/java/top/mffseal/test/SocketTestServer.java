package top.mffseal.test;

import top.mffseal.rpc.RpcServer;
import top.mffseal.rpc.api.HelloService;
import top.mffseal.rpc.registry.DefaultServiceRegistry;
import top.mffseal.rpc.registry.ServiceRegistry;
import top.mffseal.rpc.socket.server.SocketServer;

/**
 * @author mffseal
 */
public class SocketTestServer {
    public static void main(String[] args) {
        // 具体的服务实现
        HelloService helloService = new HelloServiceImpl();
        ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
        // 注册服务
        serviceRegistry.register(helloService);

        RpcServer rpcServer = new SocketServer(serviceRegistry);
        // 启动服务器
        rpcServer.start();
    }
}
