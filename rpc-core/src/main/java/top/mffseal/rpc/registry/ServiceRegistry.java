package top.mffseal.rpc.registry;

/**
 * 服务注册表接口，用于保存本地服务的信息，
 * 同时提供根据服务名返回服务实体的方法。
 * @author mffseal
 */
public interface ServiceRegistry {
    /**
     * 将服务注册进注册表中。
     * @param service 被注册的服务
     * @param <T> 服务实体类
     */
    <T> void register(T service);

    /**
     * 根据服务名称获取服务实体。
     * @param serviceName 服务名
     * @return 服务实体
     */
    Object getService(String serviceName);
}
