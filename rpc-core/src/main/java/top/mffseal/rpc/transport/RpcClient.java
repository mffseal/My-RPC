package top.mffseal.rpc.transport;

import top.mffseal.rpc.entity.RpcRequestMessage;

/**
 * RPC客户端接口，负责向服务管理平台查询服务后再请求服务。
 *
 * @author mffseal
 */
public interface RpcClient {
    /**
     * 请求远程rpc调用。
     *
     * @param rpcRequestMessage rpc请求
     * @return 调用结果
     */
    Object sendRequest(RpcRequestMessage rpcRequestMessage);
}
