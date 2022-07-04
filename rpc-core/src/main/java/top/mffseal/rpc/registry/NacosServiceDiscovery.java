package top.mffseal.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.config.RpcClientConfig;
import top.mffseal.rpc.loadbalancer.LoadBalancer;
import top.mffseal.rpc.util.NacosUtil;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author mffseal
 */
public class NacosServiceDiscovery implements ServiceDiscovery {
    private static final Logger log = LoggerFactory.getLogger(NacosServiceDiscovery.class);
    private final LoadBalancer loadBalancer;

    public NacosServiceDiscovery(LoadBalancer loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            NacosUtil.connectToNacosNamingService(RpcClientConfig.getNamingServerHost(), RpcClientConfig.getNamingServerPort());
            // 获取到某个服务的所有提供者列表
            List<Instance> instances = NacosUtil.getAllInstances(serviceName);

            // 应用负载均衡算法
            Instance instance = loadBalancer.select(instances);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException | IndexOutOfBoundsException e) {
            log.error("获取服务提供者时发生错误: ", e);
        }
        return null;
    }
}
