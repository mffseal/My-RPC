package top.mffseal.rpc.transport.socket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.RpcRequestMessage;
import top.mffseal.rpc.handler.ServerInvokeHandler;
import top.mffseal.rpc.transport.socket.util.ObjectReader;
import top.mffseal.rpc.transport.socket.util.ObjectWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 处理服务调用请求的工作线程，不负责直接调用服务实现，将结果包装成rpcResponse返回，流程：
 * 解析请求；
 * 调用rpc处理器；
 * 包装response；
 * 向客户端发送响应；
 *
 * @author mffseal
 */
public class RequestHandlerThread implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandlerThread.class);
    private final Socket socket;
    private final ServerInvokeHandler serverInvokeHandler;

    public RequestHandlerThread(Socket socket, ServerInvokeHandler serverInvokeHandler) {
        this.socket = socket;
        this.serverInvokeHandler = serverInvokeHandler;
    }

    /**
     * 解析从客户端收到的调用请求，从注册表查找对应的服务对象，调用Handler进行具体的服务调用。
     */
    @Override
    public void run() {
        try (InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {
            RpcRequestMessage rpcRequestMessage = (RpcRequestMessage) ObjectReader.readObject(inputStream);
            String interfaceName = rpcRequestMessage.getInterfaceName();
            logger.info("需要查找的接口名: {}", interfaceName);
            // 间接调用服务实现
            Object rpcResponse = serverInvokeHandler.handle(rpcRequestMessage);
            ObjectWriter.writeObject(outputStream, rpcResponse);
        } catch (IOException e) {
            logger.error("调用或发送时有错误发生: ", e);
        }
    }

}
