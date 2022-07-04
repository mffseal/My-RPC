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
        // 轮询算法的效果要同一个客户端多次调用同一个接口才能看出
        String res = helloServer.hello(helloObject);
        String res2 = helloServer.hello(helloObject);
        String res3 = helloServer.hello(helloObject);
        System.out.println(res);
    }
}
