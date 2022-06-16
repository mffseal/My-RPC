package top.mffseal.rpc.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 服务者执行完毕后的返回数据，包含执行相关状态和信息，以及执行结果。
 * 发送给客户端所用。
 * @author mffseal
 */
@AllArgsConstructor
@Getter
public enum ResponseCode {
    SUCCESS(200, "方法调用成功"),
    FAIL(500, "方法调用失败"),
    METHOD_NOT_FOUND(500, "指定方法未找到"),
    CLASS_NOT_FOUND(500, "指定类未找到");

    private final int code;
    private final String message;


}
