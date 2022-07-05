package top.mffseal.rpc.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.RpcRequestMessage;
import top.mffseal.rpc.entity.RpcResponseMessage;
import top.mffseal.rpc.transport.netty.client.NettyClient;
import top.mffseal.rpc.transport.socket.client.SocketClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 一个代理拦截器，负责将本地调用拦截到rpc调用；
 * 该类不负责解析从服务端收到的数据，直接交给RpcClient处理。
 *
 * @author mffseal
 */

public class RpcClientProxy implements InvocationHandler {
    private static final Logger log = LoggerFactory.getLogger(RpcClientProxy.class);

    private final RpcClient client;

    public RpcClientProxy(RpcClient client) {
        this.client = client;
    }

    /**
     * 获取一个代理对象，代理掉目标类，应用当前拦截器。
     *
     * @param clazz 目标类
     * @param <T>   目标类的类型
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class<?>[]{clazz}, this);
    }

    /**
     * 拦截器拦截掉所有方法，并为每个方法生成一个RpcRequest对象，
     * 并实例化一个RpcClient发送该RpcRequest，等待RpcResponse返回。
     *
     * @param proxy  要在哪个代理对象上调用该方法。
     * @param method 原始方法，这里在客户端本地是没有方法实现的，
     *               所以只用作承载方法名、参数类型。
     * @param args   用来传递给方法的参数，同一为Object类型。
     * @return RpcResponse
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        log.info("调用方法: {}#{}", method.getDeclaringClass().getName(), method.getName());
        RpcRequestMessage rpcRequestMessage = new RpcRequestMessage(UUID.randomUUID().toString(), method.getDeclaringClass().getName(),
                method.getName(), args, method.getParameterTypes(), false);
        Object result = null;

        // Netty实现和Socket实现上，对收到的响应处理方式不同

        // Netty客户端
        // proxy需要解析RpcResponse，调用getData
        if (client instanceof NettyClient) {
            try {
                CompletableFuture<RpcResponseMessage<?>> completableFuture = ((NettyClient) client).sendRequest(rpcRequestMessage);
                result = completableFuture.get().getData();  // 这里需要getData
            } catch (ExecutionException | InterruptedException e) {
                log.error("RPC调用失败", e);
            }
        }

        // Socket客户端
        // proxy不负责解析收到的RpcResponse，直接原封不动返回
        if (client instanceof SocketClient) {
            result = client.sendRequest(rpcRequestMessage);
        }

        return result;
    }
}
