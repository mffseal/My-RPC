package top.mffseal.test;

import top.mffseal.rpc.annotation.ServiceScan;
import top.mffseal.rpc.transport.RpcServer;
import top.mffseal.rpc.transport.netty.server.NettyServer;

/**
 * 测试用Netty服务端
 *
 * @author mffseal
 */
@ServiceScan
public class NettyTestServer {
    public static void main(String[] args) {
        RpcServer rpcServer = new NettyServer();
        rpcServer.start();
    }
}
