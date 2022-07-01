package top.mffseal.rpc.codec;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * 帧解码器，处理半包粘包。
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
public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {
    public ProtocolFrameDecoder() {
        this(4096, 12, 4, 0, 0);
    }

    public ProtocolFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
