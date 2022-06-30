package top.mffseal.rpc.socket.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.Message;
import top.mffseal.rpc.enumeration.RpcError;
import top.mffseal.rpc.exception.RpcException;
import top.mffseal.rpc.serializer.Serializer;

import java.io.IOException;
import java.io.InputStream;

/**
 * 原生socket接入统一反序列化接口，解码器。
 * @author mffseal
 */
//TODO 和netty的解码器代码合并
public class ObjectReader {
    private static final Logger log = LoggerFactory.getLogger(ObjectReader.class);
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    public static Object readObject(InputStream in) throws IOException {
        byte[] intBytes = new byte[4];

        in.read(intBytes);
        // 魔数
        int magic = bytesToInt(intBytes);
        if (magic != MAGIC_NUMBER) {
            log.error("未识别的协议包类型: {}", magic);
            throw new RpcException(RpcError.UNKNOWN_PROTOCOL);
        }

        // 包类型
        in.read(intBytes);
        int packageCode = bytesToInt(intBytes);
        Class<?> packageClass = Message.getMessageClass(packageCode);
        if (packageClass == null) {
            log.error("未识别的rpc数据包类型ID: {}", packageCode);
            throw new RpcException(RpcError.UNKNOWN_PACKAGE_TYPE);
        }

        // 序列化器类型
        in.read(intBytes);
        int serializerCode = bytesToInt(intBytes);
        Serializer serializer = Serializer.Library.values()[serializerCode];
        if (serializer == null) {
            log.error("未识别的反序列化器类型ID: {}", serializerCode);
            throw new RpcException(RpcError.UNKNOWN_SERIALIZER);
        }

        // 序列化数据
        in.read(intBytes);
        int length = bytesToInt(intBytes);
        byte[] bytes = new byte[length];
        in.read(bytes);
        return serializer.deserialize(bytes, packageClass);


    }

    /**
     * 字节数组转int，小端序兼容Netty。
     * @param bytes 字节数组
     * @return int数字
     */
    private static int bytesToInt(byte[] bytes) {
        int intNum;
        // byte低位放的是数字低位
        intNum = (bytes[0] & 0xFF)
                | ((bytes[1] & 0xFF)<<8)
                | ((bytes[2] & 0xFF)<<16)
                | ((bytes[3] & 0xFF)<<24);
        return intNum;
    }

}