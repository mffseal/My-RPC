package top.mffseal.rpc.provider;

/**
 * 用于记录服务端本地服务实例的类。服务端收到请求后查询本地服务表找到实现。
 *
 * @author mffseal
 */
public interface ServiceProvider {

    <T> void addServiceProvider(T service, Class<T> serverClass);

    Object getServiceProvider(String serviceName);
}
