package top.mffseal.rpc.registry;

import java.net.InetSocketAddress;

/**
 * 服务注册表接口，用于保存本地服务的信息，
 * 同时提供根据服务名返回服务实体的方法。
 *
 * @author mffseal
 */
public interface ServiceRegistry {
    /**
     * 将服务注册到远程服务管理平台。
     *
     * @param serviceName       要注册的服务名
     * @param inetSocketAddress 提供该服务的服务端地址
     */
    void register(String serviceName, InetSocketAddress inetSocketAddress);

    /**
     * 根据服务名向服务提供者。
     *
     * @param serviceName 服务名
     * @return 提供目标服务的服务端地址
     */
    InetSocketAddress lookupService(String serviceName);
}
