package top.mffseal.rpc.entity;

import lombok.*;
import top.mffseal.rpc.enumeration.ResponseCode;

/**
 * 服务者发回给消费者的执行结果
 *
 * @author mffseal
 */
@Getter
@Setter
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponseMessage<T> extends Message {
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


    public RpcResponseMessage(String sequenceId, Integer statusCode, String statusMessage, T data) {
        super(sequenceId);
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.data = data;
    }

    /**
     * 快速构建成功的响应数据
     *
     * @param data 要返回的执行结果
     * @param <T>  执行结果的数据类型
     * @return 响应内容
     */
    public static <T> RpcResponseMessage<T> success(T data, String requestId) {
        RpcResponseMessage<T> response = new RpcResponseMessage<>();
        response.setSequenceId(requestId);
        response.setStatusCode(ResponseCode.SUCCESS.getCode());
        response.setData(data);
        return response;
    }

    /**
     * 快速构建失败的响应数据
     *
     * @param code 失败的响应码和消息
     * @param <T>  执行结果的数据类型
     * @return 响应内容
     */
    public static <T> RpcResponseMessage<T> fail(ResponseCode code, String requestId) {
        RpcResponseMessage<T> response = new RpcResponseMessage<>();
        response.setSequenceId(requestId);
        response.setStatusCode(code.getCode());
        response.setStatusMessage(code.getMessage());
        return response;
    }

    @Override
    public int getMessageType() {
        return RpcResponseMessage;
    }
}
