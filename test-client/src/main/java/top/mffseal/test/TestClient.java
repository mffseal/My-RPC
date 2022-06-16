package top.mffseal.test;

import top.mffseal.rpc.api.HelloObject;
import top.mffseal.rpc.api.HelloService;
import top.mffseal.rpc.client.RpcClientProxy;

/**
 * @author mffseal
 */
public class TestClient {
    public static void main(String[] args) {
        // 客户端增强代理
        // 拦截器会拦截调用请求，将调用包装成RpcRequest并使用RpcClient将请求发送到服务端
        RpcClientProxy proxy = new RpcClientProxy("127.0.0.1", 9000);
        // 用Proxy生成一个目标接口的代理对象，HelloService被代理了，所有方法会被拦截
        HelloService helloService = proxy.getProxy(HelloService.class);
        // 构造测试用调用参数
        HelloObject object = new HelloObject(12, "This is a message");

        String res = helloService.hello(object);
        System.out.println(res);
    }
}
