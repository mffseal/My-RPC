package top.mffseal.rpc.provider;

/**
 * 用于记录服务端本地服务实例的类。服务端收到请求后查询本地服务表找到实现。
 *
 * @author mffseal
 */
public interface ServiceProvider {

    /**
     * 注册服务
     *
     * @param service     服务接口
     * @param serviceName 服务名
     * @param <T>         服务类型
     */
    <T> void addServiceProvider(T service, String serviceName);

    Object getServiceProvider(String serviceName);
}
