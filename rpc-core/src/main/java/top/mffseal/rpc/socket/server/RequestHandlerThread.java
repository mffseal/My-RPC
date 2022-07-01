package top.mffseal.rpc.socket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.RequestHandler;
import top.mffseal.rpc.entity.RpcRequestMessage;
import top.mffseal.rpc.registry.ServiceRegistry;
import top.mffseal.rpc.socket.util.ObjectReader;
import top.mffseal.rpc.socket.util.ObjectWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 处理RpcRequest的工作线程，流程：
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
    private final RequestHandler requestHandler;
    private final ServiceRegistry serviceRegistry;

    public RequestHandlerThread(Socket socket, RequestHandler requestHandler, ServiceRegistry serviceRegistry) {
        this.socket = socket;
        this.serviceRegistry = serviceRegistry;
        this.requestHandler = requestHandler;
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
            Object service = serviceRegistry.getService(interfaceName);
            Object rpcResponse = requestHandler.handle(rpcRequestMessage, service);
            ObjectWriter.writeObject(outputStream, rpcResponse);
        } catch (IOException e) {
            logger.error("调用或发送时有错误发生: ", e);
        }
    }

}
