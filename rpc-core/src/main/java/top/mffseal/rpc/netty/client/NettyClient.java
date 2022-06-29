package top.mffseal.rpc.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.RpcClient;
import top.mffseal.rpc.codec.CommonCodec;
import top.mffseal.rpc.entity.RpcRequestMessage;
import top.mffseal.rpc.entity.RpcResponseMessage;

/**
 * @author mffseal
 */
public class NettyClient implements RpcClient {
    private static final Logger log = LoggerFactory.getLogger(NettyClient.class);
    private String host;
    private int port;
    private static Bootstrap bootstrap;

    public NettyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    // 配置客户端信息
    static {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        CommonCodec COMMON_CODEC = new CommonCodec();
        NettyClientHandler RESPONSE_HANDLER = new NettyClientHandler();

        bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(LOGGING_HANDLER);
                        ch.pipeline().addLast(COMMON_CODEC);
                        ch.pipeline().addLast(RESPONSE_HANDLER);
                    }
                });
    }

    @Override
    public Object sendRequest(RpcRequestMessage rpcRequestMessage) {
        try {
            ChannelFuture future = bootstrap.connect(host, port).sync();
            Channel channel = future.channel();
            log.info("客户端连接到服务器 {}:{}", host, port);
            if (channel != null) {
                // 向服务端发送请求，并通过回调获取服务端响应结果
                channel.writeAndFlush(rpcRequestMessage).addListener(future1 -> {
                    if (future1.isSuccess()) {
                        log.info("客户端发送消息成功: {}", rpcRequestMessage.toString());
                    } else {
                        log.error("客户端发送消息失败: ", future1.cause());
                    }
                });
                channel.closeFuture().sync();

                // 获得返回结果 RpcResponse 后，将这个对象以 key 为 ”rpcResponse“ 放入 ChannelHandlerContext 中
                AttributeKey<RpcResponseMessage> key = AttributeKey.valueOf("rpcResponse");
                RpcResponseMessage rpcResponseMessage = channel.attr(key).get();
                Object data = rpcResponseMessage.getData();
                return data;
            }
        } catch (InterruptedException e) {
            log.error("服务器连接失败: ", e);
        }
        return null;
    }
}
