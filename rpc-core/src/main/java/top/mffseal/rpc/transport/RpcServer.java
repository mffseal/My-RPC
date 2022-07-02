package top.mffseal.rpc.transport;

/**
 * RPC服务端接口，负责启动网络服务并向服务管理平台注册服务实现。
 *
 * @author mffseal
 */
public interface RpcServer {
    /**
     * 启动网络服务。
     */
    void start();

    /**
     * 向服务管理平台和本地服务记录表注册服务实现。
     *
     * @param service      服务实现对象
     * @param serviceClass 服务接口
     * @param <T>          服务类型
     */
    <T> void publishService(Object service, Class<T> serviceClass);
}
