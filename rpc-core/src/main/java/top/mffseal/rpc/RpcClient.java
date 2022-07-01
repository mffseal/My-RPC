package top.mffseal.rpc;

import top.mffseal.rpc.entity.RpcRequestMessage;

/**
 * @author mffseal
 */
public interface RpcClient {
    Object sendRequest(RpcRequestMessage rpcRequestMessage);
}
