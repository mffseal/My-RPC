package top.mffseal.rpc.client;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.RpcRequest;
import top.mffseal.rpc.entity.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 一个拦截器，该类不会解析从服务端收到的数据，
 * 直接交给RpcClient处理。
 * @author mffseal
 */

public class RpcClientProxy implements InvocationHandler {
    private String host;
    private int port;
    private static final Logger logger = LoggerFactory.getLogger(RpcClientProxy.class);

    public RpcClientProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 获取一个代理对象，代理掉目标类，应用当前拦截器。
     * @param clazz 目标类
     * @return 代理对象
     * @param <T> 目标类的类型
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * 拦截器拦截掉所有方法，并为每个方法生成一个RpcRequest对象，
     * 并实例化一个RpcClient发送该RpcRequest，等待RpcResponse返回。
     *
     * @param proxy 要在哪个代理对象上调用该方法。
     *
     * @param method 原始方法，这里在客户端本地是没有方法实现的，
     *               所以只用作承载方法名、参数类型。
     *
     * @param args 用来传递给方法的参数，同一为Object类型。
     *
     * @return RpcResponse
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        logger.info("调用方法: {}#{}", method.getDeclaringClass().getName(), method.getName());
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(args)
                .paramTypes(method.getParameterTypes())
                .build();
        RpcClient rpcClient = new RpcClient();
        // proxy不负责解析收到的RpcResponse
        return rpcClient.sendRequest(rpcRequest, host, port);
    }
}
