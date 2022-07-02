package top.mffseal.rpc.transport.socket.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.config.Config;
import top.mffseal.rpc.entity.Message;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 原生socket接入统一序列化接口，编码器。
 *
 * @author mffseal
 */
//TODO 和netty的解码器代码合并
public class ObjectWriter {
    private static final Logger log = LoggerFactory.getLogger(ObjectReader.class);
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    public static void writeObject(OutputStream out, Object msg) throws IOException {
        out.write(intToBytes(MAGIC_NUMBER));  // 魔数
        log.info("{}", ((Message) msg).getMessageType());
        out.write(intToBytes(((Message) msg).getMessageType()));  // 消息类型
        out.write(intToBytes(Config.getSerializerLibrary().ordinal()));  // 序列化类型
        byte[] bytes = Config.getSerializerLibrary().serialize(msg);  // 通过配置类获取序列化架构
        out.write(intToBytes(bytes.length));  // 序列化长度
        out.write(bytes);  // 序列化内容
        out.flush();
    }

    /**
     * int数字转字节数组，小端序。
     *
     * @param intNum int数字
     * @return 字节数组
     */
    private static byte[] intToBytes(int intNum) {
        byte[] des = new byte[4];
        // 数字低位放在byte数组中的低位
        des[0] = (byte) ((intNum >> 24) & 0xFF);
        des[1] = (byte) ((intNum >> 16) & 0xFF);
        des[2] = (byte) ((intNum >> 8) & 0xFF);
        des[3] = (byte) (intNum & 0xFF);
        return des;
    }
}
