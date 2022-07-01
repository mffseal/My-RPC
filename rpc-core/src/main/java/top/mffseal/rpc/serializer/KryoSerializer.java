package top.mffseal.rpc.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.RpcRequestMessage;
import top.mffseal.rpc.entity.RpcResponseMessage;
import top.mffseal.rpc.exception.SerializeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 基于Kryo的序列化。
 *
 * @author mffseal
 */
// TODO: 2022/6/30 kryo数据压缩研究
public class KryoSerializer implements Serializer {
    private static final Logger log = LoggerFactory.getLogger(KryoSerializer.class);

    /**
     * Kryo实例不是线程安全的，每个线程需要独立的Kryo对象。
     */
    private static final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        kryo.register(RpcResponseMessage.class);
        kryo.register(RpcRequestMessage.class);
        kryo.setReferences(true);
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             Output output = new Output(bos)) {
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output, obj);
            kryoThreadLocal.remove();
            return output.toBytes();
        } catch (IOException e) {
            log.error("序列化失败: ", e);
            throw new SerializeException("序列化失败");
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             Input input = new Input(bis)) {
            Kryo kryo = kryoThreadLocal.get();
            Object obj = kryo.readObject(input, clazz);
            kryoThreadLocal.remove();
            return obj;
        } catch (Exception e) {
            log.error("反序列化失败: ", e);
            throw new SerializeException("反序列化失败");
        }
    }
}
