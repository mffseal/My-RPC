package top.mffseal.rpc.util;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.factory.SingletonFactory;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Nacos客户端侧工具类。
 *
 * @author mffseal
 */
public class NacosUtil {
    private static final Logger log = LoggerFactory.getLogger(NacosUtil.class);
    private static String NAMING_SERVER_ADDRESS;
    private static NamingService namingService;
    /**
     * 存储当前服务器注册的所有服务名。
     */
    private static final Set<String> serviceNames = new HashSet<>();
    private static InetSocketAddress localAddress;

    /**
     * 获取连接到nacos服务器。
     *
     * @param host Nacos服务器地址
     * @param port Nacos服务器端口
     */
    public static void connectToNacosNamingService(String host, int port) {
        NAMING_SERVER_ADDRESS = host + ":" + port;
        //  创建 NamingService 连接 Nacos服务器
        // 通过单例工厂创建保证唯一性
        namingService = SingletonFactory.getInstance(NamingService.class, NamingFactory.class, "createNamingService", new Class[]{NAMING_SERVER_ADDRESS.getClass()}, new Object[]{NAMING_SERVER_ADDRESS});
    }

    /**
     * 向Nacos注册服务。
     *
     * @param serviceName       要注册的服务名称
     * @param inetSocketAddress 服务提供者地址
     * @throws NacosException 注册异常
     */
    public static void registerService(String serviceName, InetSocketAddress inetSocketAddress) throws NacosException {
        namingService.registerInstance(serviceName, inetSocketAddress.getHostName(), inetSocketAddress.getPort());
        localAddress = inetSocketAddress;  // 记录本机地址
        serviceNames.add(serviceName);  // 记录注册的服务名
    }

    /**
     * 向Nacos查询服务。
     *
     * @param serviceName 要查询的服务名称
     * @return 服务提供商地址
     * @throws NacosException 查询异常
     */
    public static List<Instance> getAllInstances(String serviceName) throws NacosException {
        return namingService.getAllInstances(serviceName);
    }


    /**
     * 将本机提供的所有服务在服务管理平台上注销。
     */
    public static void clearRegistry() {
        // 如果注册过信息
        if (!serviceNames.isEmpty() && localAddress != null) {
            Iterator<String> iterator = serviceNames.iterator();
            String host = localAddress.getHostName();
            int port = localAddress.getPort();
            while (iterator.hasNext()) {
                String serviceName = iterator.next();
                try {
                    namingService.deregisterInstance(serviceName, host, port);
                    log.info("成功注销服务: {}", serviceName);
                } catch (NacosException e) {
                    log.error("注销服务 {} 失败: ", serviceName, e);
                }
            }
        }
    }


}
