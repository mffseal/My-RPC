package top.mffseal.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.RpcRequest;
import top.mffseal.rpc.entity.RpcResponse;
import top.mffseal.rpc.enumeration.ResponseCode;
import top.mffseal.rpc.enumeration.RpcError;
import top.mffseal.rpc.exception.RpcException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;

/**
 * 工作线程对象，将收到的RpcRequest包装成本地线程执行。
 * @author mffseal
 */
public class RequestHandler implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private Socket socket;
    private Object service;

    public RequestHandler (Socket socket, Object service) {
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
            Object rpcResponse = invokeMethod(rpcRequest);
            objectOutputStream.writeObject(rpcResponse);
            objectOutputStream.flush();
        } catch (IOException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
            logger.error("调用或发送时有错误发生: ", e);
        }
    }

    /**
     * 在绑定的service上指定rpcRequest中指定的方法。
     * @param rpcRequest 客户端调用请求
     * @return 请求结果
     */
    private Object invokeMethod(RpcRequest rpcRequest) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        Class<?> clazz = Class.forName(rpcRequest.getInterfaceName());
        // 如果请求中指定的接口和绑定的服务不符
        if (!clazz.isAssignableFrom(service.getClass())) {
            return RpcResponse.fail(ResponseCode.CLASS_NOT_FOUND);
        }
        Method method;
        try {
            // 利用RpcRequest中指定的方法名和参数类型，找到service中对应的方法
            method = service.getClass().getMethod(rpcRequest.getMethodName(), rpcRequest.getParamTypes());
        } catch (NoSuchMethodException e) {
            return RpcResponse.fail(ResponseCode.METHOD_NOT_FOUND);
        }
        // 调用service中对应的方法
        Object returnObject = method.invoke(service, rpcRequest.getParameters());
        if (returnObject==null) {
            return RpcResponse.fail(ResponseCode.FAIL);
        }
        return RpcResponse.success(returnObject);
    }

}
