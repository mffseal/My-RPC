package top.mffseal.rpc.transport.netty.server;

import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.RpcRequestMessage;
import top.mffseal.rpc.factory.ThreadPoolFactory;
import top.mffseal.rpc.handler.RequestHandler;

import java.util.concurrent.ExecutorService;

/**
 * Netty下处理服务端收到的RpcRequestMessage的handler。
 *
 * @author mffseal
 */
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    private static final Logger log = LoggerFactory.getLogger(NettyServerHandler.class);
    /**
     * 业务线程池，用于将服务调用与netty线程解耦，防止netty网络服务被业务阻塞。
     */
    private static final ExecutorService threadPool;
    private static final String THREAD_NAME_PREFIX = "netty-server-handler";
    /**
     * Sharable的handler加上是static final修饰，所以requestHandler不用单例工厂就能线程安全的进行单例模式实例化。
     */
    private static final RequestHandler requestHandler;

    // 初始化
    static {
        requestHandler = new RequestHandler();
        threadPool = ThreadPoolFactory.createDefaultThreadPool(THREAD_NAME_PREFIX);
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) {
        // 将业务从netty线程解耦
        threadPool.execute(() -> {
            try {
                if (msg.getHeartBeat()) {
                    log.info("收到 {} 客户端的心跳包", ctx.channel().remoteAddress());
                    return;
                }
                Object result = requestHandler.handle(msg);// 调用服务实现，返回的结果已经包装成RpcResponseMessage
                if (ctx.channel().isActive() && ctx.channel().isWritable()) {
                    ctx.writeAndFlush(result);
                } else {
                    log.error("通道 {} 不可写", ctx.channel());
                }
            } finally {
                ReferenceCountUtil.release(msg);  // 引用计数-1 TODO: 2022/6/28 确实需要手动释放资源码
            }
        });
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent event = (IdleStateEvent) evt;
        if (event.state() == IdleState.READER_IDLE) {
            log.debug("5秒内未再收到 {} 客户端发送的消息，断开连接", ctx.channel().remoteAddress());
            ctx.channel().close();
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
