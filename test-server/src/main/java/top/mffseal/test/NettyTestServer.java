package top.mffseal.test;

import top.mffseal.rpc.RpcServer;
import top.mffseal.rpc.api.HelloService;
import top.mffseal.rpc.netty.server.NettyServer;

/**
 * @author mffseal
 */
public class NettyTestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        RpcServer rpcServer = new NettyServer();
        rpcServer.publishService(helloService, HelloService.class);
        rpcServer.start();
    }
}
