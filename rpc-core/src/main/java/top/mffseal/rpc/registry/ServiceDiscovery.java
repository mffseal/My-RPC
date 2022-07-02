package top.mffseal.rpc.registry;

import java.net.InetSocketAddress;

/**
 * 服务发现接口，向服务管理平台查询服务对应提供者。
 *
 * @author mffseal
 */
public interface ServiceDiscovery {
    /**
     * 根据服务名向服务提供者。
     *
     * @param serviceName 服务名
     * @return 提供目标服务的服务端地址
     */
    InetSocketAddress lookupService(String serviceName);
}
