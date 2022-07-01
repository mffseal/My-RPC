package top.mffseal.rpc.netty.server;

import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.RequestHandler;
import top.mffseal.rpc.entity.RpcRequestMessage;
import top.mffseal.rpc.registry.DefaultServiceRegistry;
import top.mffseal.rpc.registry.ServiceRegistry;

/**
 * Netty下处理服务端收到的RpcRequestMessage的handler。
 *
 * @author mffseal
 */
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    private static final Logger log = LoggerFactory.getLogger(NettyServerHandler.class);

    private static final RequestHandler requestHandler = new RequestHandler();
    private static final ServiceRegistry serviceRegistry = new DefaultServiceRegistry();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequestMessage msg) {
        try {
            String interfaceName = msg.getInterfaceName();  // 获取接口名
            Object service = serviceRegistry.getService(interfaceName);  // 通过接口名查到对应的服务实现
            Object result = requestHandler.handle(msg, service);// 调用服务实现，返回的结果已经包装成RpcResponseMessage
            ChannelFuture future = ctx.writeAndFlush(result);
            future.addListener(ChannelFutureListener.CLOSE);  // 包发送完毕后关闭连接 todo 是否可以长连接?
        } finally {
            ReferenceCountUtil.release(msg);  // 引用计数-1 TODO: 2022/6/28 确实需要手动释放资源码
        }
    }
}
