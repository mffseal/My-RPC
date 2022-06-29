package top.mffseal.rpc.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.RpcRequestMessage;

import java.io.IOException;

/**
 * 使用Jackson实现的序列化器。
 *
 * @author mffseal
 */
public class JacksonSerializer implements Serializer {
    private static final Logger log = LoggerFactory.getLogger(JacksonSerializer.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("序列化失败: ", e);
        }
        return null;
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try {
            Object obj = objectMapper.readValue(bytes, clazz);

            // RpcRequest中的参数列表进行处理
            // TODO: 2022/6/28 疑似存在问题
            if (obj instanceof RpcRequestMessage) {
                obj = handleRequest(obj);
            }
            return obj;
        } catch (IOException e) {
            log.error("反序列化失败: ", e);
        }
        return null;
    }

    /**
     * 由于RpcRequest 中的调用参数为Object数组，json序列化的Object会丢失类型信息，
     * (其它反序列化方法转成字节数组会保留信息，不会遇到上述问题)，
     * 因此需要通过RpcRequest中的参数类型列表辅助进行反序列化。
     *
     * @param obj 反序列化后的对象
     * @return rpcRequest对象
     */
    private Object handleRequest(Object obj) throws IOException {
        RpcRequestMessage rpcRequestMessage = (RpcRequestMessage) obj;

        // 遍历参数类型列表中的每个类型
        for (int i = 0; i < rpcRequestMessage.getParamTypes().length; i++) {
            Class<?> clazz = rpcRequestMessage.getParamTypes()[i];
            // 判断参数列表中的所有参数是否和参数类型列表中的类型一一对应
            // 如果不对应了，那么用指定类型原地序列化再反序列化一次，并写回
            if (!clazz.isAssignableFrom(rpcRequestMessage.getParameters()[i].getClass())) {
                byte[] bytes = objectMapper.writeValueAsBytes(rpcRequestMessage.getParameters()[i]);
                rpcRequestMessage.getParameters()[i] = objectMapper.readValue(bytes, clazz);
            }
        }
        return rpcRequestMessage;
    }
}
