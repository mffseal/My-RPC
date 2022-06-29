package top.mffseal.test;

import top.mffseal.rpc.RpcServer;
import top.mffseal.rpc.api.HelloService;
import top.mffseal.rpc.netty.server.NettyServer;
import top.mffseal.rpc.registry.DefaultServiceRegistry;
import top.mffseal.rpc.registry.ServiceRegistry;

/**
 * @author mffseal
 */
public class NettyTestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        ServiceRegistry serviceRegistry = new DefaultServiceRegistry();
        serviceRegistry.register(helloService);
        RpcServer rpcServer = new NettyServer();
        rpcServer.start(8080);
    }
}
