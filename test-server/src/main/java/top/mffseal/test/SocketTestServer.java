package top.mffseal.test;

import top.mffseal.rpc.api.HelloService;
import top.mffseal.rpc.transport.RpcServer;
import top.mffseal.rpc.transport.socket.server.SocketServer;

/**
 * @author mffseal
 */
public class SocketTestServer {
    public static void main(String[] args) {
        // 具体的服务实现
        HelloService helloService = new HelloServiceImpl();
        RpcServer rpcServer = new SocketServer();
        // 注册服务
        rpcServer.publishService(helloService, HelloService.class);
        // 启动服务器
        rpcServer.start();
    }
}
