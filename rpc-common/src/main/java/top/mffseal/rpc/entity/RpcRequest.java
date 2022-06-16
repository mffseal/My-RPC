package top.mffseal.rpc.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 消费者向服务者发送的请求对象
 * 包含：接口名、方法名、方法参数类型、参数值，使用上述信息确定唯一方法并调用。
 * @author mffseal
 */
@Data
@Builder
public class RpcRequest implements Serializable {

    /**
     * 被调用接口名称
     */
    public String interfaceName;
    /**
     * 被调用方法名称
     */
    public String methodName;
    /**
     * 调用参数列表
     */
    public Object[] parameters;
    /**
     * 调用参数类型列表
     */
    public Class<?>[] paramTypes;

}
