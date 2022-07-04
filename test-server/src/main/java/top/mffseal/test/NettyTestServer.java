package top.mffseal.test;

import top.mffseal.rpc.api.HelloService;
import top.mffseal.rpc.transport.RpcServer;
import top.mffseal.rpc.transport.netty.server.NettyServer;

/**
 * 测试用Netty服务端
 *
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
