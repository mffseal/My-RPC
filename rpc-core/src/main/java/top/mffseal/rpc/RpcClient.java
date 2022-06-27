package top.mffseal.rpc;

import top.mffseal.rpc.entity.RpcRequest;

/**
 * @author mffseal
 */
public interface RpcClient {
    public Object sendRequest(RpcRequest rpcRequest);
}
