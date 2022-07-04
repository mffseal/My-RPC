package top.mffseal.rpc.transport.netty.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.config.RpcClientConfig;
import top.mffseal.rpc.entity.RpcRequestMessage;
import top.mffseal.rpc.entity.RpcResponseMessage;
import top.mffseal.rpc.factory.SingletonFactory;
import top.mffseal.rpc.registry.NacosServiceDiscovery;
import top.mffseal.rpc.registry.ServiceDiscovery;
import top.mffseal.rpc.serializer.Serializer;
import top.mffseal.rpc.transport.RpcClient;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

/**
 * Netty客户端，负责调用服务发现和请求发送，并异步等待响应。
 *
 * @author mffseal
 */
@ChannelHandler.Sharable
public class NettyClient implements RpcClient {
    private static final Logger log = LoggerFactory.getLogger(NettyClient.class);
    private final ServiceDiscovery serviceDiscovery;
    private final Serializer.Library serializer = RpcClientConfig.getSerializerLibrary();
    private final ResponseLocker responseLocker;

    public NettyClient() {
        serviceDiscovery = new NacosServiceDiscovery();
        // 用单例模式保证和 NettyClientHandler 使用同一个快递柜
        responseLocker = SingletonFactory.getInstance(ResponseLocker.class);
    }

    /**
     * 查询服务注册平台后向服务提供者发送请求，并通过future获取响应。
     * 不涉及具体的服务查询操作，服务查询操作由ServiceDiscovery提供。
     * 不涉及具体的netty操作，netty操作由ChannelProvider提供。
     *
     * @param rpcRequestMessage rpc请求
     * @return rpc响应的future
     */
    @Override
    public CompletableFuture<RpcResponseMessage<?>> sendRequest(RpcRequestMessage rpcRequestMessage) {
        CompletableFuture<RpcResponseMessage<?>> resultFuture = new CompletableFuture<>();
        // 查询服务提供者地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequestMessage.getInterfaceName());
        // 向服务提供者建立连接
        Channel channel = ChannelProvider.get(inetSocketAddress, serializer);
        // 连接建立失败
        if (channel == null || !channel.isActive()) {
            log.error("无法与服务端 {} 建立连接", inetSocketAddress);
            return null;
        }

        // 向服务端发送请求，并非阻塞的等待响应
        responseLocker.put(rpcRequestMessage.getSequenceId(), resultFuture);
        // 通过回调的方式告知客户端请求发送成功
        channel.writeAndFlush(rpcRequestMessage).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                log.info("客户端发送消息成功: {}", rpcRequestMessage);
            } else {
                // 发送消息失败则直接删除对应future
                future.channel().close();
                resultFuture.completeExceptionally(future.cause());
                log.error("客户端发送消息失败: ", future.cause());
            }
        });
        return resultFuture;
    }
}
