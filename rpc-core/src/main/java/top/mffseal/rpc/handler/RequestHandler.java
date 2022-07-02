package top.mffseal.rpc.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.RpcRequestMessage;
import top.mffseal.rpc.entity.RpcResponseMessage;
import top.mffseal.rpc.enumeration.ResponseCode;
import top.mffseal.rpc.provider.ServiceProvider;
import top.mffseal.rpc.provider.ServiceProviderImpl;
import top.mffseal.rpc.transport.socket.server.RequestHandlerThread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 处理过程调用（通过反射的方式）。
 *
 * @author mffseal
 */
public class RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandlerThread.class);
    private static final ServiceProvider serviceProvider;

    static {
        serviceProvider = new ServiceProviderImpl();
    }

    /**
     * 在service上调用rpcRequest中指定的方法，并返回包装成RpcResponseMessage的结果。
     *
     * @param rpcRequestMessage 客户端调用请求
     * @return 调用目标方法所返回的原始类型结果
     */
    public Object handle(RpcRequestMessage rpcRequestMessage) {
        String interfaceName = rpcRequestMessage.getInterfaceName();  // 获取接口名
        Object service = serviceProvider.getServiceProvider(interfaceName);  // 从本地放方法表找到服务实现
        Object result = null;
        try {
            result = invokeTargetMethod(rpcRequestMessage, service);
            logger.info("服务:{} ID:{} 成功调用方法:{}", rpcRequestMessage.getInterfaceName(), rpcRequestMessage.getSequenceId(), rpcRequestMessage.getMethodName());
        } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            logger.error("调用或发送时有错误发生: ", e);
        }
        return result;
    }

    /**
     * 调用的内部具体实现，返回结果会被包装成RpcResponseMessage。
     *
     * @param rpcRequestMessage 客户端调用请求
     * @param service           服务对象
     * @return 调用目标方法所返回的原始类型结果
     */
    private Object invokeTargetMethod(RpcRequestMessage rpcRequestMessage, Object service) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException {
        Class<?> clazz = Class.forName(rpcRequestMessage.getInterfaceName());
        // 如果请求中指定的接口和绑定的服务不符
        if (!clazz.isAssignableFrom(service.getClass())) {
            return RpcResponseMessage.fail(ResponseCode.CLASS_NOT_FOUND, rpcRequestMessage.getSequenceId());
        }

        Method method;
        try {
            // 利用RpcRequest中指定的方法名和参数类型，找到service中对应的方法
            method = service.getClass().getMethod(rpcRequestMessage.getMethodName(), rpcRequestMessage.getParamTypes());
        } catch (NoSuchMethodException e) {
            return RpcResponseMessage.fail(ResponseCode.METHOD_NOT_FOUND, rpcRequestMessage.getSequenceId());
        }

        Object returnObject = null;
        try {
            // 调用service中对应的方法
            returnObject = method.invoke(service, rpcRequestMessage.getParameters());

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            logger.error("目标方法调用失败: ", e);
        }
        if (returnObject == null) {
            return RpcResponseMessage.fail(ResponseCode.FAIL, rpcRequestMessage.getSequenceId());
        } else {
            return RpcResponseMessage.success(returnObject, rpcRequestMessage.getSequenceId());
        }
    }
}
