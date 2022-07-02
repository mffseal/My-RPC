package top.mffseal.rpc.transport.netty.server;

import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.RpcRequestMessage;
import top.mffseal.rpc.handler.RequestHandler;
import top.mffseal.rpc.util.ThreadPoolFactory;

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
                Object result = requestHandler.handle(msg);// 调用服务实现，返回的结果已经包装成RpcResponseMessage
                ChannelFuture future = ctx.writeAndFlush(result);
                future.addListener(ChannelFutureListener.CLOSE);  // 包发送完毕后关闭连接 todo 是否可以长连接?
            } finally {
                ReferenceCountUtil.release(msg);  // 引用计数-1 TODO: 2022/6/28 确实需要手动释放资源码
            }
        });
    }
}
