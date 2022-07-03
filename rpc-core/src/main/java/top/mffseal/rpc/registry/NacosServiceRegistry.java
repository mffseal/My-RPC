package top.mffseal.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.config.RpcServerConfig;
import top.mffseal.rpc.enumeration.RpcError;
import top.mffseal.rpc.exception.RpcException;
import top.mffseal.rpc.util.NacosUtil;

import java.net.InetSocketAddress;

/**
 * 基于Nacos实现的服务管理平台。
 *
 * @author mffseal
 */
public class NacosServiceRegistry implements ServiceRegistry {
    private static final Logger log = LoggerFactory.getLogger(NacosServiceRegistry.class);

    @Override
    public void register(String serviceName, InetSocketAddress inetSocketAddress) {
        try {
            NacosUtil.connectToNacosNamingService(RpcServerConfig.getNamingServerHost(), RpcServerConfig.getNamingServerPort());
            NacosUtil.registerService(serviceName, inetSocketAddress);
        } catch (NacosException e) {
            log.error("注册服务时发生错误: ", e);
            throw new RpcException(RpcError.REGISTER_SERVICE_FAILURE);
        }
    }
}
