package top.mffseal.rpc.serializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.exception.SerializeException;

import java.io.*;

/**
 * @author mffseal
 */
public class NativeSerializer implements Serializer {
    private static final Logger log = LoggerFactory.getLogger(NativeSerializer.class);

    @Override
    public byte[] serialize(Object obj) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("序列化失败: ", e);
            throw new SerializeException("序列化失败");
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("反序列化失败", e);
            throw new SerializeException("反序列化失败");
        }
    }
}
