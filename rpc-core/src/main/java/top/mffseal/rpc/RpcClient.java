package top.mffseal.rpc;

import top.mffseal.rpc.entity.RpcRequestMessage;

/**
 * @author mffseal
 */
public interface RpcClient {
    public Object sendRequest(RpcRequestMessage rpcRequestMessage);
}
