package top.mffseal.test;

import top.mffseal.rpc.api.HelloObject;
import top.mffseal.rpc.api.HelloService;
import top.mffseal.rpc.transport.RpcClient;
import top.mffseal.rpc.transport.RpcClientProxy;
import top.mffseal.rpc.transport.netty.client.NettyClient;

/**
 * @author mffseal
 */
public class NettyTestClient {
    public static void main(String[] args) {
        RpcClient client = new NettyClient();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        HelloService helloServer = rpcClientProxy.getProxy(HelloService.class);
        HelloObject helloObject = new HelloObject(12, "this is a message");
        helloServer.hello(helloObject);
    }
}
