package top.mffseal.rpc.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 消费者向服务者发送的请求对象
 * 包含：接口名、方法名、方法参数类型、参数值，使用上述信息确定唯一方法并调用。
 *
 * @author mffseal
 */
@Getter
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequestMessage extends Message {

    /**
     * 被调用接口名称
     */
    private String interfaceName;
    /**
     * 被调用方法名称
     */
    private String methodName;
    /**
     * 调用参数列表
     */
    private Object[] parameters;
    /**
     * 调用参数类型列表
     */
    private Class<?>[] paramTypes;

    public RpcRequestMessage(String sequenceId, String interfaceName, String methodName, Object[] parameters, Class<?>[] paramTypes) {
        super(sequenceId);
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameters = parameters;
        this.paramTypes = paramTypes;
    }

    @Override
    public int getMessageType() {
        return RpcRequestMessage;
    }
}
