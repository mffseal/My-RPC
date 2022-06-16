package top.mffseal.rpc.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.RpcRequest;
import top.mffseal.rpc.entity.RpcResponse;
import top.mffseal.rpc.enumeration.ResponseCode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 处理过程调用（通过反射的方式）。
 * @author mffseal
 */
public class RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandlerThread.class);

    /**
     * 在service上调用rpcRequest中指定的方法。
     * @param rpcRequest 客户端调用请求
     * @param service 服务对象
     * @return 调用目标方法所返回的原始类型结果
     */
    public Object handle(RpcRequest rpcRequest, Object service) {
        Object result = null;
        try {
            result = invokeTargetMethod(rpcRequest, service);
            logger.info("服务:{} 成功调用方法:{}", rpcRequest.getInterfaceName(), rpcRequest.getMethodName());
        } catch (IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            logger.error("调用或发送时有错误发生: ", e);
        }
        return result;
    }

    /**
     * 调用的内部具体实现。
     * @param rpcRequest 客户端调用请求
     * @param service 服务对象
     * @return 调用目标方法所返回的原始类型结果
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException {
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
