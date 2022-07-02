package top.mffseal.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.config.Config;
import top.mffseal.rpc.enumeration.RpcError;
import top.mffseal.rpc.exception.RpcException;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 基于Nacos实现的服务管理平台。
 *
 * @author mffseal
 */
public class NacosServiceRegistry implements ServiceRegistry {
    private static final Logger log = LoggerFactory.getLogger(NacosServiceRegistry.class);
    private static final String SERVER_ADDRESS;
    private static final NamingService namingServer;

    static {
        SERVER_ADDRESS = Config.getDynamicNamingConfigurationServerHost() + ":" + Config.getDynamicNamingConfigurationServerPort();
        try {
            //  创建 NamingService 连接 Nacos服务器
            namingServer = NamingFactory.createNamingService(SERVER_ADDRESS);
        } catch (NacosException e) {
            log.error("连接到Nacos时发生错误: ", e);
            throw new RpcException(RpcError.FAILED_TO_CONNECT_TO_DYNAMIC_NAMING_CONFIGURATION_SERVER);
        }
    }

    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try {
            namingServer.registerInstance(serviceName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
        } catch (NacosException e) {
            log.error("注册服务时发生错误: ", e);
            throw new RpcException(RpcError.REGISTER_SERVICE_FAILURE);
        }
    }

    @Override
    public InetSocketAddress lookupService(String serviceName) {
        try {
            // 获取到某个服务的所有提供者列表
            List<Instance> instances = namingServer.getAllInstances(serviceName);
            // 目前均采用找到的第一个服务提供者
            // TODO 负载均衡
            Instance instance = instances.get(0);
            return new InetSocketAddress(instance.getIp(), instance.getPort());
        } catch (NacosException e) {
            log.error("获取服务提供者时发生错误: ", e);
        }
        return null;
    }
}
