package top.mffseal.rpc.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mffseal
 */
@Data
public abstract class Message implements Serializable {
    /**
     * 消息子类注册列表，包含有哪些具体的子消息。
     */
    public static final int RpcRequestMessage = 0;
    public static final int RpcResponseMessage = 1;
    private static final Map<Integer, Class<? extends Message>> messageClasses = new HashMap<>();

    // 初始化编号--子类的映射
    static {
        messageClasses.put(RpcRequestMessage, RpcRequestMessage.class);
        messageClasses.put(RpcResponseMessage, RpcResponseMessage.class);
    }

    /**
     * 根据消息类型码，获得对应的消息类型。
     *
     * @param messageType 消息类型码
     * @return 消息类型
     */
    public static Class<? extends Message> getMessageClass(int messageType) {
        return messageClasses.get(messageType);
    }

    /**
     * 得到一个消息的类型码。
     *
     * @return 消息类型码
     */
    // jackson会序列化所有被public修饰的字段->所有被public修饰的getter->所有被public修饰的setter
    // 此getter不参与序列化，需要特别标注。否则序列化数据会多出一个messageType。
    @JsonIgnore
    public abstract int getMessageType();
}
