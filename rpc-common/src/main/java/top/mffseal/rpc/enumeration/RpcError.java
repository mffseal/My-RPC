package top.mffseal.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * RPC调用过程中的错误，服务端异常处理所用。
 *
 * @author mffseal
 */
@AllArgsConstructor
@Getter
public enum RpcError {
    UNKNOWN_ERROR("出现未知错误"),
    SERVICE_SCAN_PACKAGE_NOT_FOUND("启动类上ServiceScan注解缺失"),
    CLIENT_CONNECT_SERVER_FAILURE("客户端连接服务端失败"),
    SERVICE_INVOCATION_FAILURE("服务调用失败"),
    SERVICE_CAN_NOT_BE_NULL("注册的服务不能为空"),
    SERVICE_NOT_FOUND("指定的服务未找到"),
    SERVICE_NOT_IMPLEMENT_ANY_INTERFACE("注册的服务未实现任何接口"),
    UNKNOWN_PROTOCOL("未识别的协议包"),
    UNKNOWN_SERIALIZER("未识别的(反)序列化器"),
    UNKNOWN_PACKAGE_TYPE("未识别的数据包类型"),
    UNKNOWN_PACKAGE_LENGTH("未识别的数据包长度"),
    INCOMPLETE_DATA("不完整的数据包内容"),
    RESPONSE_NOT_MATCH("请求和响应的序列号不匹配"),
    FAILED_TO_CONNECT_TO_DYNAMIC_NAMING_CONFIGURATION_SERVER("连接服务配置平台失败"),
    REGISTER_SERVICE_FAILURE("注册服务失败");


    private final String message;

}
