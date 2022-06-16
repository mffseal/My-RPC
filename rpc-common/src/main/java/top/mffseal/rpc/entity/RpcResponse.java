package top.mffseal.rpc.entity;

import lombok.Data;
import top.mffseal.rpc.enumeration.ResponseCode;

import java.io.Serializable;

/**
 * 服务者发回给消费者的执行结果
 * @author mffseal
 */
@Data
public class RpcResponse<T> implements Serializable {
    /**
     * 响应状态码
     */
    private Integer statusCode;

    /**
     * 响应信息
     */
    private String statusMessage;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 快速构建成功的响应数据
     * @param data 要返回的执行结果
     * @return 响应内容
     * @param <T> 执行结果的数据类型
     */
    public static <T> RpcResponse<T> success(T data) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(ResponseCode.SUCCESS.getCode());
        response.setData(data);
        return response;
    }

    /**
     * 快速构建失败的响应数据
     * @param code 失败的响应码和消息
     * @return 响应内容
     * @param <T> 执行结果的数据类型
     */
    public static <T> RpcResponse<T> fail(ResponseCode code) {
        RpcResponse<T> response = new RpcResponse<>();
        response.setStatusCode(code.FAIL.getCode());
        response.setStatusMessage(code.FAIL.getMessage());
        return response;
    }
}
