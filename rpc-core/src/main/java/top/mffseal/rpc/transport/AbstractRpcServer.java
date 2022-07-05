package top.mffseal.rpc.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.annotation.Service;
import top.mffseal.rpc.annotation.ServiceScan;
import top.mffseal.rpc.config.RpcServerConfig;
import top.mffseal.rpc.enumeration.RpcError;
import top.mffseal.rpc.exception.RpcException;
import top.mffseal.rpc.provider.ServiceProvider;
import top.mffseal.rpc.registry.ServiceRegistry;
import top.mffseal.rpc.transport.netty.server.NettyServer;
import top.mffseal.rpc.util.ReflectUtil;

import java.net.InetSocketAddress;
import java.util.Set;

/**
 * RpcServer的抽象类，提供了扫描服务的公共方法。
 *
 * @author mffseal
 */
public abstract class AbstractRpcServer implements RpcServer {
    protected static final Logger log = LoggerFactory.getLogger(NettyServer.class);
    protected ServiceProvider serviceProvider;
    protected ServiceRegistry serviceRegistry;

    /**
     * 扫描服务实现类 --> 创建该类的实例 --> 发布服务。
     */
    public void scanServices() {
        // 找到最外层的调用处，作为扫描起点
        String mainClassName = ReflectUtil.getStackTrace();
        Class<?> startClass;
        try {
            startClass = Class.forName(mainClassName);
            // 起点类没有ServiceScan注解
            if (!startClass.isAnnotationPresent(ServiceScan.class)) {
                log.error("启动类缺少 @ServiceScan 注解");
                throw new RpcException(RpcError.SERVICE_SCAN_PACKAGE_NOT_FOUND);
            }
        } catch (ClassNotFoundException e) {
            log.error("注解扫描出现错误");
            throw new RpcException(RpcError.UNKNOWN_ERROR);
        }
        // 获取ServiceScan注解值
        String basePackage = startClass.getAnnotation(ServiceScan.class).value();
        if ("".equals(basePackage)) {
            // 获取该类所在包的名称
            basePackage = mainClassName.substring(0, mainClassName.lastIndexOf("."));
        }
        // 获取该包下的所有Class对象
        Set<Class<?>> classSet = ReflectUtil.getClasses(basePackage);
        for (Class<?> clazz : classSet) {
            // 如果该包下某个类存在Service注解
            if (clazz.isAnnotationPresent(Service.class)) {
                Object obj;
                // 尝试创建该类的一个实例
                try {
                    obj = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    log.error("创建 {} 时有错误发生", clazz);
                    continue;
                }

                // 尝试发布服务
                String serviceName = clazz.getAnnotation(Service.class).name();
                // 判断是否指定了服务名：
                // 如果没有指定，则通过反射获得该类实现的所有接口，将所有接口都与当前实例绑定并发布
                if ("".equals(serviceName)) {
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> oneInterface : interfaces) {
                        // 向服务管理平台注册服务
                        publishService(obj, oneInterface.getCanonicalName());
                    }
                } else {
                    // 若指定了服务名则用指定的名称发布服务
                    publishService(obj, serviceName);
                }
            }
        }
    }

    @Override
    public <T> void publishService(T service, String serviceName) {
        serviceProvider.addServiceProvider(service, serviceName);
        serviceRegistry.register(serviceName, new InetSocketAddress(RpcServerConfig.getHost(), RpcServerConfig.getPort()));
    }
}
