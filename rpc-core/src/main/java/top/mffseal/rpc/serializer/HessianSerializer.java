package top.mffseal.rpc.serializer;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.exception.SerializeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * 基于Hessian的序列化器。
 *
 * @author mffseal
 */
public class HessianSerializer implements Serializer {
    private static final Logger log = LoggerFactory.getLogger(HessianSerializer.class);

    @Override
    public byte[] serialize(Object obj) {
        HessianOutput hessianOutput = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            hessianOutput = new HessianOutput(bos);
            hessianOutput.writeObject(obj);
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("序列化失败: ", e);
            throw new SerializeException("序列化失败");
        } finally {
            if (hessianOutput != null) {
                try {
                    hessianOutput.close();
                } catch (IOException e) {
                    log.error("Hessian流关闭失败: ", e);
                }
            }
        }
    }

    @Override
    public Object deserialize(byte[] bytes, Class<?> clazz) {
        HessianInput hessianInput = null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes)) {
            hessianInput = new HessianInput(bis);
            return hessianInput.readObject();
        } catch (IOException e) {
            log.error("反序列化失败: ", e);
            throw new SerializeException("反序列化失败");
        } finally {
            if (hessianInput != null) {
                hessianInput.close();
            }
        }
    }
}
