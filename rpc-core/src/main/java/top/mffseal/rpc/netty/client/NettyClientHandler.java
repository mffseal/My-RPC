package top.mffseal.rpc.netty.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.RpcResponseMessage;

/**
 * Netty下处理客户端收到的RpcResponseMessage的handler。
 *
 * @author mffseal
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponseMessage<?>> {
    private static final Logger log = LoggerFactory.getLogger(NettyClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponseMessage msg) {
        try {
            log.info("客户端收到消息: {}", msg);
            AttributeKey<RpcResponseMessage<?>> key = AttributeKey.valueOf("rpcResponse" + msg.getSequenceId());
            ctx.channel().attr(key).set(msg);
            ctx.channel().close();
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("过程调用时发生错误:", cause);
        ctx.close();
    }
}
