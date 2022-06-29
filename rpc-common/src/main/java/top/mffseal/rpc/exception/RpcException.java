package top.mffseal.rpc.exception;

import top.mffseal.rpc.enumeration.RpcError;

/**
 * RPC调用自定义异常，服务端异常处理所用。
 *
 * @author mffseal
 */
public class RpcException extends RuntimeException {
    public RpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcException(RpcError error, String detail) {
        super(error.getMessage() + ": " + detail);
    }

    public RpcException(RpcError error) {
        super(error.getMessage());
    }
}
