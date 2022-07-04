package top.mffseal.rpc.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.codec.MessageCodec;
import top.mffseal.rpc.codec.ProtocolFrameDecoder;
import top.mffseal.rpc.config.RpcClientConfig;
import top.mffseal.rpc.handler.ResponseHandler;
import top.mffseal.rpc.serializer.Serializer;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 客户端连接管理器。
 *
 * @author mffseal
 */
public class ChannelProvider {
    private static final Logger log = LoggerFactory.getLogger(ChannelProvider.class);
    private static EventLoopGroup eventExecutors;
    private static final Bootstrap bootstrap = initBootstrap();
    private static LoggingHandler loggingHandler;
    private static ResponseHandler responseHandler;
    /**
     * 一个客户端可能同时连接多个服务提供者，所以需要用集合记录所有连接。
     */
    private static final Map<String, Channel> channelMap = new ConcurrentHashMap<>();

    /**
     * 获取一个目标服务端的连接。
     *
     * @param serverAddress 服务端地址
     * @return 连接channel
     */
    public static Channel get(InetSocketAddress serverAddress, Serializer.Library serializer) {
        // 客户端可以同时使用不同的序列化方案，这里特征需要加上序列化方案编号
        String target = serverAddress.toString() + serializer.ordinal();
        if (channelMap.containsKey(target)) {
            Channel channel = channelMap.get(target);
            // 删除失效的channel
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channelMap.remove(target);
            }
        }

        // 客户端需要channel后再组装bootstrap
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new ProtocolFrameDecoder());
                ch.pipeline().addLast(loggingHandler);
                ch.pipeline().addLast(new MessageCodec(serializer));  // 序列化方案不写死
                // 心跳超时3秒
                ch.pipeline().addLast(new IdleStateHandler(0, 3, 0, TimeUnit.SECONDS));
                ch.pipeline().addLast(responseHandler);
            }
        });

        // 尝试与服务器建立连接
        Channel channel;
        try {
            channel = connect(serverAddress);
        } catch (ExecutionException | InterruptedException e) {
            log.error("连接到服务器 {} 失败", serverAddress);
            return null;
        }
        channelMap.put(target, channel);
        return channel;
    }

    /**
     * 进行连接建立。
     *
     * @param serverAddress 服务端地址
     */
    private static Channel connect(InetSocketAddress serverAddress) throws ExecutionException, InterruptedException {
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(serverAddress).addListener((ChannelFutureListener) future -> {
            // 通过回调函数的方式，在连接成功时将建立的channel放入completableFuture
            if (future.isSuccess()) {
                log.info("连接到服务器 {} 成功", serverAddress);
                completableFuture.complete(future.channel());
            } else {
                // 连接失败在上层函数调用处处理，这里不打印log
                throw new IllegalAccessException();
            }
        });
        // 阻塞等待completableFuture对端complete
        return completableFuture.get();
    }

    /**
     * 初始化客户端Bootstrap。
     *
     * @return bootstrap
     */
    private static Bootstrap initBootstrap() {
        lazyInitTools();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventExecutors)
                .channel(NioSocketChannel.class)
                //连接的超时时间，超过这个时间还是建立不上的话则代表连接失败
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, RpcClientConfig.getNettyConnectTimeout());
        return bootstrap;
    }

    /**
     * 懒加载Bootstrap需要用到的共享时线程安全的类，不包括编解码器。
     */
    private static void lazyInitTools() {
        if (eventExecutors == null)
            eventExecutors = new NioEventLoopGroup();
        if (loggingHandler == null)
            loggingHandler = new LoggingHandler(RpcClientConfig.getNettyLogLevel());
        if (responseHandler == null)
            responseHandler = new ResponseHandler();
    }

}
