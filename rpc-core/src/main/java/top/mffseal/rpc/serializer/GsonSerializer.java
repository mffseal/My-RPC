package top.mffseal.rpc.serializer;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.RpcRequestMessage;
import top.mffseal.rpc.exception.SerializeException;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * @author mffseal
 */
public class GsonSerializer implements Serializer {
    private static final Logger log = LoggerFactory.getLogger(GsonSerializer.class);
    private static final ClassCodec classCodec = new ClassCodec();

    @Override
    public byte[] serialize(Object obj) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, classCodec).create();
        String json = gson.toJson(obj);
        return json.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, classCodec).create();
        String json = new String(bytes, StandardCharsets.UTF_8);
        Object obj = gson.fromJson(json, clazz);

        // RpcRequest中的参数列表进行处理
        if (obj instanceof RpcRequestMessage) {
            obj = Util.handleRequestParametersList(this, obj);
        }
        return obj;
    }

    /**
     * 自定义Gson解析器，用户处理Class类。
     */
    static class ClassCodec implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>> {

        @Override
        public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                String str = json.getAsString();
                return Class.forName(str);
            } catch (ClassNotFoundException e) {
                log.error("反序列化失败", e);
                throw new SerializeException("反序列化失败");
            }
        }

        @Override
        public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getName());
        }
    }
}
