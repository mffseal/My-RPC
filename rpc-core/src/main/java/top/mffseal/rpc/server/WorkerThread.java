package top.mffseal.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.RpcRequest;
import top.mffseal.rpc.entity.RpcResponse;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * 工作线程对象
 * @author mffseal
 */
public class WorkerThread implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(WorkerThread.class);
    private Socket socket;
    private Object service;

    public WorkerThread(Socket socket, Object service) {
        this.socket = socket;
        this.service = service;
    }

    /**
     * 从socket获取RpcRequest，解析函数名和参数类型，绑定到service上对应方法，
     * 使用RpcRequest携带的参数执行方法，
     * 向socket写入RpcResponse。
     */
    @Override
    public void run() {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            // 利用RpcRequest中指定的方法名和参数类型，找到service中对应的方法
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
            // 调用service中对应的方法
            Object returnObject = method.invoke(service, rpcRequest.getParameters());
            objectOutputStream.writeObject(RpcResponse.success(returnObject));
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException e) {
            logger.error("调用或发送时有错误发生: ", e);
        }
    }
}
