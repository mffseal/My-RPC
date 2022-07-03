package top.mffseal.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.config.RpcClientConfig;
import top.mffseal.rpc.config.RpcServerConfig;
import top.mffseal.rpc.util.NacosUtil;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author mffseal
 */
public class NacosServiceDiscovery implements ServiceDiscovery {
    private static final Logger log = LoggerFactory.getLogger(NacosServiceDiscovery.class);

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            NacosUtil.connectToNacosNamingService(RpcClientConfig.getNamingServerHost(), RpcClientConfig.getNamingServerPort());
            // 获取到某个服务的所有提供者列表
            List<Instance> instances = NacosUtil.getAllInstances(serviceName);
            // 目前均采用找到的第一个服务提供者
            // TODO 负载均衡
            Instance instance = instances.get(0);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException | IndexOutOfBoundsException e) {
            log.error("获取服务提供者时发生错误: ", e);
        }
        return null;
    }
}
