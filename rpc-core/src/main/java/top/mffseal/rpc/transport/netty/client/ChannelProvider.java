package top.mffseal.rpc.transport.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.codec.MessageCodec;
import top.mffseal.rpc.codec.ProtocolFrameDecoder;
import top.mffseal.rpc.config.Config;
import top.mffseal.rpc.enumeration.RpcError;
import top.mffseal.rpc.exception.RpcException;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 客户端连接管理器，实现自动重试功能。
 *
 * @author mffseal
 */
public class ChannelProvider {
    private static final Logger log = LoggerFactory.getLogger(ChannelProvider.class);
    private static EventLoopGroup eventExecutors;
    private static Bootstrap bootstrap;
    private static LoggingHandler loggingHandler;
    private static MessageCodec messageCodec;
    private static NettyClientHandler nettyClientHandler;
    private static Channel channel;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_COUNT = Config.getNettyClientRetryCount();

    /**
     * 向服务端建立连接，带重传功能。
     *
     * @param serverAddress 服务端地址
     * @return 连接channel
     */
    public static Channel get(InetSocketAddress serverAddress) {
        if (bootstrap == null)
            bootstrap = initBootstrap();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            connect(serverAddress, countDownLatch);
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("获取channel时发生错误:", e);
        }
        return channel;
    }

    /**
     * 进行连接建立的中间方法。
     *
     * @param serverAddress  服务端地址
     * @param countDownLatch 同步状态
     */
    private static void connect(InetSocketAddress serverAddress, CountDownLatch countDownLatch) {
        connect(serverAddress, countDownLatch, MAX_RETRY_COUNT);
    }

    /**
     * 进行带重试的连接建立。
     *
     * @param serverAddress  服务端地址
     * @param countDownLatch 同步状态
     * @param retry          当前重试上限
     */
    private static void connect(InetSocketAddress serverAddress, CountDownLatch countDownLatch, int retry) {
        bootstrap.connect(serverAddress).addListener((ChannelFutureListener) future -> {
            // 成功连接
            if (future.isSuccess()) {
                log.info("客户端连接到服务器成功");
                channel = future.channel();
                countDownLatch.countDown();
                return;
            }

            // 重试指定次数仍无法成功连接
            if (retry == 0) {
                log.error("客户端连接到服务器失败，并且重试次数已用完，放弃连接。");
                countDownLatch.countDown();
                throw new RpcException(RpcError.CLIENT_CONNECT_SERVER_FAILURE);
            }

            // 重试次数
            int order = MAX_RETRY_COUNT - retry + 1;

            // 本轮重试启动延迟，每次增加1（单位秒）
            // TODO 优化重试算法
            int delay = 1 + order;
            log.error("客户端连接服务端失败: {} 秒后将进行第 {}/{} 次重试", delay, order, MAX_RETRY_COUNT);
            // 添加一个定时任务
            bootstrap.config().group().schedule(() -> connect(serverAddress, countDownLatch, retry - 1), delay, TimeUnit.SECONDS);
        });
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
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Config.getNettyClientConnectTimeout())
                //是否开启 TCP 底层心跳机制
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new ProtocolFrameDecoder());
                        ch.pipeline().addLast(loggingHandler);
                        ch.pipeline().addLast(messageCodec);
                        ch.pipeline().addLast(nettyClientHandler);
                    }
                });
        return bootstrap;
    }

    /**
     * 懒加载Bootstrap需要用到的类。
     */
    private static void lazyInitTools() {
        if (eventExecutors == null)
            eventExecutors = new NioEventLoopGroup();
        if (loggingHandler == null)
            loggingHandler = new LoggingHandler(Config.getNettyClientLogLevel());
        if (messageCodec == null)
            messageCodec = new MessageCodec();
        if (nettyClientHandler == null)
            nettyClientHandler = new NettyClientHandler();
    }

}
