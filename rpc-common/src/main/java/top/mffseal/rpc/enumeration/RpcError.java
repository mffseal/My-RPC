package top.mffseal.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author mffseal
 */
@AllArgsConstructor
@Getter
public enum RpcError {
    SERVICE_INVOCATION_FAILURE("服务调用失败"),
    SERVICE_CAN_NOT_BE_NULL("注册的服务不能为空");

    private final String message;
}
