package top.mffseal.rpc.transport.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.codec.MessageCodec;
import top.mffseal.rpc.codec.ProtocolFrameDecoder;
import top.mffseal.rpc.config.Config;
import top.mffseal.rpc.provider.ServiceProvider;
import top.mffseal.rpc.provider.ServiceProviderImpl;
import top.mffseal.rpc.registry.NacosServiceRegistry;
import top.mffseal.rpc.registry.ServiceRegistry;
import top.mffseal.rpc.transport.RpcServer;

import java.net.InetSocketAddress;

/**
 * @author mffseal
 */
public class NettyServer implements RpcServer {
    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);
    private final ServiceProvider serviceProvider;
    private final ServiceRegistry serviceRegistry;

    public NettyServer() {
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
    }

    @Override
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        MessageCodec COMMON_CODEC = new MessageCodec();
        NettyServerHandler REQUEST_HANDLER = new NettyServerHandler();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(Config.getNettyServerLogLevel());
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 256)  // 全连接队列大小
                    .option(ChannelOption.SO_KEEPALIVE, true)  // tcp保活探测
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ProtocolFrameDecoder());
                            ch.pipeline().addLast(LOGGING_HANDLER);
                            ch.pipeline().addLast(COMMON_CODEC);  // 编解码handler
                            ch.pipeline().addLast(REQUEST_HANDLER);  // 请求处理handler
                        }
                    });
            // 同步等待服务器绑定端口
            ChannelFuture future = serverBootstrap.bind(Config.getServerHost(), Config.getServerPort()).sync();
            // 同步等待服务器结束
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("服务器启动失败: ", e);
        } finally {
            // 优雅地关闭服务线程
            // TODO: 2022/6/28 改用closeFuture异步执行
            log.info("服务器关闭中...");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            log.info("服务器已关闭");
        }


    }

    @Override
    public <T> void publishService(Object service, Class<T> serviceClass) {
        serviceProvider.addServiceProvider(service);
        serviceRegistry.register(serviceClass.getCanonicalName(), new InetSocketAddress(Config.getServerHost(), Config.getServerPort()));
    }
}
