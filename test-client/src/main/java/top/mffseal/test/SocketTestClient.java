package top.mffseal.test;

import top.mffseal.rpc.api.ByeService;
import top.mffseal.rpc.api.HelloObject;
import top.mffseal.rpc.api.HelloService;
import top.mffseal.rpc.transport.RpcClient;
import top.mffseal.rpc.transport.RpcClientProxy;
import top.mffseal.rpc.transport.socket.client.SocketClient;

/**
 * 测试用Socket客户端。
 *
 * @author mffseal
 */
public class SocketTestClient {
    public static void main(String[] args) {
        RpcClient client = new SocketClient();
        // 客户端增强代理
        // 拦截器会拦截调用请求，将调用包装成RpcRequest并使用RpcClient将请求发送到服务端
        RpcClientProxy proxy = new RpcClientProxy(client);
        // 用Proxy生成一个目标接口的代理对象，HelloService被代理了，所有方法会被拦截
        HelloService helloService = proxy.getProxy(HelloService.class);
        // 构造测试用调用参数
        HelloObject object = new HelloObject(12, "This is a message");

        String res = helloService.hello(object);
        System.out.println(res);

        ByeService byeService = proxy.getProxy(ByeService.class);
        String res2 = byeService.bye("socketClient");
        System.out.println(res2);
    }
}
