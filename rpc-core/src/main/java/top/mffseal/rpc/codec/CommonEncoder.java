package top.mffseal.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import top.mffseal.rpc.entity.RpcRequestMessage;

/**
 * 自定义协议编码器
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
public class CommonEncoder extends MessageToByteEncoder<RpcRequestMessage> {
    private static final int MAGIC_NUMBER = 0xCAFEBABE;

    @Override
    protected void encode(ChannelHandlerContext ctx, RpcRequestMessage msg, ByteBuf out) throws Exception {

    }
}
