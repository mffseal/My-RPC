package top.mffseal.rpc.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.RpcRequestMessage;
import top.mffseal.rpc.exception.SerializeException;

import java.io.IOException;

/**
 * 基于Jackson的序列化器。
 *
 * @author mffseal
 */
public class JacksonSerializer implements Serializer {
    private static final Logger log = LoggerFactory.getLogger(JacksonSerializer.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("序列化失败: ", e);
            throw new SerializeException("序列化失败");
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try {
            Object obj = objectMapper.readValue(bytes, clazz);

            // RpcRequest中的参数列表进行处理
            if (obj instanceof RpcRequestMessage) {
                obj = Util.handleRequestParametersList(this, obj);
            }
            return obj;
        } catch (IOException e) {
            log.error("反序列化失败: ", e);
            throw new SerializeException("反序列化失败");
        }
    }
}
