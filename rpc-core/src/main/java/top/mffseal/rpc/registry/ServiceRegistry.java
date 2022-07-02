package top.mffseal.rpc.registry;

import java.net.InetSocketAddress;

/**
 * 服务注册接口，向服务管理平台注册本地服务。
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
}
