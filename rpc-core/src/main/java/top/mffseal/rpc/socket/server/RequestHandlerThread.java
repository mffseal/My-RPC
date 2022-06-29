package top.mffseal.rpc.socket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.RequestHandler;
import top.mffseal.rpc.entity.RpcRequestMessage;
import top.mffseal.rpc.registry.ServiceRegistry;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
    private Socket socket;
    private RequestHandler requestHandler;
    private ServiceRegistry serviceRegistry;

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
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            RpcRequestMessage rpcRequestMessage = (RpcRequestMessage) objectInputStream.readObject();
            String interfaceName = rpcRequestMessage.getInterfaceName();
            logger.info("需要查找的接口名: {}", interfaceName);
            Object service = serviceRegistry.getService(interfaceName);
            Object rpcResponse = requestHandler.handle(rpcRequestMessage, service);
            objectOutputStream.writeObject(rpcResponse);
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("调用或发送时有错误发生: ", e);
        }
    }

}
