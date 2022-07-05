package top.mffseal.rpc.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.enumeration.RpcError;
import top.mffseal.rpc.exception.RpcException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务提供者默认实现。
 *
 * @author mffseal
 */
public class ServiceProviderImpl implements ServiceProvider {

    private static final Logger logger = LoggerFactory.getLogger(ServiceProviderImpl.class);

    /**
     * 记录提供服务的对象所实现的接口（可能有多个）与提供服务对象的对应关系：
     * <br/>如：A 实现了接口 X 和 Y，则记录 &lt;X,A&gt;,&lt;Y,A&gt;。
     * static保证注册信息全局唯一。
     */
    private static final Map<String, Object> serviceMap = new ConcurrentHashMap<>();

    /**
     * 记录已经注册的服务名称。
     * static保证注册信息全局唯一。
     */
    private static final Set<String> registeredService = ConcurrentHashMap.newKeySet();

    /**
     * 将传入的对象注册到本地服务注册表。
     * 所注册的方法必须至少实现一个接口；
     * 方法实现多个接口时会记录多个接口--服务对象的记录；
     * 方法不会被重复注册。
     *
     * @param service 被注册的服务
     * @param <T>     服务实体类
     */
    @Override
    public <T> void addServiceProvider(T service, String serviceName) {
        // 记录服务
        if (registeredService.contains(serviceName)) return;
        registeredService.add(serviceName);

        // 获取服务实现的接口列表
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if (interfaces.length == 0) {
            throw new RpcException(RpcError.SERVICE_NOT_IMPLEMENT_ANY_INTERFACE, serviceName);
        }

        // 将服务实现的所有接口都关联到该服务
        for (Class<?> i : interfaces) {
            serviceMap.put(i.getCanonicalName(), service);
        }
        logger.info("向接口: {} 注册了服务: {}", interfaces, serviceName);

    }

    /**
     * 通过接口名查找本地实现对象。
     *
     * @param serviceName 服务名
     * @return 实现对象
     */
    @Override
    public Object getServiceProvider(String serviceName) {
        Object service = serviceMap.get(serviceName);
        if (service == null) {
            throw new RpcException(RpcError.SERVICE_NOT_FOUND, serviceName);
        }
        return service;
    }
}
