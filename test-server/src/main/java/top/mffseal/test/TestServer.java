package top.mffseal.test;

import top.mffseal.rpc.api.HelloService;
import top.mffseal.rpc.server.RpcServer;

/**
 * @author mffseal
 */
public class TestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        RpcServer rpcServer = new RpcServer();
        rpcServer.register(helloService, 9000);
    }
}
