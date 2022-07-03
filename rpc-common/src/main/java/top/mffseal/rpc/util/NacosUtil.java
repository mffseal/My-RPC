package top.mffseal.rpc.util;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.enumeration.RpcError;
import top.mffseal.rpc.exception.RpcException;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * Nacos客户端侧工具类。
 *
 * @author mffseal
 */
public class NacosUtil {
    private static final Logger log = LoggerFactory.getLogger(NacosUtil.class);

    /**
     * 获取连接到nacos服务器。
     *
     * @param host Nacos服务器地址
     * @param port Nacos服务器端口
     * @return NamingService
     */
    public static NamingService getNacosNamingService(String host, int port) {
        final String SERVER_ADDRESS = host + ":" + port;
        try {
            //  创建 NamingService 连接 Nacos服务器
            return NamingFactory.createNamingService(SERVER_ADDRESS);
        } catch (NacosException e) {
            log.error("连接到Nacos时发生错误: ", e);
            throw new RpcException(RpcError.FAILED_TO_CONNECT_TO_DYNAMIC_NAMING_CONFIGURATION_SERVER);
        }
    }

    /**
     * 向Nacos注册服务。
     *
     * @param namingService     Nacos服务器
     * @param serviceName       要注册的服务名称
     * @param inetSocketAddress 服务提供者地址
     * @throws NacosException 注册异常
     */
    public static void registerService(NamingService namingService, String serviceName, InetSocketAddress inetSocketAddress) throws NacosException {
        namingService.registerInstance(serviceName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
    }

    /**
     * 向Nacos查询服务。
     *
     * @param namingService Nacos服务器
     * @param serviceName   要查询的服务名称
     * @return 服务提供商地址
     * @throws NacosException 查询异常
     */
    public static List<Instance> getAllInstances(NamingService namingService, String serviceName) throws NacosException {
        return namingService.getAllInstances(serviceName);
    }


}
