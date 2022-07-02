package top.mffseal.rpc.transport.netty.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.RpcRequestMessage;
import top.mffseal.rpc.entity.RpcResponseMessage;
import top.mffseal.rpc.registry.NacosServiceDiscovery;
import top.mffseal.rpc.registry.ServiceDiscovery;
import top.mffseal.rpc.transport.RpcClient;
import top.mffseal.rpc.util.RpcMessageChecker;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author mffseal
 */
@ChannelHandler.Sharable
public class NettyClient implements RpcClient {
    private static final Logger log = LoggerFactory.getLogger(NettyClient.class);
    private final ServiceDiscovery serviceDiscovery;

    public NettyClient() {
        serviceDiscovery = new NacosServiceDiscovery();
    }

    @Override
    public Object sendRequest(RpcRequestMessage rpcRequestMessage) {
        // 查询服务提供者地址
        InetSocketAddress inetSocketAddress = serviceDiscovery.lookupService(rpcRequestMessage.getInterfaceName());
        AtomicReference<Object> result = new AtomicReference<>(null);
        try {
            Channel channel = ChannelProvider.get(inetSocketAddress);
            if (channel.isActive()) {
                // 向服务端发送请求，并通过回调获取服务端响应结果
                channel.writeAndFlush(rpcRequestMessage).addListener(future1 -> {
                    if (future1.isSuccess()) {
                        log.info("客户端发送消息成功: {}", rpcRequestMessage);
                    } else {
                        log.error("客户端发送消息失败: ", future1.cause());
                    }
                });
                channel.closeFuture().sync();

                // 获得返回结果 RpcResponse 后，将这个对象以 key 为 ”rpcResponse“ 放入 ChannelHandlerContext 中
                AttributeKey<RpcResponseMessage<?>> key = AttributeKey.valueOf("rpcResponse" + rpcRequestMessage.getSequenceId());
                RpcResponseMessage<?> rpcResponseMessage = channel.attr(key).get();
                RpcMessageChecker.check(rpcRequestMessage, rpcResponseMessage);
                result.set(rpcResponseMessage.getData());
            } else {
                // 结束客户端主线程
                System.exit(0);
            }
        } catch (InterruptedException e) {
            log.error("发送消息失败: ", e);
        }
        return result.get();
    }
}
