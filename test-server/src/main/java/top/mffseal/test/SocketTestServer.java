package top.mffseal.test;

import top.mffseal.rpc.annotation.ServiceScan;
import top.mffseal.rpc.transport.RpcServer;
import top.mffseal.rpc.transport.socket.server.SocketServer;

/**
 * 测试用Socket服务端。
 *
 * @author mffseal
 */
@ServiceScan
public class SocketTestServer {
    public static void main(String[] args) {
        RpcServer rpcServer = new SocketServer();
        // 启动服务器
        rpcServer.start();
    }
}
