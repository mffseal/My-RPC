package top.mffseal.rpc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.RpcRequestMessage;
import top.mffseal.rpc.entity.RpcResponseMessage;
import top.mffseal.rpc.enumeration.ResponseCode;
import top.mffseal.rpc.enumeration.RpcError;
import top.mffseal.rpc.exception.RpcException;

/**
 * 请求和响应检查器。
 *
 * @author mffseal
 */
public class RpcMessageChecker {
    private static final String INTERFACE_NAME = "interfaceName";
    private static final Logger log = LoggerFactory.getLogger(RpcRequestMessage.class);

    /**
     * 检查一对请求和响应内容的合法性。
     *
     * @param request  本机发出的请求
     * @param response 本机收到的响应
     */
    public static void check(RpcRequestMessage request, RpcResponseMessage<?> response) {
        // 无响应
        if (response == null) {
            log.error("服务调用失败, {}: {}", INTERFACE_NAME, request.getInterfaceName());
            throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ": " + request.getInterfaceName());
        }

        // 请求和响应不匹配
        if (!request.getSequenceId().equals(response.getSequenceId())) {
            log.error("响应和请求不匹配, 请求ID: {}, 响应ID: {}", request.getSequenceId(), response.getSequenceId());
            throw new RpcException(RpcError.RESPONSE_NOT_MATCH, INTERFACE_NAME + ": " + request.getInterfaceName());
        }

        // 响应包含错误
        if (response.getStatusCode() == null || !response.getStatusCode().equals(ResponseCode.SUCCESS.getCode())) {
            log.error("服务调用失败， {}: {}, response: {}", INTERFACE_NAME, request.getInterfaceName(), response);
            throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, INTERFACE_NAME + ": " + request.getInterfaceName());
        }
    }
}
