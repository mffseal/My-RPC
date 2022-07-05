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
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.codec.MessageCodec;
import top.mffseal.rpc.codec.ProtocolFrameDecoder;
import top.mffseal.rpc.config.RpcServerConfig;
import top.mffseal.rpc.hook.ShutdownHook;
import top.mffseal.rpc.provider.ServiceProviderImpl;
import top.mffseal.rpc.registry.NacosServiceRegistry;
import top.mffseal.rpc.transport.AbstractRpcServer;

import java.util.concurrent.TimeUnit;

/**
 * @author mffseal
 */
public class NettyServer extends AbstractRpcServer {
    private static final Logger log = LoggerFactory.getLogger(NettyServer.class);

    public NettyServer() {
        serviceRegistry = new NacosServiceRegistry();
        serviceProvider = new ServiceProviderImpl();
        scanServices();
    }

    @Override
    public void start() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        MessageCodec COMMON_CODEC = new MessageCodec(RpcServerConfig.getSerializerLibrary());
        NettyServerHandler REQUEST_HANDLER = new NettyServerHandler();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(RpcServerConfig.getNettyLogLevel());
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 256)  // 全连接队列大小
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ProtocolFrameDecoder());
                            ch.pipeline().addLast(LOGGING_HANDLER);
                            ch.pipeline().addLast(COMMON_CODEC);  // 编解码handler
                            ch.pipeline().addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));
                            ch.pipeline().addLast(REQUEST_HANDLER);  // 请求处理handler
                        }
                    });
            // 同步等待服务器绑定端口
            ChannelFuture future = serverBootstrap.bind(RpcServerConfig.getHost(), RpcServerConfig.getPort()).sync();

            // 关闭后注销服务
            ShutdownHook.getShutdownHock().addClearAllHock();

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
}
