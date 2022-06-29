package top.mffseal.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.Message;
import top.mffseal.rpc.enumeration.RpcError;
import top.mffseal.rpc.exception.RpcException;
import top.mffseal.rpc.serializer.CommonSerializer;

import java.util.List;

/**
 * 自定义协议编解码器
 *
 * <table style="border:1px solid gray;border-collapse:collapse;">
 *     <tr>
 *         <td style="border:1px solid gray;text-align:center"><p>Magic Number</p> <p>4 bytes</p></td>
 *         <td style="border:1px solid gray;text-align:center"><p>Package Type</p> <p>4 bytes</p></td>
 *         <td style="border:1px solid gray;text-align:center"><p>Serializer Type</p> <p>4 bytes</p></td>
 *         <td style="border:1px solid gray;text-align:center"><p>Data Length</p> <p>4 bytes</p></td>
 *     </tr>
 *     <tr style="border:1px solid gray;text-align:center">
 *         <td colspan="4"><p>Data Bytes</p><p>Length: ${Data Length}</p></td>
 *     </tr>
 * </table>
 *
 * @author mffseal
 */
@ChannelHandler.Sharable
public class CommonCodec extends MessageToMessageCodec<ByteBuf, Message> {
    private static final Logger log = LoggerFactory.getLogger(CommonCodec.class);
    private static final int MAGIC_NUMBER = 0xCAFEBABE;
    private final CommonSerializer serializer;

    public CommonCodec(CommonSerializer serializer) {
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> outList) throws Exception {
        ByteBuf out = ctx.alloc().buffer();
        out.writeInt(MAGIC_NUMBER);  // 魔数
        out.writeInt(msg.getMessageType());  // 消息类型
        out.writeInt(serializer.getCode());  // 序列化类型
        byte[] bytes = serializer.serialize(msg);
        out.writeInt(bytes.length);  // 序列化长度
        out.writeBytes(bytes);  // 序列化内容
        outList.add(out);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 魔数
        int magic = in.readInt();
        if (magic != MAGIC_NUMBER) {
            log.error("未识别的协议包类型: {}", magic);
            throw new RpcException(RpcError.UNKNOWN_PROTOCOL);
        }

        // 包类型
        int packageCode = in.readInt();
        Class<?> packageClass = Message.getMessageClass(packageCode);
        if (packageClass == null) {
            log.error("未识别的rpc数据包类型ID: {}", packageCode);
            throw new RpcException(RpcError.UNKNOWN_PACKAGE_TYPE);
        }

        // 序列化器类型
        int serializerCode = in.readInt();
        CommonSerializer serializer = CommonSerializer.getByCode(serializerCode);
        if (serializer == null) {
            log.error("未识别的反序列化器类型ID: {}", serializerCode);
            throw new RpcException(RpcError.UNKNOWN_SERIALIZER);
        }

        // 序列化数据
        int length = in.readInt();
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        Object obj = serializer.deserialize(bytes, packageClass);
        out.add(obj);
    }
}
