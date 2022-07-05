package top.mffseal.rpc.transport.netty.client;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.ChannelInputShutdownReadComplete;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.RpcRequestMessage;
import top.mffseal.rpc.entity.RpcResponseMessage;
import top.mffseal.rpc.factory.SingletonFactory;

/**
 * Netty下，用于处理客户端收到的{@link RpcResponseMessage}的handler，将响应填充到{@link ResponseLocker}中对应的位置。
 *
 * @author mffseal
 */
@ChannelHandler.Sharable
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponseMessage<?>> {
    private static final Logger log = LoggerFactory.getLogger(NettyClientHandler.class);
    /**
     * 用单例模式保证与{@link NettyClient}对象使用的是同一个快递柜。
     */
    private static final ResponseLocker responseLocker = SingletonFactory.getInstance(ResponseLocker.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) {
        try {
            log.info("客户端收到消息: {}", msg);
            responseLocker.complete(msg);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("过程调用时发生错误:", cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent event = (IdleStateEvent) evt;
        if (event.state() == IdleState.WRITER_IDLE) {
            log.info("发送心跳包: {}", ctx.channel().remoteAddress());
            RpcRequestMessage request = new RpcRequestMessage();
            request.setHeartBeat(true);
            ctx.writeAndFlush(request);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
